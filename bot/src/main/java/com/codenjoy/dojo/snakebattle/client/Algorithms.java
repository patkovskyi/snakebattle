package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * This class should know NOTHING about our domain.
 */
public class Algorithms {

  public static int[][] findStaticDistances(
      boolean[][] staticObstacles, Point fromPoint, Direction fromDirection) {
    int[][] distances = new int[staticObstacles.length][staticObstacles[0].length];
    for (int[] row : distances) {
      Arrays.fill(row, Integer.MAX_VALUE);
    }

    Queue<Point> q = new ArrayDeque<>();
    q.add(fromPoint);
    distances[fromPoint.getX()][fromPoint.getY()] = 0;

    while (!q.isEmpty()) {
      Point point = q.remove();

      for (Direction nextDirection : Direction.onlyDirections()) {
        if (distances[point.getX()][point.getY()] == 0
            && nextDirection == fromDirection.inverted()) {
          // can't turn backwards on the first step of a search
          continue;
        }

        Point nextPoint = point.copy();
        nextPoint.change(nextDirection);

        if (!staticObstacles[nextPoint.getX()][nextPoint.getY()]
            && distances[point.getX()][point.getY()] + 1
            < distances[nextPoint.getX()][nextPoint.getY()]) {
          q.add(nextPoint);
          distances[nextPoint.getX()][nextPoint.getY()] = distances[point.getX()][point.getY()] + 1;
        }
      }
    }

    return distances;
  }

  public static boolean[][] findStaticDeadEnds(boolean[][] staticObstacles) {
    boolean[][] deadEnds = new boolean[staticObstacles.length][staticObstacles.length];
    for (int i = 0; i < staticObstacles.length; i++) {
      for (int j = 0; j < staticObstacles.length; j++) {
        deadEnds[i][j] = staticObstacles[i][j];
      }
    }

    boolean updated;
    Direction[] directions = Direction.onlyDirections().toArray(new Direction[0]);

    do {
      updated = false;
      for (int x = 0; x < staticObstacles.length; x++) {
        for (int y = 0; y < staticObstacles.length; y++) {
          if (!deadEnds[x][y]) {
            int passableNeighbors = 0;
            for (int d = 0; d < 4; d++) {
              int nx = directions[d].changeX(x);
              int ny = directions[d].changeY(y);
              if (!isOutOf(nx, ny, staticObstacles.length) && !deadEnds[nx][ny]) {
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

  private static boolean isOutOf(int x, int y, int size) {
    return x < 0 || y < 0 || x >= size || y >= size;
  }
}
