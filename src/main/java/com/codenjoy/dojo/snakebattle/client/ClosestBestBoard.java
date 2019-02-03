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
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static com.codenjoy.dojo.snakebattle.model.Elements.APPLE;
import static com.codenjoy.dojo.snakebattle.model.Elements.BODY_HORIZONTAL;
import static com.codenjoy.dojo.snakebattle.model.Elements.BODY_LEFT_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.BODY_LEFT_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.BODY_RIGHT_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.BODY_RIGHT_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.BODY_VERTICAL;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY_HORIZONTAL;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY_LEFT_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY_LEFT_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY_RIGHT_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY_RIGHT_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY_VERTICAL;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_EVIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_FLY;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_LEFT;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_RIGHT;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_SLEEP;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_TAIL_END_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_TAIL_END_LEFT;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_TAIL_END_RIGHT;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_TAIL_END_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_TAIL_INACTIVE;
import static com.codenjoy.dojo.snakebattle.model.Elements.GOLD;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_DOWN;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_EVIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_FLY;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_LEFT;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_RIGHT;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_SLEEP;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_UP;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_BODY;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_HEAD;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_SNAKE;
import static com.codenjoy.dojo.snakebattle.model.Elements.START_FLOOR;
import static com.codenjoy.dojo.snakebattle.model.Elements.STONE;
import static com.codenjoy.dojo.snakebattle.model.Elements.WALL;

