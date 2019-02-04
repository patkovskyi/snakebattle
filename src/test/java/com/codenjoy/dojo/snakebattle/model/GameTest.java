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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.client.Board;
import com.codenjoy.dojo.snakebattle.client.Game;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class GameTest {

  @Rule()
  public TestRule globalTimeout = new DisableOnDebug(new Timeout(500, TimeUnit.MILLISECONDS));

  private Game game;

  @Before
  public void setup() {
    game = new Game();
  }

  private void tick(String board) {
    game.updateFromBoard(new Board().forString(board));
  }

  @Test
  public void smallTest() {
    tick("☼☼☼☼☼☼☼☼"
        + "☼☼     ☼"
        + "☼☼☼☼   ☼"
        + "☼☼    ●☼"
        + "☼☼   ˄ ☼"
        + "☼☼ ×─┘▲☼"
        + "☼☼╘═══╝☼"
        + "☼☼☼☼☼☼☼☼");

    assertTrue(game.isAlive());
    assertFalse(game.isRoundLost());
    assertEquals(new PointImpl(6, 2), game.getMyHead());
    assertEquals(new PointImpl(2, 1), game.getMyBoardSnake().tail());
    assertEquals(1, game.getStones().size());
    assertTrue(game.getStones().contains(new PointImpl(6, 4)));
    assertEquals(1, game.getEnemyBoardSnakes().size());
    assertEquals(new PointImpl(5, 3), game.getEnemyBoardSnakes().stream().findFirst().get().head());
    assertEquals(new PointImpl(3, 2), game.getEnemyBoardSnakes().stream().findFirst().get().tail());
  }
}
