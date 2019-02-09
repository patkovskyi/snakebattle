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
import java.util.List;
import java.util.stream.Collectors;

public class GameHelper {

  public static final int MIN_SNAKE_LENGTH = 2;
  private static final Cloner cloner = new Cloner();
  private static final PrinterFactoryImpl printerFactory = new PrinterFactoryImpl();

  static {
    cloner.registerImmutable(EmulatingEventListener.class);
  }

  public static SnakeBoard getNewOrContinuedGame(SnakeBoard game, Board boardFromServer) {
    if (game == null || boardFromServer.isNewRound()) {
      // initialize new game
      System.out.println("NEW ROUND!");
      game = initializeGame(boardFromServer);
    } else {
      game = GameHelper.continueGame(game, boardFromServer);

      if (game == null) {
        // re-sync required :(
        System.out.println("FAIL: RESYNC :(");
        game = initializeGame(boardFromServer);
      }
    }

    if (!game.getHeroes().get(0).isAlive() && game.getHeroes().stream().anyMatch(h -> h.isAlive())) {
      System.out.println("ROUND LOST");
    }

    return game;
  }

  public static int getManhattanDistance(Point p1, Point p2) {
    return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
  }

  public static SnakeBoard initializeGame(Board board) {
    String boardString = board.boardAsString();
    LevelImpl level = new LevelImpl(boardString.replaceAll("\n", ""));
    SnakeBoard game =
        new SnakeBoard(
            level,
            new RandomDice(),
            new Timer(new SimpleParameter<>(0)),
            new Timer(new SimpleParameter<>(300)),
            new SimpleParameter<>(0), // rounds per one match
            new SimpleParameter<>(10),
            new SimpleParameter<>(10),
            new SimpleParameter<>(3),
            new SimpleParameter<>(2));

    boolean snakesActive = !board.isNewRound();

    Hero hero = level.getHero();
    if (hero != null) {
      Player heroPlayer = new Player(new EmulatingEventListener(0));
      game.newGame(heroPlayer);
      heroPlayer.setHero(hero);
      hero.setActive(snakesActive);
      hero.init(game);
    }

    List<Hero> enemies = level.getEnemies();
    for (int i = 0; i < enemies.size(); i++) {
      Hero enemy = enemies.get(i);
      final int j = i + 1;
      Player enemyPlayer = new Player(new EmulatingEventListener(j));
      game.newGame(enemyPlayer);
      enemyPlayer.setHero(enemy);
      enemy.init(game);
      enemy.setActive(snakesActive);
    }

    game.addWallsBehindSleepingHeroes();
    return game;
  }

  private static SnakeBoard continueGame(SnakeBoard game, Board expectedBoard) {
    long start = System.currentTimeMillis();

    if (game.getHeroes().stream().allMatch(h -> !h.isActive())) {
      // simply activate heroes and return the same board
      game.getHeroes().forEach(h -> h.setActive(true));
      if (gameAsString(game).equals(expectedBoard.boardAsString())) {
        System.out.println("SUCCESS activating heroes");
      } else {
        System.out.println("FAIL activating heroes");
      }

      copyObjectsFromBoardToGame(game, expectedBoard);
      return game;
    }

    List<Hero> heroes =
        game.getHeroes().stream().filter(h -> h.isAlive()).collect(Collectors.toList());

    if (expectedBoard.get(Elements.HEAD_DEAD, Elements.ENEMY_HEAD_DEAD).size() == heroes.size()) {
      // round ended by timeout
      System.out.println("ROUND ENDED");
      heroes.forEach(h -> h.setAlive(false));
      return game;
    }

    List<Integer[]> permutations = new ArrayList<>();
    getPermutations(new Integer[heroes.size()], permutations, 0, 3);
    System.out.printf(
        "Found %d permutations for %d players.\n", permutations.size(), heroes.size());

    int skipped = 0;

    List<Integer[]> consideredPerms = new ArrayList<>();
    String clonedBoardString = "";
    for (int i = 0; i < permutations.size(); i++) {
      Integer[] actions = permutations.get(i);
      // check if this might be The One
      boolean theOne = true;
      for (int j = 0; j < heroes.size(); j++) {
        Point head = heroes.get(j).head().copy();
        Direction newDirection = heroes.get(j).getRelativeDirection(actions[j]);
        head.change(newDirection);
        if (!expectedBoard.containsPoint(head)) {
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
            "SUCCESS! Skipped %d, found continuation in %d ms\n",
            skipped, System.currentTimeMillis() - start);
        return clonedGame;
      }
    }

    System.out.printf(
        "FAIL! Skipped %d, failed continuation in %d ms. Considered permutations: \n",
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

  private static void copyObjectsFromBoardToGame(SnakeBoard game, Board board) {
    board.getApples().forEach(a -> game.setApple(a));
    board.getGold().forEach(g -> game.setGold(g));
    board.getStones().forEach(s -> game.setStone(s));
    board.getFuryPills().forEach(f -> game.setFuryPill(f));
    board.getFlyingPills().forEach(f -> game.setFlyingPill(f));
  }
}