import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.codenjoy.dojo.snakebattle.model.OldSnake;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class ClosestBestBoard extends AbstractBoard<Elements> {

  private static final int STONE_LENGTH_COST = 3;
  private static final int MIN_SNAKE_LENGTH = 2;
  private static final int FURY_LENGTH = 10;

  private int[][] lucrativeness;
  private Point myHead;
  private boolean stoneEatenLastRound;
  private int stoneCount = 0;
  private Direction headDirection = Direction.RIGHT;

  protected void refreshDirection() {
    if (!get(Elements.HEAD_SLEEP, Elements.HEAD_RIGHT).isEmpty()) {
      // in case of a new round also reset to right
      headDirection = Direction.RIGHT;
    } else if (!get(Elements.HEAD_DOWN).isEmpty()) {
      headDirection = Direction.DOWN;
    } else if (!get(Elements.HEAD_RIGHT).isEmpty()) {
      headDirection = Direction.RIGHT;
    } else if (!get(Elements.HEAD_LEFT).isEmpty()) {
      headDirection = Direction.LEFT;
    } else if (!get(Elements.HEAD_UP).isEmpty()) {
      headDirection = Direction.UP;
    }
  }

  protected void markDeadEnds() {
    // TODO: implement dynamic dead end prediction (probably should predict where enemy snake is
    // moving)
    boolean[][] nonPassable = new boolean[size][size];
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        nonPassable[x][y] =
            isBarrierAt(x, y)
                || isStoneAt(x, y)
                && !areWeFurious()
                && getMySnakeLength() - STONE_LENGTH_COST < MIN_SNAKE_LENGTH;
      }
    }

    boolean updated;
    Direction[] directions = Direction.onlyDirections().toArray(new Direction[0]);

    do {
      updated = false;
      for (int x = 0; x < size; x++) {
        for (int y = 0; y < size; y++) {
          if (!nonPassable[x][y]) {
            int passableNeighbors = 0;
            for (int d = 0; d < 4; d++) {
              int nx = directions[d].changeX(x);
              int ny = directions[d].changeY(y);
              if (isWithinBoard(nx, ny) && !nonPassable[nx][ny]) {
                ++passableNeighbors;
              }
            }

            if (passableNeighbors <= 1) {
              nonPassable[x][y] = true;
              updated = true;

              if (Elements.IMMEDIATELY_PASSABLE.contains(getAt(x, y))) {
                set(x, y, Elements.WALL.ch());
              }
            }
          }
        }
      }
    } while (updated);
  }

  public String getNextStep() {
    if (isGameOver()) {
      System.out.println("GAME OVER");
      return "";
    } else {
      refreshMyHead();
      refreshDirection();
      markDeadEnds();

      if (shitABrick()) {
        --stoneCount;
        headDirection = headDirection.clockwise();
        return "ACT, " + headDirection.toString();
      }

      int[][] distances = getDistances();
      Point closestPowerUp = getBestPowerUp(distances);
      Direction newDirection = getFirstStepNonDirectional(closestPowerUp, distances);
      if (newDirection.value() < 4) {
        headDirection = newDirection;
        refreshWhatsEaten(newDirection);
      }

      return headDirection.toString();
    }
  }

  private boolean shitABrick() {
    Point np = getMe();
    np.change(headDirection.clockwise());
    return stoneCount > 0 && !stoneEatenLastRound && canEatStone() && !isNotPassableOrRisky(np);
  }

  private boolean canEatStone() {
    return areWeFurious() || (getMySnakeLength() - STONE_LENGTH_COST >= MIN_SNAKE_LENGTH);
  }

  private void refreshWhatsEaten(Direction newDirection) {
    Point np = getMe();
    np.change(newDirection);
    if (getAt(np) == Elements.STONE) {
      stoneEatenLastRound = true;
      ++stoneCount;
    } else {
      stoneEatenLastRound = false;
    }
  }

  public Point getBestPowerUp(int[][] nondir) {
    List<Point> points =
        getMySnakeLength() - STONE_LENGTH_COST >= MIN_SNAKE_LENGTH
            ? this.get(Elements.STONE)
            : new ArrayList<>();
    if (points.isEmpty() && areWeFurious()) {
      points =
          this.get(Elements.STONE).stream()
              .filter(p -> distanceFromMe(p) < FURY_LENGTH)
              .collect(Collectors.toList());
    }

    if (points.isEmpty()) {
      points = this.get(Elements.GOLD, Elements.APPLE);
      if (points.isEmpty()) {
        points = this.get(Elements.FURY_PILL);
        if (points.isEmpty()) {
          points = this.get(Elements.FLYING_PILL);
          if (points.isEmpty()) {
            points = this.get(Elements.NONE);
          }
        }
      }
    }

    return points.stream().min(Comparator.comparingInt(p -> nondir[p.getX()][p.getY()])).get();
  }

  public Direction getFirstStepNonDirectional(Point p, int[][] nondir) {
    System.out.printf("getFirstStepNonDirectional search for %d %d\n", p.getX(), p.getY());
    if (nondir[p.getX()][p.getY()] == Integer.MAX_VALUE) {
      System.out.println("getFirstStepNonDirectional: path is BLOCKED");
      return Direction.ACT;
    }

    Point bestP = p;
    do {
      p = bestP;
      bestP = null;
      for (Direction d : Direction.onlyDirections()) {
        Point newP = p.copy();
        newP.change(d);

        if (nondir[newP.getX()][newP.getY()] == nondir[p.getX()][p.getY()] - 1) {
          if (nondir[newP.getX()][newP.getY()] == 0) {
            System.out.println("getFirstStepNonDirectional: SUCCESS");
            return d.inverted();
          }

          if (bestP == null
              || lucrativeness[newP.getX()][newP.getY()]
              > lucrativeness[bestP.getX()][bestP.getY()]) {
            bestP = newP;
          }
        }
      }
    } while (nondir[bestP.getX()][bestP.getY()] == nondir[p.getX()][p.getY()] - 1);

    throw new IllegalStateException("getFirstStepNonDirectional should never reach this line");
  }

  public int[][] getDistances() {
    System.out.println("getDistances started");
    int[][] dist = new int[size][size];
    lucrativeness = new int[size][size];

    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        dist[x][y] = Integer.MAX_VALUE;
      }
    }

    Point head = getMe();
    System.out.printf("Me: %d %d\n", head.getX(), head.getY());

    int step = 0;
    PriorityQueue<Point> openSet =
        new PriorityQueue<>(
            (p1, p2) ->
                Integer.compare(
                    lucrativeness[p2.getX()][p2.getY()], lucrativeness[p1.getX()][p1.getY()]));
    openSet.add(head);
    boolean[][] visited = new boolean[size][size];

    while (!openSet.isEmpty()) {
      PriorityQueue<Point> closedSet = openSet;
      openSet =
          new PriorityQueue<>(
              (p1, p2) ->
                  Integer.compare(
                      lucrativeness[p2.getX()][p2.getY()], lucrativeness[p1.getX()][p1.getY()]));

      while (!closedSet.isEmpty()) {
        Point p = closedSet.remove();
        if (!visited[p.getX()][p.getY()]) {
          visited[p.getX()][p.getY()] = true;
          dist[p.getX()][p.getY()] = step;

          for (Direction ndir : Direction.onlyDirections()) {
            if (step > 0 || ndir != headDirection.inverted()) {
              Point np = p.copy();
              np.change(ndir);

              if (!isNotPassableOrRisky(np) && step + 1 < dist[np.getX()][np.getY()]) {
                lucrativeness[np.getX()][np.getY()] =
                    Math.max(
                        lucrativeness[p.getX()][p.getY()] + getPointLucrativity(np),
                        lucrativeness[np.getX()][np.getY()]);
                openSet.add(np);
              }
            }
          }
        }
      }

      ++step;
    }

    System.out.println("Distances: ");
    for (int y = size; y-- > 0; ) {
      for (int x = 0; x < size; x++) {
        if (dist[x][y] < Integer.MAX_VALUE) {
          System.out.printf("%d", dist[x][y] / 10);
        } else {
          System.out.printf("*");
        }
      }
      System.out.println();
    }

    System.out.println("Lucrativity: ");
    for (int y = size; y-- > 0; ) {
      for (int x = 0; x < size; x++) {
        System.out.printf("%d", lucrativeness[x][y]);
      }
      System.out.println();
    }

    System.out.println("getDistances finished");
    return dist;
  }

  protected int getPointLucrativity(Point p) {
    return Elements.POWER_UPS.contains(getAt(p)) ? 1 : 0;
  }

  public List<Point> getNeighborPoints(Point p) {
    List<Point> l = new ArrayList<>();
    for (Direction d : Direction.onlyDirections()) {
      Point np = p.copy();
      np.change(d);
      if (isWithinBoard(np)) {
        l.add(np);
      }
    }

    return l;
  }

  public List<Point> getNeighborPoints(int x, int y) {
    return getNeighborPoints(new PointImpl(x, y));
  }

  public int getMySnakeLength() {
    int displayedLength = get(MY_SNAKE.toArray(new Elements[0])).size();
    return stoneEatenLastRound ? displayedLength - 3 : displayedLength;
  }

  public boolean areWeFurious() {
    return getAt(getMe()) == HEAD_EVIL;
  }

  @Override
  public Elements valueOf(char ch) {
    return Elements.valueOf(ch);
  }

  public boolean isBarrierAt(Point p) {
    return isBarrierAt(p.getX(), p.getY());
  }

  public boolean isBarrierAt(int x, int y) {
    // ENEMY_HEAD_DEAD is not a barrier, but ENEMY_HEAD_SLEEP and ENEMY_TAIL_INACTIVE are
    return isAt(
        x,
        y,
        WALL,
        START_FLOOR,
        ENEMY_HEAD_SLEEP,
        ENEMY_TAIL_INACTIVE);
  }

  public boolean isStoneAt(Point p) {
    return isStoneAt(p.getX(), p.getY());
  }

  public boolean isStoneAt(int x, int y) {
    return isAt(x, y, STONE);
  }

  public boolean isDynamicBarrier(Point p) {
    return isDynamicBarrier(p.getX(), p.getY());
  }

  public boolean isDynamicBarrier(int x, int y) {
    return isAt(
        x,
        y,
        BODY_HORIZONTAL,
        BODY_VERTICAL,
        BODY_LEFT_DOWN,
        BODY_LEFT_UP,
        BODY_RIGHT_DOWN,
        BODY_RIGHT_UP,
        ENEMY_HEAD_DOWN,
        ENEMY_HEAD_LEFT,
        ENEMY_HEAD_RIGHT,
        ENEMY_HEAD_UP,
        ENEMY_HEAD_EVIL,
        ENEMY_TAIL_END_DOWN,
        ENEMY_TAIL_END_LEFT,
        ENEMY_TAIL_END_UP,
        ENEMY_TAIL_END_RIGHT,
        ENEMY_TAIL_INACTIVE,
        ENEMY_BODY_HORIZONTAL,
        ENEMY_BODY_VERTICAL,
        ENEMY_BODY_LEFT_DOWN,
        ENEMY_BODY_LEFT_UP,
        ENEMY_BODY_RIGHT_DOWN,
        ENEMY_BODY_RIGHT_UP);
  }

  public boolean isWithinBoard(Point p) {
    return isWithinBoard(p.getX(), p.getY());
  }

  public boolean isWithinBoard(int x, int y) {
    return x >= 0 && y >= 0 && x < size && y < size;
  }

  public boolean isNotPassableOrRisky(Point p) {
    return isNotPassableOrRisky(p.getX(), p.getY());
  }

  public int distanceFromMe(Point p) {
    return getManhattanDistance(getMe(), p);
  }

  public int distanceFromMe(int x, int y) {
    return getManhattanDistance(getMe(), new PointImpl(x, y));
  }

  protected boolean isNextStepCollisionPossible(int x, int y) {
    if (distanceFromMe(x, y) == 1) {
      if (isBarrierAt(x, y)) {
        return true;
      }
      boolean weAreFurious = areWeFurious();
      if (!weAreFurious
          && isStoneAt(x, y)
          && (getMySnakeLength() - STONE_LENGTH_COST < MIN_SNAKE_LENGTH)) {
        return true;
      }

      Elements e = getAt(x, y);
      if (MY_BODY.contains(e)) {
        // actually we can cut a piece of ourselves and survive, but we'll implement it later
        return true;
      }

      if (!weAreFurious && (ENEMY_BODY.contains(e) || ENEMY_HEAD.contains(e))) {
        return true;
      }

      // check head-to-head collision danger
      HashSet<Elements> neighbors = new HashSet<>(getNear(x, y));
      neighbors.remove(ENEMY_HEAD_FLY);
      neighbors.remove(HEAD_FLY);

      if (neighbors.stream().anyMatch(MY_HEAD::contains)) {
        for (Point p : getNeighborPoints(x, y)) {
          Elements enemyHeadPoint = getAt(p);
          if (ENEMY_HEAD.contains(enemyHeadPoint)) {
            OldSnake enemySnake = OldSnake.identify(p.getX(), p.getY(), this);

            if (!weAreFurious && enemySnake.isFurious()) {
              return true;
            }
            if (weAreFurious == enemySnake.isFurious()
                && getMySnakeLength() - enemySnake.getLength() < MIN_SNAKE_LENGTH) {
              return true;
            }
          }
        }
      }
    }

    // no immediate danger
    return false;
  }

  public boolean isNotPassableOrRisky(int x, int y) {
    return !isWithinBoard(x, y)
        || isBarrierAt(x, y)
        ||
        // TODO: should check for how long we stay furious here
        isStoneAt(x, y)
            && getMySnakeLength() < 5
            && !(areWeFurious() && distanceFromMe(x, y) < FURY_LENGTH)
        || isNextStepCollisionPossible(x, y);
  }

  public boolean isPowerUp(Point p) {
    return isPowerUp(p.getX(), p.getY());
  }

  public boolean isPowerUp(int x, int y) {
    return isAt(x, y, GOLD, APPLE);
  }

  public int getManhattanDistance(Point p1, Point p2) {
    return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
  }

  @Override
  protected int inversionY(int y) {
    return size - 1 - y;
  }

  protected void refreshMyHead() {
    myHead = get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY).get(0);

    // refresh counters
    if (!get(HEAD_SLEEP).isEmpty()) {
      stoneEatenLastRound = false;
      stoneCount = 0;
      headDirection = Direction.RIGHT;
    }
  }

  public Point getMe() {
    return myHead.copy();
  }

  public boolean isGameOver() {
    return getMyHead().isEmpty();
  }

  private List<Point> getMyHead() {
    return get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY);
  }
}
