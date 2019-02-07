package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.objects.Apple;
import com.codenjoy.dojo.snakebattle.model.objects.FlyingPill;
import com.codenjoy.dojo.snakebattle.model.objects.FuryPill;
import com.codenjoy.dojo.snakebattle.model.objects.Gold;
import com.codenjoy.dojo.snakebattle.model.objects.Stone;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class HeroAnalysis {
  private final SnakeBoard game;
  private final Hero hero;
  private int[][] distances;
  private int[][] values;

  private HeroAnalysis(SnakeBoard game, Hero hero) {
    this.game = game;
    this.hero = hero;
  }

  private static HeroAnalysis analyze(SnakeBoard game, Hero hero) {
    Set<Hero> activeAndAliveHeroes =
        game.getHeroes().stream()
            .filter(h -> h.isAlive() && h.isActive())
            .collect(Collectors.toSet());

    if (activeAndAliveHeroes.contains(hero)) {
      System.out.println("FAIL called analyze with wrong Hero");
      return null;
    }

    HeroAnalysis analysis = new HeroAnalysis(game, hero);
    analysis.findDistancesAndValues(game, hero);
    return analysis;
  }

  private static int[][] findDistances(
      boolean[][] barriers, Point fromPoint, Direction fromDirection) {
    int[][] distances = new int[barriers.length][barriers[0].length];
    for (int[] row : distances) Arrays.fill(row, Integer.MAX_VALUE);

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


  // TODO: split in two because values actually depend on computed distances
  private void findDistancesAndValues(SnakeBoard game, Hero hero) {
    distances = new int[game.size()][game.size()];
    values = new int[game.size()][game.size()];

    for (int x = 0; x < game.size(); x++) {
      for (int y = 0; y < game.size(); y++) {
        distances[x][y] = Integer.MAX_VALUE;
      }
    }

    Point head = hero.head();
    Direction headDirection = hero.getDirection();

    int step = 0;
    PriorityQueue<Point> openSet =
        new PriorityQueue<>(
            (p1, p2) ->
                Integer.compare(values[p2.getX()][p2.getY()], values[p1.getX()][p1.getY()]));

    openSet.add(head);
    boolean[][] visited = new boolean[game.size()][game.size()];

    while (!openSet.isEmpty()) {
      PriorityQueue<Point> closedSet = openSet;
      openSet =
          new PriorityQueue<>(
              (p1, p2) ->
                  Integer.compare(values[p2.getX()][p2.getY()], values[p1.getX()][p1.getY()]));

      while (!closedSet.isEmpty()) {
        Point p = closedSet.remove();
        if (!visited[p.getX()][p.getY()]) {
          visited[p.getX()][p.getY()] = true;
          distances[p.getX()][p.getY()] = step;

          for (Direction ndir : Direction.onlyDirections()) {
            if (step > 0 || ndir != headDirection.inverted()) {
              Point np = p.copy();
              np.change(ndir);

              if (!isNotPassableOrRisky(np) && step + 1 < distances[np.getX()][np.getY()]) {
                values[np.getX()][np.getY()] =
                    Math.max(
                        values[p.getX()][p.getY()] + getPointValue(np, step + 1),
                        values[np.getX()][np.getY()]);
                openSet.add(np);
              }
            }
          }
        }
      }

      ++step;
    }
  }

  private boolean isNotPassableOrRisky(Point np) {
    return false;
  }

  private int getPointValue(Point point, int distanceToPoint) {
    Point p = game.getObjOn(point);
    if (p instanceof Apple) {
      return 3;
    }
    if (p instanceof Gold) {
      return 5;
    }
    if (p instanceof Stone && hero.getFuryCount() >= distanceToPoint) {
      return 12;
    }
    if (p instanceof Stone && hero.size() >= 5) {
      return 10;
    }
    if (p instanceof FlyingPill) {
      return -10;
    }
    if (p instanceof FuryPill) {
      return Math.max(
          getShittingPoints(), getPointsForStonesAround(p, game.furyCount().getValue()));
    }

    // enemy head
    // enemy neck
    // enemy body if I'm furious

    return 0;
  }

  private int getPointsForStonesAround(Point point, int radius) {
    return 10
        * Math.min(
            3,
            (int)
                game.getStones().stream()
                    .filter(stone -> GameHelper.getManhattanDistance(stone, point) < radius)
                    .count());
  }

  private int getShittingPoints() {
    // TODO: rethink this considering tail positioning
    int total = 0;
    int furyRounds = Math.max(0, game.furyCount().getValue() - hero.size() - 1);
    int skips = Math.max(0, hero.size() - hero.getStonesCount());
    if (furyRounds >= hero.getStonesCount()) {
      furyRounds -= hero.getStonesCount();
      total += 10 * hero.getStonesCount();
    }

    while (furyRounds > 0) {
      furyRounds -= skips;
      total += 10 * Math.min(hero.getFuryCount(), hero.getStonesCount());
      furyRounds -= hero.getStonesCount();
    }

    return total;
  }
}
