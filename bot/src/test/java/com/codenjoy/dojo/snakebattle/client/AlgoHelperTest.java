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

public class AlgoHelperTest {

  @Rule
  public TestRule globalTimeout = new DisableOnDebug(new Timeout(500, TimeUnit.MILLISECONDS));
  private SnakeBoard game;
  private Hero hero;

  private void newGame(String boardAsString) {
    game = GameHelper.initializeGame(new Board().forString(boardAsString));
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
          actual.append("*");
        } else {
          actual.append(arr[x][y]);
        }
      }

      actual.append("\n");
    }

    Assert.assertEquals(TestUtils.injectN(expected), actual.toString());
  }

  private void assertDynamicDistances(String expected) {
    int[][] arr = Algorithms.findDynamicDistances(game, hero);
    assertEqual(expected, arr);
  }

  private void assertStaticDistances(String expected) {
    int[][] arr = Algorithms.findStaticDistances(game, hero);
    assertEqual(expected, arr);
  }

  @Test
  public void staticDistanceTrivial() {
    newGame("☼☼☼☼☼"
        + "☼   ☼"
        + "╘►  ☼"
        + "☼   ☼"
        + "☼☼☼☼☼");

    assertStaticDistances("*****"
        + "*123*"
        + "*012*"
        + "*123*"
        + "*****");
  }

  @Test
  public void staticDistanceSimpleObstacle() {
    newGame("☼☼☼☼☼"
        + "☼   ☼"
        + "╘►☼ ☼"
        + "☼   ☼"
        + "☼☼☼☼☼");

    assertStaticDistances("*****"
        + "*123*"
        + "*0*4*"
        + "*123*"
        + "*****");
  }

  @Test
  public void dynamicDistanceEnemyOutOfReach() {
    newGame("☼☼☼☼☼"
        + "☼  ˄☼"
        + "╘► │☼"
        + "☼  ¤☼"
        + "☼☼☼☼☼");

    assertDynamicDistances("*****"
        + "*123*"
        + "*012*"
        + "*123*"
        + "*****");
  }

  @Test
  public void dynamicDistanceEnemyWithinReach() {
    newGame("☼☼☼☼☼"
        + "☼ ˄ ☼"
        + "╘►│ ☼"
        + "☼ ¤ ☼"
        + "☼☼☼☼☼");

    assertDynamicDistances("*****"
        + "*1*5*"
        + "*0*4*"
        + "*123*"
        + "*****");
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
    assertDynamicDistances("******"
        + "*2123*"
        + "*30*4*"
        + "*2123*"
        + "*3234*"
        + "******");
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

    assertDynamicDistances("******"
        + "*2123*"
        + "*3012*"
        + "*2123*"
        + "*3234*"
        + "******");
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
    assertDynamicDistances("******"
        + "*2123*"
        + "*3012*"
        + "*2123*"
        + "*3234*"
        + "******");
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

    assertDynamicDistances("******"
        + "*30*4*"
        + "*2123*"
        + "*3234*"
        + "*4345*"
        + "******");
  }
}