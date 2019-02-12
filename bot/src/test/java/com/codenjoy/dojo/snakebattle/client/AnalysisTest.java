package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
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
 * You staticDistanceTrivial have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.snakebattle.model.HeroAction;
import com.codenjoy.dojo.snakebattle.model.Player;
import com.codenjoy.dojo.snakebattle.model.TestUtils;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class AnalysisTest {

  @Rule
  public TestRule globalTimeout = new DisableOnDebug(new Timeout(500, TimeUnit.MILLISECONDS));
  private SnakeBoard game;
  private Hero hero;

  private void newGame(String boardAsString) {
    game = GameHelper.initializeGame(new MyBoard().forString(boardAsString));
    hero = game.getHeroes().get(0);
  }

  private void assertBoard(String expected, Player player) {
    assertEquals(com.codenjoy.dojo.utils.TestUtils.injectN(expected),
        new PrinterFactoryImpl().getPrinter(game.reader(), player).print());
  }

  private void assertH(String expected) {
    assertBoard(expected, game.getPlayers().get(0));
  }

  private void assertEqual(String expected, int[][] arr) {
    StringBuilder actual = new StringBuilder();

    for (int y = arr.length; y-- > 0; ) {
      for (int x = 0; x < arr[y].length; x++) {
        if (arr[x][y] == Integer.MAX_VALUE) {
          actual.append("☼");
        } else if (arr[x][y] < 0) {
          actual.append("#");
        } else {
          actual.append(arr[x][y] % 10);
        }
      }

      actual.append("\n");
    }

    Assert.assertEquals(TestUtils.injectN(expected), actual.toString());
  }

  private void assertWithPadding(String expected, int[][] arr, int padding) {
    StringBuilder actual = new StringBuilder();

    for (int y = arr.length; y-- > 0; ) {
      for (int x = 0; x < arr[y].length; x++) {
        if (arr[x][y] == Integer.MAX_VALUE) {
          actual.append(String.format("%-" + padding + "s", "☼"));
        } else if (arr[x][y] < 0) {
          actual.append(String.format("%-" + padding + "s", "#"));
        } else {
          actual.append(String.format("%-" + padding + "s", arr[x][y]));
        }
      }

      actual.append("\n");
    }

    Assert.assertEquals(TestUtils.inject(expected, arr.length * padding, "\n"), actual.toString());
  }

  private void assertDynamicDistances(String expected) {
    int[][] arr = new GreedyAnalysis(game).getDynamicDistances(hero);
    assertEqual(expected, arr);
  }

  private void assertStaticDistances(String expected) {
    int[][] arr = new GreedyAnalysis(game).getStaticDistances(hero);
    assertEqual(expected, arr);
  }

  private void assertValues(String expected) {
    assertValues(expected, 1);
  }

  private void assertValues(String expected, int padding) {
    int[][] acc = new GreedyAnalysis(game).getValues(hero);
    assertWithPadding(expected, acc, padding);
  }

  private void assertAccumulatedValues(String expected) {
    int[][] acc = new GreedyAnalysis(game).getAccumulatedValues(hero);
    assertWithPadding(expected, acc, 2);
  }

  private void assertDeadEnds(String board, String expectedDeadEnds) {
    SnakeBoard game = GameHelper.initializeGame(new MyBoard().forString(board));
    Analysis analysis = new GreedyAnalysis(game);
    boolean[][] deadEnds = analysis.getStaticObstacles(game.getHeroes().get(0));
    StringBuilder sb = new StringBuilder();
    for (int y = deadEnds.length; y-- > 0; ) {
      for (int x = 0; x < deadEnds.length; x++) {
        sb.append(deadEnds[x][y] ? "☼" : " ");
      }
    }

    TestUtils.assertBoardsEqual(expectedDeadEnds, sb.toString());
  }

  // @formatter:off

  @Test
  public void deadEndsSmall() {
    assertDeadEnds(
              "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼☼☼   ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",

              "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼   ☼"
            + "☼☼☼☼   ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void deadEndsSmallDirectional() {
    assertDeadEnds(
              "☼☼☼☼☼☼☼☼"
            + "☼      ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼ ╘►   ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼☼☼",

              "☼☼☼☼☼☼☼☼"
            + "☼      ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼☼☼    ☼"
            + "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void deadEndsSmallDirectional2() {
    assertDeadEnds(
              "☼☼☼☼☼☼☼☼"
            + "☼      ☼"
            + "☼ ╘►   ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼☼☼",

              "☼☼☼☼☼☼☼☼"
            + "☼      ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼☼☼☼☼"
            + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void deadEndsSmallDirectional3() {
    assertDeadEnds(
              "☼☼☼☼☼☼☼☼"
            + "☼      ☼"
            + "☼ ╘►   ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼      ☼"
            + "☼☼☼ ☼☼ ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼☼☼",

              "☼☼☼☼☼☼☼☼"
            + "☼      ☼"
            + "☼      ☼"
            + "☼☼☼☼☼☼ ☼"
            + "☼☼☼    ☼"
            + "☼☼☼ ☼☼ ☼"
            + "☼☼☼    ☼"
            + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void deadEndsBig() {
    assertDeadEnds(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼┌>                         ☼"
            + "☼ø│         ®                ☼"
            + "☼☼└──ö   ●         ○         ☼"
            + "☼☼                      ○    ☼"
            + "☼☼           ●    ○          ☼"
            + "☼☼     ☼☼☼☼☼                 ☼"
            + "☼☼     ☼                     ☼"
            + "☼#     ☼☼☼     ○  ☼☼☼☼#      ☼"
            + "☼☼     ☼          ☼   ☼  ●   ☼"
            + "☼☼  ○  ☼☼☼☼ø    ○ ☼☼☼☼# ○    ☼"
            + "☼☼                ☼ ○        ☼"
            + "☼☼                ☼        ®$☼"
            + "☼☼    ●  $                   ☼"
            + "☼ø             ○ ○    ○      ☼"
            + "☼☼    ○                    ® ☼"
            + "☼☼        ☼☼☼    $           ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼   ● ☼●☼ ☼ ☼ ○  ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼    ☼"
            + "☼☼   ○          © ☼     ☼    ☼"
            + "☼☼     ●          ☼   ╓ ☼    ☼"
            + "☼☼                    ║      ☼"
            + "☼☼                    ║      ☼"
            + "☼☼             ●      ╚══╗   ☼"
            + "☼#                       ║   ☼"
            + "☼☼   ©                ◄╗ ║   ☼"
            + "☼☼                     ╚═╝   ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼     ☼☼☼☼☼                 ☼"
            + "☼☼     ☼☼☼                   ☼"
            + "☼☼     ☼☼☼        ☼☼☼☼☼      ☼"
            + "☼☼     ☼☼☼        ☼☼☼☼☼      ☼"
            + "☼☼     ☼☼☼☼☼      ☼☼☼☼☼      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼                ☼          ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼        ☼☼☼                ☼"
            + "☼☼       ☼☼☼☼                ☼"
            + "☼☼      ☼☼☼☼☼     ☼☼   ☼☼    ☼"
            + "☼☼      ☼   ☼     ☼☼☼☼☼☼☼    ☼"
            + "☼☼      ☼   ☼     ☼  ☼  ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼");
  }

  @Test
  public void staticDistanceTrivial() {
    newGame("☼☼☼☼☼"
        + "☼   ☼"
        + "╘►  ☼"
        + "☼   ☼"
        + "☼☼☼☼☼");

    assertStaticDistances("☼☼☼☼☼"
        + "☼123☼"
        + "☼012☼"
        + "☼123☼"
        + "☼☼☼☼☼");
  }

  @Test
  public void staticDistanceSimpleObstacle() {
    newGame("☼☼☼☼☼"
        + "☼   ☼"
        + "╘►☼ ☼"
        + "☼   ☼"
        + "☼☼☼☼☼");

    assertStaticDistances("☼☼☼☼☼"
        + "☼123☼"
        + "☼0☼4☼"
        + "☼123☼"
        + "☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceSleep() {
    newGame("☼☼☼☼☼"
        + "☼  ˄☼"
        + "~& │☼"
        + "☼  ¤☼"
        + "☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼"
        + "☼123☼"
        + "☼012☼"
        + "☼123☼"
        + "☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceStoneCantPass() {
    newGame("☼☼☼☼☼"
        + "☼   ☼"
        + "╘►● ☼"
        + "☼   ☼"
        + "☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼"
        + "☼123☼"
        + "☼0☼4☼"
        + "☼123☼"
        + "☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceStoneCanPass() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + " ╔►● ☼"
        + "☼║   ☼"
        + "☼╚╕  ☼"
        + "☼☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼2123☼"
        + "☼☼012☼"
        + "☼☼123☼"
        + "☼3234☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceStoneCanPassFury() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + "╘►®● ☼"
        + "☼    ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    game.tick();

    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼2123☼"
        + "☼3012☼"
        + "☼2123☼"
        + "☼3234☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceStoneCanPassFlight() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + "╘►©● ☼"
        + "☼    ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    game.tick();

    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼2123☼"
        + "☼3012☼"
        + "☼2123☼"
        + "☼3234☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyOutOfReach() {
    newGame("☼☼☼☼☼"
        + "☼  ˄☼"
        + "╘► │☼"
        + "☼  ¤☼"
        + "☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼"
        + "☼123☼"
        + "☼012☼"
        + "☼123☼"
        + "☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyWithinReach() {
    newGame("☼☼☼☼☼"
        + "☼ ˄ ☼"
        + "╘►│ ☼"
        + "☼ ¤ ☼"
        + "☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼"
        + "☼1☼5☼"
        + "☼0☼4☼"
        + "☼123☼"
        + "☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyWithinReach2() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + " ╔►˄ ☼"
        + "☼║ │ ☼"
        + "☼╙ ¤ ☼"
        + "☼☼☼☼☼☼");

    // this assumes neck hits == head hits
    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼2123☼"
        + "☼30☼4☼"
        + "☼2123☼"
        + "☼3234☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyOnThePassageDoesNotBlock() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼   ☼  ☼"
        + "☼╘► ☼  ☼"
        + "☼  ˄   ☼"
        + "☼  ¤☼  ☼"
        + "☼   ☼  ☼"
        + "☼   ☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼☼☼☼"
        + "☼212☼67☼"
        + "☼301☼56☼"
        + "☼212345☼"
        + "☼323☼56☼"
        + "☼434☼67☼"
        + "☼545☼78☼"
        + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyOnThePassageBlocks() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼   ☼  ☼"
        + "☼╘► ☼  ☼"
        + "☼  ˄   ☼"
        + "☼  │☼  ☼"
        + "☼  ¤☼  ☼"
        + "☼   ☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼☼☼☼"
        + "☼212☼☼☼☼"
        + "☼301☼☼☼☼"
        + "☼21☼☼☼☼☼"
        + "☼323☼☼☼☼"
        + "☼434☼☼☼☼"
        + "☼545☼☼☼☼"
        + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyOnThePassageDetour() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼   ☼  ☼"
        + "☼╘► ☼  ☼"
        + "☼  ˄   ☼"
        + "☼  │☼  ☼"
        + "☼  ¤   ☼"
        + "☼   ☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼☼☼☼"
        + "☼212☼01☼"
        + "☼301☼90☼"
        + "☼21☼989☼"
        + "☼323☼78☼"
        + "☼434567☼"
        + "☼545☼78☼"
        + "☼☼☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyWithinReachButIamFury() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + "╘►®˄ ☼"
        + "☼  │ ☼"
        + "☼  ¤ ☼"
        + "☼☼☼☼☼☼");

    game.tick();

    assertH("☼☼☼☼☼☼"
        + "☼  ˄ ☼"
        + " ╘♥│ ☼"
        + "☼  ¤ ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼2123☼"
        + "☼3012☼"
        + "☼2123☼"
        + "☼3234☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceEnemyWithinReachButIamLonger() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + " ╔►˄ ☼"
        + "☼║ ¤ ☼"
        + "☼╙   ☼"
        + "☼☼☼☼☼☼");

    // this assumes neck hits == head hits
    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼2123☼"
        + "☼3012☼"
        + "☼2123☼"
        + "☼3234☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void dynamicDistanceFuryEnemyWithinReachAndIamLonger() {
    newGame("☼☼☼☼☼☼"
        + " ▲ ® ☼"
        + "☼║ ˄ ☼"
        + "☼║ ¤ ☼"
        + "☼╙   ☼"
        + "☼☼☼☼☼☼");

    hero.right();
    game.tick();

    assertH("☼☼☼☼☼☼"
        + " ╔►♣ ☼"
        + "☼║ ¤ ☼"
        + "☼╙   ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    assertDynamicDistances("☼☼☼☼☼☼"
        + "☼30☼4☼"
        + "☼2123☼"
        + "☼3234☼"
        + "☼4345☼"
        + "☼☼☼☼☼☼");
  }

  @Test
  public void getValues() {
    newGame(  "☼☼☼☼☼☼"
            + "☼▲ $ ☼"
            + "☼║ ○●☼"
            + "☼║   ☼"
            + "☼╙© ®☼"
            + "☼☼☼☼☼☼");

    assertValues(
              "0 0 0 0 0 0 "
            + "0 0 0 100 0 "
            + "0 0 0 4 # 0 "
            + "0 0 0 0 0 0 "
            + "0 0 # 0 300 "
            + "0 0 0 0 0 0 ", 2);
  }

  @Test
  public void getAccummulatedValues() {
    newGame(  "☼☼☼☼☼☼"
            + "☼▲ $ ☼"
            + "☼║ ○●☼"
            + "☼║   ☼"
            + "☼╙© ®☼"
            + "☼☼☼☼☼☼");

    assertAccumulatedValues(
              "0 0 0 0 0 0 "
            + "0 0 0 10100 "
            + "0 0 0 140 0 "
            + "0 0 0 14140 "
            + "0 0 # 14440 "
            + "0 0 0 0 0 0 ");
  }

  @Test
  public void getAccummulatedValuesFight() {
    newGame(  "☼☼☼☼☼☼"
            + "☼▲˄  ☼"
            + "☼║¤  ☼"
            + "☼║   ☼"
            + "☼╙   ☼"
            + "☼☼☼☼☼☼");

    assertAccumulatedValues(
              "0 0 0 0 0 0 "
            + "0 0 7070700 "
            + "0 707070700 "
            + "0 707070700 "
            + "0 707070700 "
            + "0 0 0 0 0 0 ");
  }

  @Test
  public void getAccummulatedValuesLosingFight() {
    newGame(  "☼☼☼☼☼☼"
            + "☼▲˄  ☼"
            + "☼║│  ☼"
            + "☼║¤  ☼"
            + "☼╙   ☼"
            + "☼☼☼☼☼☼");

    assertAccumulatedValues(
              "0 0 0 0 0 0 "
            + "0 0 0 0 0 0 "
            + "0 0 0 0 0 0 "
            + "0 0 0 0 0 0 "
            + "0 0 0 0 0 0 "
            + "0 0 0 0 0 0 ");
  }

  @Test
  public void greedyAnalysisTest() {
    newGame("☼☼☼☼☼☼"
        + " ▲   ☼"
        + "☼║  ○☼"
        + "☼║   ☼"
        + "☼╙   ☼"
        + "☼☼☼☼☼☼");

    Analysis a = new GreedyAnalysis(game);
    assertEquals(HeroAction.RIGHT, a.findBestAction());
  }

  @Test
  public void greedyAnalysisTestBetterPath1() {
    newGame(
          "☼☼☼☼☼☼"
        + "☼ ●●●☼"
        + "☼ ○ ®☼"
        + "☼╘►○○☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");


    Analysis a = new GreedyAnalysis(game);
    assertEquals(HeroAction.RIGHT, a.findBestAction());
  }

  @Test
  public void greedyAnalysisTestBetterPath2() {
    newGame(
          "☼☼☼☼☼☼"
        + "☼ ●●●☼"
        + "☼ ○○®☼"
        + "☼╘► ○☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");


    Analysis a = new GreedyAnalysis(game);
    assertEquals(HeroAction.UP, a.findBestAction());
  }

  @Test
  public void shitAndEatValue() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + "╘►®● ☼"
        + "☼    ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    game.tick();
    game.tick();

    assertValues("000000"
        + "000000"
        + "005000"
        + "000000"
        + "000000"
        + "000000", 1);
  }

  @Test
  public void shitAndEatValue2() {
    newGame("☼☼☼☼☼☼"
        + "☼    ☼"
        + "╘══►®☼"
        + "☼   ●☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    game.tick();
    hero.down();
    game.tick();

    assertValues("000000"
        + "000000"
        + "005000"
        + "000000"
        + "000000"
        + "000000", 1);

    GreedyAnalysis ga = new GreedyAnalysis(game);
    Assert.assertEquals(HeroAction.LEFT_STONE, ga.findBestAction());
  }

  @Test
  public void shitAndEatValue3() {
    newGame("☼☼☼☼☼☼"
        + "☼  ●®☼"
        + " ╘══►☼"
        + "☼    ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    hero.up();
    game.tick();
    hero.left();
    game.tick();

    assertValues("000000"
        + "000000"
        + "000500"
        + "000000"
        + "000000"
        + "000000", 1);

    GreedyAnalysis ga = new GreedyAnalysis(game);
    Assert.assertEquals(HeroAction.DOWN_STONE, ga.findBestAction());
  }

  // @formatter:on
}