package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class AlgoHelper {

  private static int[][] findDistances(
      boolean[][] barriers, Point fromPoint, Direction fromDirection) {
    int[][] distances = new int[barriers.length][barriers[0].length];
    for (int[] row : distances) {
      Arrays.fill(row, Integer.MAX_VALUE);
    }

    Queue<Point> q = new ArrayDeque<>();
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

        if (!barriers[nextPoint.getX()][nextPoint.getY()]
            && distances[point.getX()][point.getY()] + 1
                < distances[nextPoint.getX()][nextPoint.getY()]) {
          q.add(nextPoint);
          distances[nextPoint.getX()][nextPoint.getY()] = distances[point.getX()][point.getY()] + 1;
        }
      }
    }

    return distances;
  }

  private static boolean[][] getDynamicBarriers(
      SnakeBoard game, boolean[][] staticDeadEnds, Hero hero) {
    int[][] staticDistances = findDistances(staticDeadEnds, hero.head(), hero.getDirection());

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
              // start from 1 (if no negative growBy) because tail is immediately passable
              // skip neck and head in this loop
              for (int i = 1 - enemy.getGrowBy(); i < enemy.getBody().size() - 2; i++) {
                Point p = enemy.getBody().get(i);
                if (hero.getFuryCount() < staticDistances[p.getX()][p.getY()]
                    && hero.getFlyingCount() < staticDistances[p.getX()][p.getY()]
                    && enemy.getFlyingCount() < staticDistances[p.getX()][p.getY()]) {
                  dynamicBarriers[p.getX()][p.getY()] = true;
                }
              }

              for (int i = enemy.getBody().size() - 2; i < enemy.getBody().size(); i++) {
                Point p = enemy.getBody().get(i);
                dynamicBarriers[p.getX()][p.getY()] =
                    !wouldSurviveHeadToHead(hero, enemy, staticDistances[p.getX()][p.getY()]);
              }
            });

    return dynamicBarriers;
  }

  private static boolean wouldSurviveHeadToHead(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroLonger =
        hero.size() + hero.getGrowBy()
            > enemy.size() + enemy.getGrowBy() + GameHelper.MIN_SNAKE_LENGTH;

    boolean heroFury = hero.getFuryCount() >= ticksToCollision;
    boolean enemyFury = enemy.getFuryCount() >= ticksToCollision;
    boolean heroFly = hero.getFlyingCount() >= ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() >= ticksToCollision;

    return heroFly || enemyFly || heroFury && !enemyFury || heroFury == enemyFury && heroLonger;
  }
}
