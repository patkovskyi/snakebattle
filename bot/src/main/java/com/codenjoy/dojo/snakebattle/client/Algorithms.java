package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.DynamicObstacle;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class Algorithms {

  public static int[][] findDynamicDistances(SnakeBoard game, Hero hero) {
    boolean[][] staticDeadEnds = findStaticDeadEnds(game);
    // int[][] staticDistances = findStaticDistances(staticDeadEnds, hero.head(), hero.getDirection());
    boolean[][] dynamicBarriers = findDynamicBarriers(game, staticDeadEnds, hero);
    return findStaticDistances(dynamicBarriers, hero.head(), hero.getDirection());
  }

  public static int[][] findStaticDistances(SnakeBoard game, Hero hero) {
    boolean[][] staticDeadEnds = findStaticDeadEnds(game);
    return findStaticDistances(staticDeadEnds, hero.head(), hero.getDirection());
  }

  private static int[][] findStaticDistances(
      boolean[][] staticDeadEnds, Point fromPoint, Direction fromDirection) {
    int[][] distances = new int[staticDeadEnds.length][staticDeadEnds[0].length];
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
          // can't turn backwards on the first step of search
          continue;
        }

        Point nextPoint = point.copy();
        nextPoint.change(nextDirection);

        if (!staticDeadEnds[nextPoint.getX()][nextPoint.getY()]
            && distances[point.getX()][point.getY()] + 1
            < distances[nextPoint.getX()][nextPoint.getY()]) {
          q.add(nextPoint);
          distances[nextPoint.getX()][nextPoint.getY()] = distances[point.getX()][point.getY()] + 1;
        }
      }
    }

    return distances;
  }

  public static boolean[][] findStaticDeadEnds(boolean[][] barriers) {
    boolean[][] deadEnds = new boolean[barriers.length][barriers.length];
    for (int i = 0; i < barriers.length; i++) {
      for (int j = 0; j < barriers.length; j++) {
        deadEnds[i][j] = barriers[i][j];
      }
    }

    boolean updated;
    Direction[] directions = Direction.onlyDirections().toArray(new Direction[0]);

    do {
      updated = false;
      for (int x = 0; x < barriers.length; x++) {
        for (int y = 0; y < barriers.length; y++) {
          if (!deadEnds[x][y]) {
            int passableNeighbors = 0;
            for (int d = 0; d < 4; d++) {
              int nx = directions[d].changeX(x);
              int ny = directions[d].changeY(y);
              if (!isOutOf(nx, ny, barriers.length) && !deadEnds[nx][ny]) {
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

  private static boolean[][] findStaticDeadEnds(SnakeBoard game) {
    boolean[][] barriers = new boolean[game.size()][game.size()];
    game.getWalls().forEach(w -> barriers[w.getX()][w.getY()] = true);
    return findStaticDeadEnds(barriers);
  }

  private static boolean[][] findDynamicBarriers(
      SnakeBoard game, boolean[][] staticDeadEnds, Hero hero) {
    int[][] staticDistances = findStaticDistances(staticDeadEnds, hero.head(), hero.getDirection());

    boolean[][] dynamicBarriers = new boolean[game.size()][game.size()];
    for (int x = 0; x < game.size(); x++) {
      for (int y = 0; y < game.size(); y++) {
        dynamicBarriers[x][y] = staticDeadEnds[x][y];
      }
    }

    game.getHeroes().stream()
        .filter(h -> h.isAlive() && h.isActive())
        .forEach(
            enemy -> {
              enemy.getBody().forEach(p -> {
                int roundsToPoint = staticDistances[p.getX()][p.getY()];
                DynamicObstacle obstacle = whatWillBeOnThisPoint(enemy, p, roundsToPoint);
                switch (obstacle) {
                  case Neck:
                    dynamicBarriers[p.getX()][p.getY()] |= !wouldSurviveHeadToHead(hero, enemy,
                        roundsToPoint);
                    break;
                  case Body:
                    dynamicBarriers[p.getX()][p.getY()] |= !wouldSurviveHeadToBody(hero, enemy,
                        roundsToPoint);
                    break;
                }
              });
            });

    return dynamicBarriers;
  }

  private static DynamicObstacle whatWillBeOnThisPoint(Hero hero, Point point, int rounds) {
    int trueBodyIndex = Math.max(-1, hero.getBodyIndex(point) + hero.getGrowBy() - rounds);
    if (trueBodyIndex < 0) {
      return DynamicObstacle.Nothing;
    }

    int trueSize = hero.size() + hero.getGrowBy();
    int trueDistanceFromHead = trueSize - trueBodyIndex - 1;
    // TODO: this place assumes head == neck
    if (trueDistanceFromHead <= 1) {
      return DynamicObstacle.Neck;
    } else {
      return DynamicObstacle.Body;
    }
  }

  private static boolean wouldSurviveHeadToHead(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroLonger = hero.size() + hero.getGrowBy() >= enemy.size() + enemy.getGrowBy()
        + GameHelper.MIN_SNAKE_LENGTH;

    boolean heroFury = hero.getFuryCount() >= ticksToCollision;
    boolean enemyFury = enemy.getFuryCount() >= ticksToCollision;
    boolean heroFly = hero.getFlyingCount() >= ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() >= ticksToCollision;

    return heroFly || enemyFly || heroFury && !enemyFury || heroFury == enemyFury && heroLonger;
  }

  private static boolean wouldSurviveHeadToBody(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroFury = hero.getFuryCount() >= ticksToCollision;
    boolean heroFly = hero.getFlyingCount() >= ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() >= ticksToCollision;

    return heroFury || heroFly || enemyFly;
  }
}
