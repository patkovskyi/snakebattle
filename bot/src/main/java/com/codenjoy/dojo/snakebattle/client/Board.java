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

import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import java.util.Collection;
import lombok.Getter;

/**
 * This class simply captures string representation of the game board. It does not make any
 * assumptions and is not stateful (does not track previous states in any way).
 */
public class Board extends AbstractBoard<Elements> {

  @Getter
  private Collection<Point> gold;

  @Getter
  private Collection<Point> apples;

  @Getter
  private Collection<Point> stones;

  @Getter
  private Collection<Point> furyPills;

  @Getter
  private Collection<Point> flyingPills;

  @Getter
  private Collection<Point> enemySnakeHeads;

  @Override
  public Elements valueOf(char ch) {
    return Elements.valueOf(ch);
  }

  @Override
  protected int inversionY(int y) {
    return size - 1 - y;
  }

  @Override
  public Board forString(String boardString) {
    super.forString(boardString);

    gold = get(Elements.GOLD);
    apples = get(Elements.APPLE);
    stones = get(Elements.STONE);
    furyPills = get(Elements.FURY_PILL);
    flyingPills = get(Elements.FLYING_PILL);
    enemySnakeHeads = get(Elements.ENEMY_HEAD.toArray(new Elements[0]));
    return this;
  }

  public boolean containsPoint(Point p) {
    return !p.isOutOf(size);
  }

  /**
   * Best to run once when round starts to avoid confusion with dead heads.
   */
  public boolean[][] getStaticDeadEnds() {
    boolean[][] deadEnds = new boolean[size()][size()];
    for (int x = 0; x < size(); x++) {
      for (int y = 0; y < size(); y++) {
        deadEnds[x][y] = getAt(x, y).isStaticBarrier();
      }
    }

    boolean updated;
    Direction[] directions = Direction.onlyDirections().toArray(new Direction[0]);

    do {
      updated = false;
      for (int x = 0; x < size; x++) {
        for (int y = 0; y < size; y++) {
          if (!deadEnds[x][y]) {
            int passableNeighbors = 0;
            for (int d = 0; d < 4; d++) {
              int nx = directions[d].changeX(x);
              int ny = directions[d].changeY(y);
              if (!this.isOutOfField(nx, ny) && !deadEnds[nx][ny]) {
                ++passableNeighbors;
              }
            }

            if (passableNeighbors <= 1) {
              deadEnds[x][y] = true;
              updated = true;
            }
          }
        }
      }
    } while (updated);

    return deadEnds;
  }
}
