package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * This class should know NOTHING about our domain.
 */
public class Algorithms {

  public static int[][] arrayCopy(int[][] original) {
    int[][] copy = new int[original.length][];
    for (int i = 0; i < original.length; i++) {
      copy[i] = Arrays.copyOf(original[i], original[i].length);
    }

    return copy;
  }

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
        if (distances[point.getX()][point.getY()] == 0 &&
            fromDirection == nextDirection.inverted()) {
          // can't turn backwards on the first step of a search
          continue;
        }

        Point nextPoint = point.copy();
        nextPoint.change(nextDirection);

        if (!nextPoint.isOutOf(distances.length)
            && !staticObstacles[nextPoint.getX()][nextPoint.getY()]
            && distances[point.getX()][point.getY()] + 1
            < distances[nextPoint.getX()][nextPoint.getY()]) {
          q.add(nextPoint);
          distances[nextPoint.getX()][nextPoint.getY()] = distances[point.getX()][point.getY()] + 1;
        }
      }
    }

    return distances;
  }

  public static int[][] findAccumulatedValues(int[][] distances, int[][] values) {
    Point start = findStartingPoint(distances);
    int[][] acc = new int[values.length][values.length];
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values.length; j++) {
        if (distances[i][j] == Integer.MAX_VALUE) {
          acc[i][j] = Integer.MIN_VALUE;
        }
      }
    }

    Set<Point> openSet = new HashSet<>();
    openSet.add(start);

    while (!openSet.isEmpty()) {
      Queue<Point> closedSet = new ArrayDeque<>(openSet);
      openSet = new HashSet<>();

      while (!closedSet.isEmpty()) {
        Point p = closedSet.remove();
        acc[p.getX()][p.getY()] += values[p.getX()][p.getY()];

        for (Direction newDirection : Direction.onlyDirections()) {
          Point np = p.copy();
          np.change(newDirection);

          if (!np.isOutOf(values.length) &&
              distances[np.getX()][np.getY()] == distances[p.getX()][p.getY()] + 1) {

            if (!openSet.contains(np)) {
              // hack to make sure that even negative values are set
              acc[np.getX()][np.getY()] = acc[p.getX()][p.getY()];
            } else {
              acc[np.getX()][np.getY()] = Math
                  .max(acc[p.getX()][p.getY()], acc[np.getX()][np.getY()]);
            }

            openSet.add(np);
          }
        }
      }
    }

    return acc;
  }

  public static boolean[][] findDirectionalDeadEnds(boolean[][] obstacles, Point fromPoint,
      Direction fromDirection) {
    boolean[][] deadEnds = new boolean[obstacles.length][obstacles.length];
    for (int i = 0; i < obstacles.length; i++) {
      Arrays.fill(deadEnds[i], true);
    }

    recDirectionalDeadEnds(obstacles, fromPoint, fromDirection, deadEnds,
        new boolean[obstacles.length][obstacles.length]);

    return deadEnds;
  }

  private static boolean recDirectionalDeadEnds(boolean[][] obstacles, Point fromPoint,
      Direction fromDirection, boolean[][] deadEnds, boolean[][] visited) {
    visited[fromPoint.getX()][fromPoint.getY()] = true;

    boolean foundCycle = false;
    for (Direction newDirection : Direction.onlyDirections()) {
      if (fromDirection != newDirection.inverted()) {
        Point newPoint = fromPoint.copy();
        newPoint.change(newDirection);

        if (!newPoint.isOutOf(obstacles.length) && visited[newPoint.getX()][newPoint.getY()]) {
          foundCycle = true;
          continue;
        }

        if (!newPoint.isOutOf(obstacles.length) && !obstacles[newPoint.getX()][newPoint.getY()]) {
          foundCycle |= recDirectionalDeadEnds(obstacles, newPoint, newDirection, deadEnds,
              visited);
        }
      }
    }

    if (foundCycle) {
      deadEnds[fromPoint.getX()][fromPoint.getY()] = false;
    }

    return foundCycle;
  }

  private static Point findStartingPoint(int[][] distances) {
    Point start = null;
    for (int x = 0; x < distances.length; x++) {
      for (int y = 0; y < distances.length; y++) {
        if (distances[x][y] == 0) {
          if (start != null) {
            throw new IllegalArgumentException("Found more than one starting point");
          }

          start = PointImpl.pt(x, y);
        }
      }
    }

    if (start == null) {
      throw new IllegalArgumentException("Could not find starting point.");
    }
    return start;
  }

  private static boolean isOutOf(int x, int y, int size) {
    return x < 0 || y < 0 || x >= size || y >= size;
  }

//  public static int maximumTSP(int[][] distancesBetweenObjects, int[] distanceFromMeToObject,
//      int[] objectValues, int turns) {
//    int numObjects = objectValues.length;
//    // m[i][t] will reflect total value we can get if we finish on object i and have t turns left
//    int[][] m = new int[numObjects][turns + 1];
//    Set<Integer>[] visited = new HashSet[numObjects];
//
//    for (int t = turns; t-- > 0; ) {
//      for (int i = 0; i < objectValues.length; i++) {
//        if (distanceFromMeToObject[i] <= t) {
//          m[i][turns - distanceFromMeToObject[i]] = objectValues[i];
//          visited[i].add(i);
//        }
//      }
//    }
//
//    for (int t = turns; t-- > 0; ) {
//      for (int i = 0; i < objectValues.length; i++) {
//        for (int j = 0; j < objectValues.length; j++) {
//          if (visited[i].contains(j))
//            continue;
//
//          if (t >= distancesBetweenObjects[i][j]) {
//            m[j][t - distancesBetweenObjects[i][j]] =
//                Math.max(m[j][t - distancesBetweenObjects[i][j]], m[i][t]);
//          }
//        }
//      }
//    }
//  }

//  public static boolean[][] findStaticDeadEnds(boolean[][] obstacles) {
//    boolean[][] deadEnds = new boolean[obstacles.length][obstacles.length];
//    for (int i = 0; i < obstacles.length; i++) {
//      for (int j = 0; j < obstacles.length; j++) {
//        deadEnds[i][j] = obstacles[i][j];
//      }
//    }
//
//    boolean updated;
//    Direction[] directions = Direction.onlyDirections().toArray(new Direction[0]);
//
//    do {
//      updated = false;
//      for (int x = 0; x < obstacles.length; x++) {
//        for (int y = 0; y < obstacles.length; y++) {
//          if (!deadEnds[x][y]) {
//            int passableNeighbors = 0;
//            for (int d = 0; d < 4; d++) {
//              int nx = directions[d].changeX(x);
//              int ny = directions[d].changeY(y);
//              if (!isOutOf(nx, ny, obstacles.length) && !deadEnds[nx][ny]) {
//                ++passableNeighbors;
//              }
//            }
//
//            if (passableNeighbors <= 1) {
//              deadEnds[x][y] = true;
//              updated = true;
//            }
//          }
//        }
//      }
//    } while (updated);
//
//    return deadEnds;
//  }
}
