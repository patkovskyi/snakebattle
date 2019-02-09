package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.DynamicObstacle;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.objects.Apple;
import com.codenjoy.dojo.snakebattle.model.objects.FlyingPill;
import com.codenjoy.dojo.snakebattle.model.objects.FuryPill;
import com.codenjoy.dojo.snakebattle.model.objects.Gold;
import com.codenjoy.dojo.snakebattle.model.objects.Stone;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Analysis {

  private final SnakeBoard game;
  private final Map<Hero, boolean[][]> staticObstacles;
  private final Map<Hero, boolean[][]> dynamicObstacles;
  private final Map<Hero, int[][]> staticDistances;
  private final Map<Hero, int[][]> dynamicDistances;

  private Analysis(SnakeBoard game) {
    this.game = game;
    staticObstacles = new HashMap<>();
    dynamicObstacles = new HashMap<>();
    staticDistances = new HashMap<>();
    dynamicDistances = new HashMap<>();
  }

  public static Analysis create(SnakeBoard game) {
    // TODO: check if game is in valid state and hero is alive & active ?
    Analysis a = new Analysis(game);
    return a;
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
        + Constants.MIN_SNAKE_LENGTH;

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

  boolean[][] getStaticObstacles(Hero hero) {
    return staticObstacles.computeIfAbsent(hero, h -> {
      boolean[][] barriers = new boolean[game.size()][game.size()];
      getBarriers().forEach(b -> barriers[b.getX()][b.getY()] = true);
      return Algorithms.findStaticDeadEnds(barriers);
    });
  }

  boolean[][] getDynamicObstacles(Hero hero) {
    return dynamicObstacles.computeIfAbsent(hero, h -> {
      boolean[][] staticObstacles = getStaticObstacles(hero);
      int[][] staticDistances = getStaticDistances(hero);
      boolean[][] dynamicObstacles = new boolean[game.size()][game.size()];

      for (int x = 0; x < game.size(); x++) {
        for (int y = 0; y < game.size(); y++) {
          dynamicObstacles[x][y] = staticObstacles[x][y];
        }
      }

      getActiveAliveHeroes().forEach(
          enemy -> enemy.getBody().forEach(p -> {
            int roundsToPoint = staticDistances[p.getX()][p.getY()];
            DynamicObstacle obstacle = whatWillBeOnThisPoint(enemy, p, roundsToPoint);
            switch (obstacle) {
              case Neck:
                dynamicObstacles[p.getX()][p.getY()] |= !wouldSurviveHeadToHead(hero, enemy,
                    roundsToPoint);
                break;
              case Body:
                dynamicObstacles[p.getX()][p.getY()] |= !wouldSurviveHeadToBody(hero, enemy,
                    roundsToPoint);
                break;
            }
          }));

      return dynamicObstacles;
    });
  }

  int[][] getStaticDistances(Hero hero) {
    return staticDistances.computeIfAbsent(hero, h -> {
      boolean[][] deadEnds = getStaticObstacles(hero);
      return Algorithms.findStaticDistances(deadEnds, hero.head(), hero.getDirection());
    });
  }

  int[][] getDynamicDistances(Hero hero) {
    return dynamicDistances.computeIfAbsent(hero, h -> {
      boolean[][] dynamicObstacles = getDynamicObstacles(hero);
      return Algorithms.findStaticDistances(dynamicObstacles, hero.head(), hero.getDirection());
    });
  }

  private Stream<Point> getBarriers() {
    return Stream.concat(game.getWalls().stream(), game.getStarts().stream());
  }

  private Stream<Hero> getActiveAliveHeroes() {
    return game.getHeroes().stream().filter(h -> h.isActive() && h.isAlive());
  }

  private Hero getMyHero() {
    return game.getHeroes().get(0);
  }

  private int getPointValue(Point point, int distanceToPoint) {
    Point p = game.getObjOn(point);
    if (p instanceof Apple) {
      return 3;
    }
    if (p instanceof Gold) {
      return 5;
    }
    if (p instanceof Stone && getMyHero().getFuryCount() >= distanceToPoint) {
      return 12;
    }
    if (p instanceof Stone && getMyHero().size() >= 5) {
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
    Hero hero = getMyHero();
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
