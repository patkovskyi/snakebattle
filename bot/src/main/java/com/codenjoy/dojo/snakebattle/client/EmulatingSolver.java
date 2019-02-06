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

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.services.settings.SimpleParameter;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.codenjoy.dojo.snakebattle.model.Player;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.board.Timer;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.level.LevelImpl;
import com.rits.cloning.Cloner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EmulatingSolver implements Solver<Board> {
  private static final int PLAYERS_IN_ROUND = 5;
  private final Random random = new Random();
  private final Cloner cloner = new Cloner();
  private final PrinterFactoryImpl printerFactory = new PrinterFactoryImpl();
  private Point myHead;
  private SnakeBoard game;

  public EmulatingSolver() {
    cloner.registerImmutable(EmulatingEventListener.class);
  }

  @Override
  public String get(Board board) {
    try {
      // TODO: || needResync
      if (game == null || isNewRound(board)) {
        // initialize from scratch
        System.out.println("NEW ROUND!");
        game = initializeGame(board.boardAsString());
        game.addWallsBehindSleepingHeroes();
        // System.out.print(printerFactory.getPrinter(game.reader(),
        // game.getPlayers().get(0)).print());
      } else {
        System.out.println(
            "Active heroes: " + game.getHeroes().stream().filter(h -> h.isActive()).count());
        System.out.println(
            "Alive heroes: " + game.getHeroes().stream().filter(h -> h.isAlive()).count());
        game = continueGame(game, board);

        if (game == null) {
          throw new IllegalStateException("Failed to continue");

          // re-sync :(
          // game = initializeGame(board.boardAsString());
        }
      }

      System.out.println("Tracked game: ");
      System.out.println(gameAsString(game));

      List<Hero> aliveHeroes =
          game.getHeroes().stream().filter(h -> h.isAlive()).collect(Collectors.toList());
      for (int i = 0; i < aliveHeroes.size(); i++) {
        System.out.printf("Hero %d: %s\n", i, aliveHeroes.get(i));
      }

      return setExpectationsAndReturn(getRandomDirection(), false);
    } catch (Exception e) {
      e.printStackTrace();
      // throw e;
      return "NO ACTION";
    }
  }

  private String setExpectationsAndReturn(Direction direction, boolean leaveStone) {
    myHead = game.getHeroes().get(0).head().copy();
    myHead.change(direction);
    return direction.toString();
  }

  private boolean isNewRound(Board board) {
    return board.get(Elements.HEAD_SLEEP).size() == 1;
    // && board.get(Elements.ENEMY_HEAD_SLEEP).size() == PLAYERS_IN_ROUND - 1;
  }

  private SnakeBoard initializeGame(String boardString) {
    LevelImpl level = new LevelImpl(boardString.replaceAll("\n", ""));
    SnakeBoard game =
        new SnakeBoard(
            level,
            new RandomDice(),
            new Timer(new SimpleParameter<>(0)),
            new Timer(new SimpleParameter<>(300)),
            new SimpleParameter<>(0),
            new SimpleParameter<>(10),
            new SimpleParameter<>(10),
            new SimpleParameter<>(3));

    boolean snakesActive = boardString.indexOf(Elements.HEAD_SLEEP.ch()) < 0;

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

    return game;
  }

  private SnakeBoard continueGame(SnakeBoard game, Board expectedBoard) {
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

    List<Integer[]> permutations = new ArrayList<>();
    List<Hero> heroes =
        game.getHeroes().stream().filter(h -> h.isAlive()).collect(Collectors.toList());
    getPermutations(new Integer[heroes.size()], permutations, 0, 4);
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

      //      if (i == 0) {
      //        System.out.println("CLONED BOARD all counter-clockwise: ");
      //        System.out.print(clonedBoardString);
      //      }

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

    System.out.println("Last tried board: ");
    System.out.println(clonedBoardString);
    return null;
  }

  private String gameAsString(SnakeBoard game) {
    return (String) printerFactory.getPrinter(game.reader(), game.getPlayers().get(0)).print();
  }

  private void getPermutations(Integer[] current, List<Integer[]> list, int n, int directions) {
    if (n == current.length) {
      list.add(current.clone());
    } else {
      for (int i = 0; i < directions; i++) {
        current[n] = i;
        getPermutations(current, list, n + 1, directions);
      }
    }
  }

  private Direction getRandomDirection() {
    if (game != null) {
      Hero me = game.getHeroes().get(0);
      for (int i = 0; i < 3; i++) {
        Direction newDirection = me.getRelativeDirection(i);
        Point newHead = me.head().copy();
        newHead.change(newDirection);

        if (game.isFree(newHead)) {
          return newDirection;
        }
      }
    }

    return Direction.onlyDirections().get(random.nextInt(4));
  }

  private void copyObjectsFromBoardToGame(SnakeBoard game, Board board) {
    board.getApples().forEach(a -> game.setApple(a));
    board.getGold().forEach(g -> game.setGold(g));
    board.getStones().forEach(s -> game.setStone(s));
    board.getFuryPills().forEach(f -> game.setFuryPill(f));
    board.getFlyingPills().forEach(f -> game.setFlyingPill(f));
  }
}
