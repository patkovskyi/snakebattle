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
import java.util.List;
import java.util.Random;

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
    // TODO: || needResync
    if (game == null || isNewRound(board)) {
      // initialize from scratch
      System.out.println("NEW ROUND!");
      game = initializeGame(board.boardAsString());
      // System.out.print(printerFactory.getPrinter(game.reader(),
      // game.getPlayers().get(0)).print());
    } else {
      // continue existing game
      game = continueGame(game, board);

      if (game == null) {
        // re-sync :(
        game = initializeGame(board.boardAsString());
      }
    }

    for (int i = 0; i < game.getHeroes().size(); i++) {
      System.out.printf("Hero %d: %s\n", i, game.getHeroes().get(i));
    }

    return setExpectationsAndReturn(getRandomDirection(), false);
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

    Hero hero = level.getHero();
    if (hero != null) {
      hero.setActive(true);
      Player heroPlayer = new Player(new EmulatingEventListener(0));
      game.newGame(heroPlayer);
      heroPlayer.setHero(hero);
      hero.init(game);
    }

    List<Hero> enemies = level.getEnemies();
    for (int i = 0; i < enemies.size(); i++) {
      Hero enemy = enemies.get(i);
      enemy.setActive(true);
      final int j = i + 1;
      Player enemyPlayer = new Player(new EmulatingEventListener(j));
      game.newGame(enemyPlayer);
      enemyPlayer.setHero(enemy);
      enemy.init(game);
    }

    return game;
  }

  private SnakeBoard continueGame(SnakeBoard game, Board expectedBoard) {
    long start = System.currentTimeMillis();

    List<Integer[]> permutations = new ArrayList<>();
    List<Hero> heroes = game.getHeroes();
    getPermutations(new Integer[heroes.size()], permutations, 0, 6);
    System.out.printf(
        "Found %d permutations for %d players.\n", permutations.size(), heroes.size());

    int skipped = 0;

    for (int i = 0; i < permutations.size(); i++) {
      Integer[] actions = permutations.get(i);
      // check if this might be The One
      boolean theOne = true;
      for (int j = 0; j < heroes.size(); j++) {
        Point head = heroes.get(j).head().copy();
        Direction newDirection = heroes.get(j).newDirection(actions[j]);
        head.change(newDirection);
        if (!expectedBoard.containsPoint(head)) {
          theOne = false;
          break;
        }

        Elements elementAtHead = expectedBoard.getAt(head);
        Point tail = heroes.get(j).getTailPoint();
        Elements elementAtTail = expectedBoard.getAt(tail);
        if (elementAtHead == Elements.NONE
            || elementAtHead == Elements.WALL
            || (actions[j] >= 3 && elementAtTail == Elements.NONE)) {
          theOne = false;
          break;
        }
      }

      if (!theOne) {
        ++skipped;
        continue;
      }

      SnakeBoard clonedGame = cloner.deepClone(game);

      List<Hero> clonedHeroes = clonedGame.getHeroes();
      for (int j = 0; j < actions.length; j++) {
        clonedHeroes.get(j).setAction(actions[j]);
      }

      clonedGame.tick();
      String clonedBoardString = gameAsString(clonedGame);

      //      if (i == 0) {
      //        System.out.println("CLONED BOARD all counter-clockwise: ");
      //        System.out.print(clonedBoardString);
      //      }

      if (BoardStringComparator.movesEqual(clonedBoardString, expectedBoard.boardAsString())) {
        System.out.printf(
            "SUCCESS! Skipped %d, found continuation in %d ms\n",
            skipped, System.currentTimeMillis() - start);
        return clonedGame;
      }
    }

    System.out.printf(
        "FAIL! Skipped %d, failed continuation in %d ms\n",
        skipped, System.currentTimeMillis() - start);
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
    return Direction.onlyDirections().get(random.nextInt(4));
  }
}
