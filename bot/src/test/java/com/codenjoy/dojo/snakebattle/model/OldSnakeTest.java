package com.codenjoy.dojo.snakebattle.model;

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
 * You trivialCases have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.snakebattle.client.ClosestBestBoard;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class OldSnakeTest {
  @Rule()
  public TestRule globalTimeout = new DisableOnDebug(new Timeout(500, TimeUnit.MILLISECONDS));

  private ClosestBestBoard board(String board) {
    return (ClosestBestBoard) new ClosestBestBoard().forString(board);
  }

  @Test
  public void mySnakeTail() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        2,
        1,
        new OldSnake(true, 6, Direction.UP, false));
  }

  @Test
  public void mySnakeBody() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        5,
        1,
        new OldSnake(true, 6, Direction.UP, false));
  }

  @Test
  public void mySnakeHead() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        6,
        2,
        new OldSnake(true, 6, Direction.UP, false));
  }

  @Test
  public void enemySnakeTail() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ┌>☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        3,
        2,
        new OldSnake(false, 5, Direction.RIGHT, false));
  }

  @Test
  public void enemySnakeBody() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ┌>☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        4,
        2,
        new OldSnake(false, 5, Direction.RIGHT, false));
  }

  @Test
  public void enemySnakeHead() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ┌>☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        6,
        3,
        new OldSnake(false, 5, Direction.RIGHT, false));
  }

  @Test
  // TODO: remove this test, it's for a hack to avoid snakes without tails
  public void enemySnakeJustHead() {
    assertSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼    >☼"
            + "☼☼    ▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        6,
        3,
        new OldSnake(false, 1, Direction.RIGHT, false));
  }

  private void assertSnake(String board, int x, int y, OldSnake expected) {
    OldSnake actual = OldSnake.identify(x, y, board(board));
    assertEquals(expected, actual);
  }
}
