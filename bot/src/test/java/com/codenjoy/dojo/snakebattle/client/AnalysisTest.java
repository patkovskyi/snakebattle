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
import org.junit.Ignore;
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

  private void print(int[][] arr) {
    for (int y = arr.length; y --> 0; ) {
      for (int x = 0; x < arr.length; x++) {
        System.out.printf("%d ", arr[x][y]);
      }

      System.out.println();
    }
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
    int[][] arr = new Analysis(game).getDynamicDistances(hero);
    assertEqual(expected, arr);
  }

  private void assertStaticDistances(String expected) {
    int[][] arr = new Analysis(game).getStaticDistances(hero);
    assertEqual(expected, arr);
  }

  private void assertValues(String expected) {
    assertValues(expected, 1);
  }

  private void assertValues(String expected, int padding) {
    int[][] acc = new Analysis(game).getValues(hero);
    assertWithPadding(expected, acc, padding);
  }

  private void assertAccumulatedValues(String expected) {
    int[][] acc = new Analysis(game).getAccumulatedValues(hero);
    assertWithPadding(expected, acc, 2);
  }

  private void assertDeadEnds(String board, String expectedDeadEnds) {
    SnakeBoard game = GameHelper.initializeGame(new MyBoard().forString(board));
    Analysis analysis = new Analysis(game);
    boolean[][] deadEnds = analysis.getStaticObstacles(game.getHeroes().get(0));
    StringBuilder sb = new StringBuilder();
    for (int y = deadEnds.length; y-- > 0; ) {
      for (int x = 0; x < deadEnds.length; x++) {
        sb.append(deadEnds[x][y] ? "☼" : " ");
      }
    }

    TestUtils.assertBoardsEqual(expectedDeadEnds, sb.toString());
  }

  private void assertMove(HeroAction expected) {
    Analysis ga = new Analysis(game);
    HeroAction actual = ga.findBestAction();
    Assert.assertEquals(expected, actual);
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
        + "☼12☼☼"
        + "☼01☼☼"
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
        + "☼3012☼"
        + "☼2123☼"
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
        + "☼12☼☼"
        + "☼01☼☼"
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

    assertDynamicDistances(
        "☼☼☼☼☼"
      + "☼☼☼☼☼"
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
        + "☼21☼5☼"
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

    assertDynamicDistances(
          "☼☼☼☼☼☼☼☼"
        + "☼212☼☼☼☼"
        + "☼30☼☼☼☼☼"
        + "☼4☼☼☼☼☼☼"
        + "☼567☼☼☼☼"
        + "☼678☼☼☼☼"
        + "☼789☼☼☼☼"
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

    assertDynamicDistances(
        "☼☼☼☼☼☼☼☼"
      + "☼212☼☼☼☼"
      + "☼30☼☼☼☼☼"
      + "☼4☼☼☼☼☼☼"
      + "☼567☼☼☼☼"
      + "☼678☼☼☼☼"
      + "☼789☼☼☼☼"
      + "☼☼☼☼☼☼☼☼"
    );
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

    assertDynamicDistances(
          "☼☼☼☼☼☼☼☼"
        + "☼212☼45☼"
        + "☼30☼☼34☼"
        + "☼4☼☼☼23☼"
        + "☼567☼12☼"
        + "☼678901☼"
        + "☼789☼12☼"
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
            + "0 0 0 1 # 0 "
            + "0 0 0 0 0 0 "
            + "0 0 # 0 150 "
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
              "# # # # # # "
            + "# 0 0 1010# "
            + "# 0 0 11# # "
            + "# 0 0 1111# "
            + "# 0 # 1126# "
            + "# # # # # # ");
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
              "# # # # # # "
            + "# 0 707070# "
            + "# 70707070# "
            + "# 70707070# "
            + "# 70707070# "
            + "# # # # # # ");
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
              "# # # # # # "
            + "# 0 # # # # "
            + "# # # # # # "
            + "# # # # # # "
            + "# # # # # # "
            + "# # # # # # ");
  }

  @Test
  public void AnalysisTest() {
    newGame("☼☼☼☼☼☼"
        + " ▲   ☼"
        + "☼║  ○☼"
        + "☼║   ☼"
        + "☼╙   ☼"
        + "☼☼☼☼☼☼");

    Analysis a = new Analysis(game);
    assertEquals(HeroAction.RIGHT, a.findBestAction());
  }

  @Test
  public void AnalysisTestBetterPath1() {
    newGame(
          "☼☼☼☼☼☼"
        + "☼ ●●●☼"
        + "☼ ○ ®☼"
        + "☼╘►○○☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");


    Analysis a = new Analysis(game);
    assertEquals(HeroAction.RIGHT, a.findBestAction());
  }

  @Test
  public void AnalysisTestBetterPath2() {
    newGame(
          "☼☼☼☼☼☼"
        + "☼ ●●●☼"
        + "☼ ○○®☼"
        + "☼╘► ○☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");


    Analysis a = new Analysis(game);
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

    Analysis ga = new Analysis(game);
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

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.DOWN_STONE, ga.findBestAction());
  }

  @Test
  public void dontCrossYourself() {
    newGame("☼☼☼☼☼☼"
        + "☼  ● ☼"
        + "╘═══►☼"
        + "☼   ®☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    hero.down();
    game.tick();
    hero.left();
    game.tick();

    assertH("☼☼☼☼☼☼"
        + "☼  ● ☼"
        + "  ╘═╗☼"
        + "☼  ♥╝☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertNotEquals(HeroAction.UP, ga.findBestAction());
  }

  @Test
  public void selfDefense1() {
    newGame(
          "☼☼☼☼☼☼"
        + "╘►®˄ ☼"
        + "☼  │ ☼"
        + "☼ ○│ ☼"
        + "☼  ¤ ☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.DOWN, ga.findBestAction());
  }

  @Test
  public void selfDefense2() {
    newGame(
          "☼☼☼☼☼☼"
        + "╘►○ ˄☼"
        + "☼   │☼"
        + "☼   │☼"
        + "☼   ¤☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.DOWN, ga.findBestAction());
  }

  @Test
  public void selfDefense3() {
    newGame(
          "☼☼☼☼☼☼"
        + "╘►○˄ ☼"
        + "☼  │ ☼"
        + "☼  ¤ ☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.DOWN, ga.findBestAction());
  }

  @Test
  public void selfDefense4() {
    newGame(
          "☼☼☼☼☼☼"
        + "╘►○ ˄☼"
        + "☼   │☼"
        + "☼   ¤☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.RIGHT, ga.findBestAction());
  }

  @Test
  public void selfDefense5() {
    newGame(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
      + "☼☼                           ☼"
      + "☼#                           ☼"
      + "☼☼       ●            ●      ☼"
      + "☼☼                           ☼"
      + "☼☼           ●         ┌ö    ☼"
      + "☼☼     ☼☼☼☼☼     ┌─────┘    ○☼"
      + "☼☼     ☼        ┌┘           ☼"
      + "☼#     ☼☼☼     ┌┘ ☼☼☼☼#      ☼"
      + "☼☼     ☼       │  ☼   ☼  ●   ☼"
      + "☼☼     ☼☼☼☼# ┌─┘  ☼☼☼☼#      ☼"
      + "☼☼           │    ☼          ☼"
      + "☼☼           │    ☼          ☼"
      + "☼☼    ●      ˅               ☼"
      + "☼#                           ☼"
      + "☼☼╔════════►                 ☼"
      + "☼☼║       ☼☼☼  ○             ☼"
      + "☼☼║      ☼  ☼                ☼"
      + "☼☼║     ☼☼☼☼#     ☼☼   ☼#    ☼"
      + "☼☼╚═╕   ☼   ☼  ○● ☼ ☼ ☼ ☼    ☼"
      + "☼#      ☼   ☼     ☼○ ☼ ○☼    ☼"
      + "☼☼                ☼     ☼    ☼"
      + "☼☼    ©●          ☼     ☼    ☼"
      + "☼☼                           ☼"
      + "☼☼                           ☼"
      + "☼☼                           ☼"
      + "☼#                ○        ○ ☼"
      + "☼☼                           ☼"
      + "☼☼             ○             ☼"
      + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.UP, ga.findBestAction());
  }

  @Ignore
  @Test
  public void takeEnemyWithMe() {
    newGame(  "☼☼☼☼☼☼"
            + "☼▲˄  ☼"
            + "☼║│  ☼"
            + "☼║¤  ☼"
            + "☼╙   ☼"
            + "☼☼☼☼☼☼");

    assertAccumulatedValues(
              "# # # # # # "
            + "# 0 # # # # "
            + "# # # # # # "
            + "# # # # # # "
            + "# # # # # # "
            + "# # # # # # ");

    assertMove(HeroAction.RIGHT);
  }

  @Test
  public void avoidStoneDrop1() {
    newGame(
          "☼☼☼☼☼☼"
        + "☼  ˄ ☼"
        + "☼  │ ☼"
        + "☼╘►¤○☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.DOWN, ga.findBestAction());
  }

  @Test
  public void avoidStoneDrop2() {
    newGame(
          "☼☼☼☼☼☼"
        + "☼  ˄ ☼"
        + "☼  │ ☼"
        + " ╘♥¤○☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    hero.setFuryCount(2);

    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.RIGHT, ga.findBestAction());
  }

  @Test
  public void intercept1() {
    newGame(
          "☼☼☼☼☼☼"
        + "╘══►○☼"
        + "☼    ☼"
        + "☼ ˄  ☼"
        + "☼ ¤  ☼"
        + "☼☼☼☼☼☼");

    assertMove(HeroAction.DOWN);
  }

  @Test
  public void intercept2tooWeak() {
    newGame(
          "☼☼☼☼☼☼"
        + " ╘═►○☼"
        + "☼    ☼"
        + "☼ ˄  ☼"
        + "☼ ¤  ☼"
        + "☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertEquals(values[2][3], 0);
    Assert.assertEquals(HeroAction.RIGHT, ga.findBestAction());
  }

  @Test
  public void intercept3() {
    newGame(
          "☼☼☼☼☼☼"
        + "╘♥   ☼"
        + "☼    ☼"
        + "☼○ ˄ ☼"
        + "☼  ¤ ☼"
        + "☼☼☼☼☼☼");

    hero.setFuryCount(3);
    assertMove(HeroAction.DOWN);
  }

  @Test
  public void avoidStonesInLateGame() {
    newGame(
          "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
        + "☼☼      ○                  ○ ☼"
        + "☼#              ○            ☼"
        + "☼☼                           ☼"
        + "☼☼  ○                        ☼"
        + "☼☼                           ☼"
        + "☼☼     ☼☼☼☼☼ $               ☼"
        + "☼☼     ☼                     ☼"
        + "☼#     ☼☼☼        ☼☼☼☼#      ☼"
        + "☼☼     ☼®   ●     ☼   ☼      ☼"
        + "☼☼     ☼☼☼☼#▲     ☼☼☼☼#      ☼"
        + "☼☼       ╔══╝     ☼          ☼"
        + "☼☼       ║        ☼          ☼"
        + "☼☼       ║                   ☼"
        + "☼#       ║     ©        ○   ○☼"
        + "☼☼       ║                   ☼"
        + "☼☼×┐   ╔═╝☼☼☼                ☼"
        + "☼☼ │   ║ ☼  ☼                ☼"
        + "☼☼ └──┐║☼☼☼☼#     ☼☼   ☼#    ☼"
        + "☼☼    │║☼   ☼     ☼ ☼ ☼ ☼    ☼"
        + "☼#    │╙☼   ☼     ☼  ☼  ☼    ☼"
        + "☼☼   ©│           ☼     ☼    ☼"
        + "☼☼    └┐          ☼     ☼    ☼"
        + "☼☼     │                 ○   ☼"
        + "☼☼     └──>                  ☼"
        + "☼☼                           ☼"
        + "☼#                           ☼"
        + "☼☼                      ○    ☼"
        + "☼☼                           ☼"
        + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼");

    GameHelper.setTick(250);

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    int[][] acc = ga.getAccumulatedValues(hero);
    Assert.assertEquals(Mechanics.SOMEWHAT_NEGATIVE, values[12][20]);
    Assert.assertEquals(Mechanics.SOMEWHAT_NEGATIVE, acc[12][20]);
    Assert.assertEquals(Mechanics.SOMEWHAT_NEGATIVE, acc[12][21]);
    Assert.assertEquals(Mechanics.SOMEWHAT_NEGATIVE, acc[12][23]);
    Assert.assertEquals(HeroAction.RIGHT, ga.findBestAction());
  }

  @Test
  public void appleHighValueWhenShort() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼   ☼  ☼"
        + "☼╘► ☼  ☼"
        + "☼    ˄ ☼"
        + "☼   ☼¤ ☼"
        + "☼   ☼  ☼"
        + "☼○  ☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertEquals(11, values[1][1]);
  }

  @Test
  public void appleHighValueWhenLateGame() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼ ○ ☼  ☼"
        + "☼╘► ☼  ☼"
        + "☼  ˄   ☼"
        + "☼  ¤☼  ☼"
        + "☼   ☼  ☼"
        + "☼   ☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    GameHelper.setTick(220);
    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertEquals(21, values[2][6]);
  }

  @Test
  public void doPutValueOnAppleIfWereCloserEvenByOne() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼     ○☼"
        + "☼╘►    ☼"
        + "☼  ○   ☼"
        + "☼   ☼  ☼"
        + "☼   ☼˄ ☼"
        + "☼   ☼¤ ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertTrue(values[3][4] > 0);
    assertMove(HeroAction.DOWN);
  }

  @Test
  public void dontPutValueOnAppleIfItsEnemiesClosest() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼     ○☼"
        + "☼╘►    ☼"
        + "☼      ☼"
        + "☼  ○☼  ☼"
        + "☼  ˄☼  ☼"
        + "☼  ¤☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertEquals(0, values[3][3]);
    assertMove(HeroAction.UP);
  }

  @Test
  public void dontPutValueOnGoldIfItsEnemiesClosestAndWeCantBeat1() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼$     ☼"
        + "☼╘►    ☼"
        + "☼   $  ☼"
        + "☼   ☼  ☼"
        + "☼   ☼˄ ☼"
        + "☼   ☼¤ ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    int[][] accValues = ga.getAccumulatedValues(hero);
    int[][] distances = ga.getDynamicDistances(hero);
    Assert.assertEquals(0, values[4][4]);

    print(values);
    print(accValues);
    print(distances);
    assertMove(HeroAction.UP);
  }

  @Test
  public void dontPutValueOnGoldIfItsEnemiesClosestAndWeCantBeat2() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼$     ☼"
        + "☼╘♥    ☼"
        + "☼   $  ☼"
        + "☼   ☼  ☼"
        + "☼   ☼˄ ☼"
        + "☼   ☼¤ ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    hero.setFuryCount(3);
    int[][] values = ga.getValues(hero);
    Assert.assertEquals(0, values[4][4]);
    assertMove(HeroAction.UP);
  }

  @Test
  public void dontPutValueOnGoldIfWereCloserByOneButEnemyCanKill() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼$     ☼"
        + "☼╘►    ☼"
        + "☼  $   ☼"
        + "☼   ☼˄æ☼"
        + "☼   ☼└┘☼"
        + "☼   ☼  ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertEquals(0, values[3][4]);
    assertMove(HeroAction.UP);
  }

  @Test
  public void putDoubleValueIfWeReachThePointSameMomentAndCanBeat() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼$     ☼"
        + "☼╘♥    ☼"
        + "☼   $  ☼"
        + "☼   ☼  ☼"
        + "☼   ☼˄ ☼"
        + "☼   ☼¤ ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    hero.setFuryCount(4);
    int[][] values = ga.getValues(hero);
    Assert.assertTrue(values[4][4] >= 2 * Mechanics.GOLD_REWARD);
    assertMove(HeroAction.DOWN);
  }

  @Test
  public void putDoubleValueIfWeReachThePointSameMomentAndCanBeat2() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼$     ☼"
        + "☼╔►    ☼"
        + "☼║  $  ☼"
        + "☼╙  ☼  ☼"
        + "☼   ☼˄ ☼"
        + "☼   ☼¤ ☼"
        + "☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    int[][] values = ga.getValues(hero);
    Assert.assertTrue(values[4][4] >= 2 * Mechanics.GOLD_REWARD);
    assertMove(HeroAction.DOWN);
  }

  @Test
  public void strangeDeadend() {
    newGame("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
          + "☼☼              ©            ☼"
          + "☼#○                          ☼"
          + "☼☼       ●                   ☼"
          + "☼☼                           ☼"
          + "☼☼           ●         ┌────┐☼"
          + "☼☼     ☼☼☼☼☼  ●  ○     │    │☼"
          + "☼☼     ☼         ●     │    │☼"
          + "☼#     ☼☼☼        ☼☼☼☼#│   ┌┘☼"
          + "☼☼     ☼ ○        ☼   ☼│ ● │ ☼"
          + "☼☼  ○  ☼☼☼☼#      ☼☼☼☼#│   │ ☼"
          + "☼☼                ☼  ×─┘   └┐☼"
          + "☼☼              ○ ☼        ┌┘☼"
          + "☼☼    ●®                  ♦┘ ☼"
          + "☼#○             ○            ☼"
          + "☼☼                      ●    ☼"
          + "☼☼        ☼☼☼                ☼"
          + "☼☼      ○☼  ☼                ☼"
          + "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼"
          + "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼    ☼"
          + "☼#      ☼   ☼     ☼  ☼  ☼    ☼"
          + "☼☼        ●  ○    ☼     ☼    ☼"
          + "☼☼                ☼○    ☼  ╓ ☼"
          + "☼☼       ○     ╔═══►╔══╗   ╚╗☼"
          + "☼☼             ║   ╔╝  ╚════╝☼"
          + "☼☼  ○          ╚═══╝         ☼"
          + "☼# ○                         ☼"
          + "☼☼   ●                       ☼"
          + "☼☼                          ●☼"
          + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼");

    Analysis ga = new Analysis(game);
    double[][] accvalues = ga.getAccumulatedDistanceAdjustedValues(hero);
    Assert.assertEquals(11, accvalues[19][7], 0.1);
    assertMove(HeroAction.UP);
  }

  @Test
  public void avoidFuryHeads() {
    newGame("☼☼☼☼☼☼☼☼"
        + "☼ ○    ☼"
        + "☼      ☼"
        + "☼╘►  $ ☼"
        + "☼   $○ ☼"
        + "☼   $ ♣☼"
        + "☼   $ ¤☼"
        + "☼☼☼☼☼☼☼☼");

    game.getHeroes().get(1).setFuryCount(4);
    assertMove(HeroAction.UP);
  }

  @Test
  public void sometimesBetterEatYourself() {
    newGame("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
          + "☼☼                           ☼"
          + "☼#      ©                    ☼"
          + "☼☼                  ○        ☼"
          + "☼☼                           ☼"
          + "☼☼        ○  ╓╔═╗            ☼"
          + "☼☼     ☼☼☼☼☼ ║║ ║            ☼"
          + "☼☼     ☼○ ▲╔╗║║ ║            ☼"
          + "☼#     ☼☼☼║║║╚╝ ║ ☼☼☼☼#      ☼"
          + "☼☼   æ ☼  ╚╝╚═══╝ ☼  ®☼  ●   ☼"
          + "☼☼   │ ☼☼☼☼#     ©☼☼☼☼#    ○ ☼"
          + "☼☼   │            ☼         ○☼"
          + "☼☼   │            ☼          ☼"
          + "☼☼   │      ○                ☼"
          + "☼#   │●                  ●   ☼"
          + "☼☼   │●                      ☼"
          + "☼☼   │    ☼☼☼                ☼"
          + "☼☼   │   ☼  ☼                ☼"
          + "☼☼   │  ☼☼☼☼#     ☼☼ ○ ☼#    ☼"
          + "☼☼   │  ☼   ☼     ☼ ☼○☼ ☼    ☼"
          + "☼#   │  ☼   ☼     ☼  ☼  ☼    ☼"
          + "☼☼   │     ●      ☼     ☼    ☼"
          + "☼☼   │     ●      ☼     ☼    ☼"
          + "☼☼   │                 ○     ☼"
          + "☼☼   │                   ○   ☼"
          + "☼☼   └─┐                     ☼"
          + "☼#    <┘                     ☼"
          + "☼☼   ○     $                 ☼"
          + "☼☼              $      ○     ☼"
          + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼");

    assertMove(HeroAction.RIGHT);
  }

  @Ignore
  @Test
  public void smartAvoidance() {
    newGame("☼☼☼☼☼☼"
        + "☼  ● ☼"
        + "  ╘═╗☼"
        + "☼  ♥╝☼"
        + "☼    ☼"
        + "☼☼☼☼☼☼");

    hero.setFuryCount(3);
    Analysis ga = new Analysis(game);
    Assert.assertEquals(HeroAction.LEFT, ga.findBestAction());
  }

  @Test
  public void smartIntercept() {
    newGame("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
          + "☼☼                           ☼"
          + "☼#                           ☼"
          + "☼☼       ●                   ☼"
          + "☼☼                           ☼"
          + "☼☼   ○       ●               ☼"
          + "☼☼     ☼☼☼☼☼                 ☼"
          + "☼☼     ☼                     ☼"
          + "☼#     ☼☼☼        ☼☼☼☼#      ☼"
          + "☼☼○    ☼          ☼   ☼      ☼"
          + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
          + "☼☼                ☼          ☼"
          + "☼☼                ☼          ☼"
          + "☼☼    ●                      ☼"
          + "☼#                           ☼"
          + "☼☼                           ☼"
          + "☼☼        ☼☼☼                ☼"
          + "☼☼       ☼$ ☼                ☼"
          + "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼"
          + "☼☼╔╗    ☼   ☼   ● ☼ ☼ ☼ ☼    ☼"
          + "☼#║╙    ☼   ☼     ☼  ☼  ☼    ☼"
          + "☼☼║               ☼ ●   ☼    ☼"
          + "☼☼║    ●   ○      ☼     ☼    ☼"
          + "☼☼║                          ☼"
          + "☼☼╚═►                        ☼"
          + "☼☼ ○              æ          ☼"
          + "☼#                │●  ©      ☼"
          + "☼☼            <───┘          ☼"
          + "☼☼                           ☼"
          + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼");

    Analysis a = new Analysis(game);
    Assert.assertEquals(120, a.getValues(hero)[11][5]);
    assertMove(HeroAction.RIGHT);
  }

  // @formatter:on
}