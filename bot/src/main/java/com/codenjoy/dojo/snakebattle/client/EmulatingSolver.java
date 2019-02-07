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
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.PriorityQueue;
import java.util.Random;

public class EmulatingSolver implements Solver<Board> {
  private final Random random = new Random();
  private Point myHead;
  private SnakeBoard game;
  private boolean[][] deadEnd;

  @Override
  public String get(Board boardFromServer) {
    game = GameHelper.getNewOrContinuedGame(game, boardFromServer);
    if (boardFromServer.isNewRound() || deadEnd == null) {
      deadEnd = boardFromServer.getStaticDeadEnds();
    }

    return findBestMove(boardFromServer);
  }

  private String findBestMove(Board board) {
    return "LEFT";
  }



  private Hero getMyHero() {
    return game.getHeroes().get(0);
  }

  private Point getMyHead() {
    return getMyHero().head();
  }

  private String setExpectationsAndReturn(Direction direction, boolean leaveStone) {
    myHead = game.getHeroes().get(0).head().copy();
    myHead.change(direction);
    return direction.toString();
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
}
