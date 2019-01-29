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
 * You trivialCases have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.Direction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertEquals;

public class SnakeTest {
    @Rule()
    public Timeout globalTimeout = Timeout.millis(400);

    private Board board(String board) {
        return (Board) new MyBoard().forString(board);
    }

    @Test
    public void mySnakeTail() {
        assertSnake("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼    ●☼" +
                "☼☼   ˄ ☼" +
                "☼☼ ×─┘▲☼" +
                "☼☼╘═══╝☼" +
                "☼☼☼☼☼☼☼☼", 2, 1, new Snake(true, 6, Direction.UP, false));
    }

    @Test
    public void mySnakeBody() {
        assertSnake("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼    ●☼" +
                "☼☼   ˄ ☼" +
                "☼☼ ×─┘▲☼" +
                "☼☼╘═══╝☼" +
                "☼☼☼☼☼☼☼☼", 5, 1, new Snake(true, 6, Direction.UP, false));
    }

    @Test
    public void mySnakeHead() {
        assertSnake("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼    ●☼" +
                "☼☼   ˄ ☼" +
                "☼☼ ×─┘▲☼" +
                "☼☼╘═══╝☼" +
                "☼☼☼☼☼☼☼☼", 6, 2, new Snake(true, 6, Direction.UP, false));
    }

    @Test
    public void enemySnakeTail() {
        assertSnake("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼    ●☼" +
                "☼☼   ┌>☼" +
                "☼☼ ×─┘▲☼" +
                "☼☼╘═══╝☼" +
                "☼☼☼☼☼☼☼☼", 3, 2, new Snake(false, 5, Direction.RIGHT, false));
    }

    @Test
    public void enemySnakeBody() {
        assertSnake("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼    ●☼" +
                "☼☼   ┌>☼" +
                "☼☼ ×─┘▲☼" +
                "☼☼╘═══╝☼" +
                "☼☼☼☼☼☼☼☼", 4, 2, new Snake(false, 5, Direction.RIGHT, false));
    }

    @Test
    public void enemySnakeHead() {
        assertSnake("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼    ●☼" +
                "☼☼   ┌>☼" +
                "☼☼ ×─┘▲☼" +
                "☼☼╘═══╝☼" +
                "☼☼☼☼☼☼☼☼", 6, 3, new Snake(false, 5, Direction.RIGHT, false));
    }

    private void assertSnake(String board, int x, int y, Snake expected) {
        Snake actual = Snake.identify(x, y, board(board));
        assertEquals(expected, actual);
    }
}
