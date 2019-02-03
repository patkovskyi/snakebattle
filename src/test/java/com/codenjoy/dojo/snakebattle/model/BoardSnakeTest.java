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
import static org.junit.Assert.assertNull;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.client.Board;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class BoardSnakeTest {

  @Rule()
  public TestRule globalTimeout = new DisableOnDebug(new Timeout(500, TimeUnit.MILLISECONDS));

  private Board getBoard(String board) {
    return new Board().forString(board);
  }

  private BoardSnake getSnake(String board, int x, int y) {
    return BoardSnake.identify(new PointImpl(x, y), getBoard(board));
  }

  @Test
  public void mySnakeFull() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼╘═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        6, 2);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.UP, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(6, snake.size());
    assertEquals(new PointImpl(6, 2), snake.head());
    assertEquals(new PointImpl(2, 1), snake.tail());
  }

  @Test
  public void mySnakeShort() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘ ☼"
            + "☼☼  ◄╕ ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 1);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.LEFT, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(2, snake.size());
    assertEquals(new PointImpl(4, 1), snake.head());
    assertEquals(new PointImpl(5, 1), snake.tail());
  }

  @Test
  public void mySnakeAbrupt() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ˄ ☼"
            + "☼☼ ×─┘▲☼"
            + "☼☼ ═══╝☼"
            + "☼☼☼☼☼☼☼☼",
        6, 2);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.UP, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(5, snake.size());
    assertEquals(new PointImpl(6, 2), snake.head());
    assertEquals(new PointImpl(3, 1), snake.tail());
  }

  @Test
  public void mySnakeJustHead() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼  ▼˄ ☼"
            + "☼☼ ×─┘ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.DOWN, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(1, snake.size());
    assertEquals(new PointImpl(4, 3), snake.head());
    assertEquals(new PointImpl(4, 3), snake.tail());
  }

  @Test
  public void mySnakeJustFuryJustHead() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼  ♥˄ ☼"
            + "☼☼ ×─┘ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertNull(snake);
  }

  @Test
  public void mySnakeFuryDirectionFull() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼╘═♥˄ ☼"
            + "☼☼ ×─┘ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.RIGHT, snake.getDirection());
    assertEquals(true, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(3, snake.size());
    assertEquals(new PointImpl(4, 3), snake.head());
    assertEquals(new PointImpl(2, 3), snake.tail());
  }

  @Test
  public void mySnakeFuryDirectionBody() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼ ═♥˄ ☼"
            + "☼☼ ×─┘ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.RIGHT, snake.getDirection());
    assertEquals(true, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(2, snake.size());
    assertEquals(new PointImpl(4, 3), snake.head());
    assertEquals(new PointImpl(3, 3), snake.tail());
  }

  @Test
  public void mySnakeFuryDirectionTail() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼ ♥╕˄ ☼"
            + "☼☼ ×─┘ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        3, 3);

    assertEquals(true, snake.isMe());
    assertEquals(Direction.LEFT, snake.getDirection());
    assertEquals(true, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(2, snake.size());
    assertEquals(new PointImpl(3, 3), snake.head());
    assertEquals(new PointImpl(4, 3), snake.tail());
  }


  @Test
  public void enemySnakeFull() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ▲ ☼"
            + "☼☼ ╘═╝˄☼"
            + "☼☼×───┘☼"
            + "☼☼☼☼☼☼☼☼",
        6, 2);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.UP, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(6, snake.size());
    assertEquals(new PointImpl(6, 2), snake.head());
    assertEquals(new PointImpl(2, 1), snake.tail());
  }

  @Test
  public void enemySnakeShort() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ▲ ☼"
            + "☼☼ ╘═╝ ☼"
            + "☼☼  <ö ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 1);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.LEFT, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(2, snake.size());
    assertEquals(new PointImpl(4, 1), snake.head());
    assertEquals(new PointImpl(5, 1), snake.tail());
  }

  @Test
  public void enemySnakeAbrupt() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼   ▲ ☼"
            + "☼☼ ╘═╝˄☼"
            + "☼☼ ───┘☼"
            + "☼☼☼☼☼☼☼☼",
        6, 2);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.UP, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(5, snake.size());
    assertEquals(new PointImpl(6, 2), snake.head());
    assertEquals(new PointImpl(3, 1), snake.tail());
  }

  @Test
  public void enemySnakeJustHead() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼  ˅▲ ☼"
            + "☼☼ ╘═╝ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.DOWN, snake.getDirection());
    assertEquals(false, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(1, snake.size());
    assertEquals(new PointImpl(4, 3), snake.head());
    assertEquals(new PointImpl(4, 3), snake.tail());
  }

  @Test
  public void enemySnakeJustFuryJustHead() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼  ♣▲ ☼"
            + "☼☼ ╘═╝ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertNull(snake);
  }

  @Test
  public void enemySnakeFuryDirectionFull() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼×─♣▲ ☼"
            + "☼☼ ╘═╝ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.RIGHT, snake.getDirection());
    assertEquals(true, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(3, snake.size());
    assertEquals(new PointImpl(4, 3), snake.head());
    assertEquals(new PointImpl(2, 3), snake.tail());
  }

  @Test
  public void enemySnakeFuryDirectionBody() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼ ─♣▲ ☼"
            + "☼☼ ╘═╝ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        4, 3);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.RIGHT, snake.getDirection());
    assertEquals(true, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(2, snake.size());
    assertEquals(new PointImpl(4, 3), snake.head());
    assertEquals(new PointImpl(3, 3), snake.tail());
  }

  @Test
  public void enemySnakeFuryDirectionTail() {
    BoardSnake snake = getSnake(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼    ●☼"
            + "☼☼ ♣ö▲ ☼"
            + "☼☼ ╘═╝ ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        3, 3);

    assertEquals(false, snake.isMe());
    assertEquals(Direction.LEFT, snake.getDirection());
    assertEquals(true, snake.isFurious());
    assertEquals(false, snake.isFlying());
    assertEquals(2, snake.size());
    assertEquals(new PointImpl(3, 3), snake.head());
    assertEquals(new PointImpl(4, 3), snake.tail());
  }
}
