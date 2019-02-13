package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.services.settings.SimpleParameter;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.codenjoy.dojo.snakebattle.model.EmulatingEventListener;
import com.codenjoy.dojo.snakebattle.model.Player;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.board.Timer;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.level.LevelImpl;
import com.rits.cloning.Cloner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameHelper {

  private static final Cloner cloner = new Cloner();
  private static final PrinterFactoryImpl printerFactory = new PrinterFactoryImpl();
  private static int tick = 0;

  static {
    cloner.registerImmutable(EmulatingEventListener.class);
  }

  public static int getTick(SnakeBoard game) {
    return tick;
  }

  public static void setTick(int tick) {
    GameHelper.tick = tick;
  }

  public static SnakeBoard initializeGame(MyBoard board) {
    tick = 0;

    String boardString = board.boardAsString();
    LevelImpl level = new LevelImpl(boardString.replaceAll("\n", ""));
    SnakeBoard game = new SnakeBoard(
        level,
        new RandomDice(),
        new Timer(new SimpleParameter<>(0)),
        new Timer(new SimpleParameter<>(300)),
        new Timer(new SimpleParameter<>(1)),
        new SimpleParameter<>(1), // rounds per one match
        new SimpleParameter<>(10),
        new SimpleParameter<>(10),
        new SimpleParameter<>(3),
        new SimpleParameter<>(40));

    Hero hero = level.getHero(game);
    if (hero != null) {
      Player heroPlayer = new Player(new EmulatingEventListener(0));
      game.newGame(heroPlayer);
      heroPlayer.setHero(hero);
      hero.setActive(board.getAt(hero.head()) != Elements.HEAD_SLEEP);
      hero.init(game);
    }

    List<Hero> enemies = level.getEnemies(game);
    for (int i = 0; i < enemies.size(); i++) {
      Hero enemy = enemies.get(i);
      final int j = i + 1;
      Player enemyPlayer = new Player(new EmulatingEventListener(j));
      game.newGame(enemyPlayer);
      enemyPlayer.setHero(enemy);
      enemy.init(game);
      enemy.setActive(board.getAt(enemy.head()) != Elements.ENEMY_HEAD_SLEEP);
    }

    game.addWallsBehindSleepingHeroes();
    return game;
  }

  public static SnakeBoard continueGame(SnakeBoard game, MyBoard expectedBoard) {
    long start = System.currentTimeMillis();

    ++tick;

    if (game.getHeroes().stream().allMatch(h -> !h.isActive())) {
      // simply activate heroes and return the same board
      game.getHeroes().forEach(h -> h.setActive(true));
      if (gameAsString(game).equals(expectedBoard.boardAsString())) {
        System.out.println("continueGame: SUCCESS activating heroes");
      } else {
        System.out.println("continueGame: FAIL activating heroes");
      }

      copyObjectsFromBoardToGame(game, expectedBoard);
      return game;
    }

    List<Hero> trackedAliveHeroes =
        game.getHeroes().stream().filter(h -> h.isAlive()).collect(Collectors.toList());

    if (expectedBoard.get(Elements.HEAD_DEAD, Elements.ENEMY_HEAD_DEAD).size() == trackedAliveHeroes
        .size()) {
      // round ended by timeout
      System.out.println("continueGame: round ended by timeout");
      trackedAliveHeroes.forEach(h -> h.setAlive(false));
      return game;
    }

    List<Integer[]> permutations = new ArrayList<>();
    getPermutations(new Integer[trackedAliveHeroes.size()], permutations, 0, 3);
    System.out.printf(
        "Found %d permutations for %d players.", permutations.size(), trackedAliveHeroes.size());

    int skipped = 0;

    List<Integer[]> consideredPerms = new ArrayList<>();
    String clonedBoardString = "";
    for (int i = 0; i < permutations.size(); i++) {
      Integer[] actions = permutations.get(i);
      // check if this might be The One
      boolean theOne = true;
      for (int j = 0; j < trackedAliveHeroes.size(); j++) {
        Point head = trackedAliveHeroes.get(j).head().copy();
        Direction newDirection = trackedAliveHeroes.get(j).getRelativeDirection(actions[j]);
        head.change(newDirection);
        if (head.isOutOf(expectedBoard.size())) {
          theOne = false;
          break;
        }

        Elements elementAtHead = expectedBoard.getAt(head);
        if (elementAtHead == Elements.NONE
            || elementAtHead == Elements.WALL
            || elementAtHead == Elements.START_FLOOR) {
          theOne = false;
          break;
        }
      }

      if (!theOne) {
        ++skipped;
        continue;
      }

      consideredPerms.add(actions);
      SnakeBoard clonedGame = cloner.deepClone(game);

      List<Hero> clonedHeroes =
          clonedGame.getHeroes().stream().filter(h -> h.isAlive()).collect(Collectors.toList());
      for (int j = 0; j < actions.length; j++) {
        clonedHeroes.get(j).setRelativeDirection(actions[j]);
      }

      clonedGame.tick();
      clonedBoardString = gameAsString(clonedGame);

      if (BoardStringComparator.movesEqual(clonedBoardString, expectedBoard.boardAsString())) {
        copyObjectsFromBoardToGame(clonedGame, expectedBoard);
        System.out.printf(
            "SUCCESS! Skipped %d, tracked in %d ms\n",
            skipped, System.currentTimeMillis() - start);
        return clonedGame;
      }
    }

    System.out.printf(
        "FAIL! Skipped %d, lost track in %d ms. Considered: \n",
        skipped, System.currentTimeMillis() - start);
    consideredPerms.forEach(p -> System.out.println(Arrays.toString(p)));

    System.out.println("Last tracked game: ");
    System.out.println(gameAsString(game));

    System.out.println("Last board tried: ");
    System.out.println(clonedBoardString);
    return null;
  }

  private static String gameAsString(SnakeBoard game) {
    return (String) printerFactory.getPrinter(game.reader(), game.getPlayers().get(0)).print();
  }

  private static void getPermutations(
      Integer[] current, List<Integer[]> list, int n, int directions) {
    if (n == current.length) {
      list.add(current.clone());
    } else {
      for (int i = 0; i < directions; i++) {
        current[n] = i;
        getPermutations(current, list, n + 1, directions);
      }
    }
  }

  private static void copyObjectsFromBoardToGame(SnakeBoard game, MyBoard board) {
    board.getApples().forEach(a -> game.setApple(a));
    board.getGold().forEach(g -> game.setGold(g));
    board.getStones().forEach(s -> game.setStone(s));
    board.getFuryPills().forEach(f -> game.setFuryPill(f));
    board.getFlyingPills().forEach(f -> game.setFlyingPill(f));
  }

  public static class BoardStringComparator {

    private static final Set<Elements> objects = new HashSet<>(
        Arrays.asList(Elements.GOLD, Elements.APPLE, Elements.STONE, Elements.FURY_PILL,
            Elements.FLYING_PILL));

    public static boolean movesEqual(String board1, String board2) {
      if (board1.length() != board2.length()) {
        return false;
      }

      for (int i = 0; i < board1.length(); i++) {
        char c1 = board1.charAt(i);
        char c2 = board2.charAt(i);
        if (c1 == c2) {
          continue;
        }

        if (objects.contains(Elements.valueOf(c1))) {
          c1 = ' ';
        }
        if (objects.contains(Elements.valueOf(c2))) {
          c2 = ' ';
        }
        if ((c1 == Elements.HEAD_DEAD.ch() || c1 == Elements.ENEMY_HEAD_DEAD.ch()) && c2 != ' ') {
          continue;
        }

        if ((c2 == Elements.HEAD_DEAD.ch() || c2 == Elements.ENEMY_HEAD_DEAD.ch()) && c1 != ' ') {
          continue;
        }

        if (c1 != c2) {
          return false;
        }
      }

      return true;
    }
  }
}
