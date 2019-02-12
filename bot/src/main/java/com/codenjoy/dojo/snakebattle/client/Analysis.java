package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.DynamicObstacle;
import com.codenjoy.dojo.snakebattle.model.HeroAction;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.hero.Tail;
import com.codenjoy.dojo.snakebattle.model.objects.Apple;
import com.codenjoy.dojo.snakebattle.model.objects.FlyingPill;
import com.codenjoy.dojo.snakebattle.model.objects.FuryPill;
import com.codenjoy.dojo.snakebattle.model.objects.Gold;
import com.codenjoy.dojo.snakebattle.model.objects.Stone;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Analysis {

  protected final SnakeBoard game;

  private final Map<Hero, boolean[][]> staticObstacles;
  private final Map<Hero, boolean[][]> dynamicObstacles;
  private final Map<Hero, int[][]> staticDistances;
  private final Map<Hero, int[][]> dynamicDistances;
  private final Map<Hero, int[][]> values;
  private final Map<Hero, double[][]> distanceAdjustedValues;
  private final Map<Hero, int[][]> accumulatedValues;
  private final Map<Hero, double[][]> closestAdjustedValues;

  protected Analysis(SnakeBoard game) {
    this.game = game;
    staticObstacles = new HashMap<>();
    dynamicObstacles = new HashMap<>();
    staticDistances = new HashMap<>();
    dynamicDistances = new HashMap<>();
    values = new HashMap<>();
    distanceAdjustedValues = new HashMap<>();
    accumulatedValues = new HashMap<>();
    closestAdjustedValues = new HashMap<>();
  }

  public abstract HeroAction findBestAction();

  public boolean hasRoundStarted() {
    return getMyHero().isActive();
  }

  boolean[][] getStaticObstacles(Hero hero) {
    return staticObstacles.computeIfAbsent(hero, h -> {
      boolean[][] obstacles = new boolean[game.size()][game.size()];
      getBarriers().forEach(b -> obstacles[b.getX()][b.getY()] = true);
      return Algorithms.findDirectionalDeadEnds(obstacles, hero.head(), hero.getDirection());
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

      game.getStones().forEach(s -> {
        int distanceToStone = staticDistances[s.getX()][s.getY()];
        dynamicObstacles[s.getX()][s.getY()] |= !Mechanics.canPassStone(hero, distanceToStone);
      });

      getAliveActiveHeroes().forEach(
          enemy -> enemy.body().forEach(p -> {
            int roundsToPoint = staticDistances[p.getX()][p.getY()];
            DynamicObstacle obstacle = Mechanics.whatWillBeOnThisPoint(enemy, p, roundsToPoint);
            switch (obstacle) {
              case Neck:
                dynamicObstacles[p.getX()][p.getY()] |= !Mechanics
                    .wouldSurviveHeadToHead(hero, enemy,
                        roundsToPoint);
                break;
              case Body:
                dynamicObstacles[p.getX()][p.getY()] |= !Mechanics
                    .wouldSurviveHeadToBody(hero, enemy,
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

  int[][] getValues(Hero hero) {
    return values.computeIfAbsent(hero, h -> {
      int[][] values = new int[game.size()][game.size()];
      int[][] distances = getDynamicDistances(hero);

      // GOLD - simplest
      game.getGold().forEach(p -> values[p.getX()][p.getY()] = Mechanics.GOLD_REWARD);

      // FLIGHT - useless
      game.getFlyingPills().forEach(p -> values[p.getX()][p.getY()] = Mechanics.SOMEWHAT_NEGATIVE);

      // APPLES - higher value in late game
      game.getApples().forEach(p ->
          values[p.getX()][p.getY()] =
              Mechanics.APPLE_REWARD + (Mechanics.isLateGame(game) ? 6 : 3));

      // FURY - very useful, TODO calculation
      game.getFuryPills().forEach(p -> values[p.getX()][p.getY()] = 30);

      // STONES - complex
      game.getStones().forEach(s -> {
        boolean heroFury = hero.getFuryCount() >= distances[s.getX()][s.getY()];
        boolean heroFly = hero.getFlyingCount() >= distances[s.getX()][s.getY()];
        boolean heroLong =
            getTrueLength(hero) - Mechanics.STONE_LENGTH_PENALTY >= Mechanics.MIN_SNAKE_LENGTH;

        if (heroFly) {
          values[s.getX()][s.getY()] = 0;
        } else if (heroFury) {
          // TODO: think about how stones eaten with fury should be valued higher
          values[s.getX()][s.getY()] = Mechanics.STONE_REWARD;
        } else if (heroLong) {
          if (!Mechanics.isLateGame(game)) {
            values[s.getX()][s.getY()] = Mechanics.STONE_REWARD - 1;
          }
        } else {
          values[s.getX()][s.getY()] = Mechanics.VERY_NEGATIVE;
        }
      });

      // LEAVE STONE AT TAIL
      Point tail = hero.getTailPoint();
      int roundsToTail = distances[tail.getX()][tail.getY()];
      if (hero.getFuryCount() >= roundsToTail && hero.getStonesCount() > 0) {
        values[tail.getX()][tail.getY()] = Mechanics.STONE_REWARD;
      }

      // GUARANTEED KILLS - very useful
      getAliveActiveEnemies().forEach(enemy -> enemy.body().forEach(p -> {
        int roundsToTarget = distances[p.getX()][p.getY()];

        switch (Mechanics.whatWillBeOnThisPoint(enemy, p, roundsToTarget)) {
          case Nothing:
            break;
          case Body:
            if (Mechanics.wouldWinHeadToBody(hero, enemy, roundsToTarget)) {
              int lengthToEat = Mechanics.getTrueBodyIndex(enemy, p) - roundsToTarget;
              values[p.getX()][p.getY()] += Mechanics.BLOOD_REWARD_PER_CELL * lengthToEat;
            } else if (Mechanics.wouldSurviveHeadToBody(hero, enemy, roundsToTarget)) {
              values[p.getX()][p.getY()] = 0;
            } else {
              values[p.getX()][p.getY()] = Mechanics.VERY_NEGATIVE;
            }
            break;
          case Neck:
            if (Mechanics.wouldWinHeadToHead(hero, enemy, roundsToTarget)) {
              int lengthToEat = Mechanics.getTrueLength(enemy);
              values[p.getX()][p.getY()] +=
                  Mechanics.ROUND_REWARD + Mechanics.BLOOD_REWARD_PER_CELL * lengthToEat;
            }
        }
      }));

//      getAliveActiveEnemies().forEach(enemy ->
//      {
//        Point enemyHead = enemy.head();
//
//        if ( static)
//
//        for (Direction possibleDir : Direction.onlyDirections()) {
//          if (possibleDir.inverted() != enemy.getDirection()) {
//            Point possibleNextPoint = enemyHead.copy();
//            possibleNextPoint.change(possibleDir);
//
//            if (!game.isBarrier())
//          }
//        }
//      });

      return values;
    });
  }

  double[][] getDistanceAdjustedValues(Hero hero) {
    return distanceAdjustedValues.computeIfAbsent(hero, h -> {
      int[][] values = getValues(hero);
      int[][] distances = getDynamicDistances(hero);
      double[][] result = new double[game.size()][game.size()];

      for (int x = 0; x < game.size(); x++) {
        for (int y = 0; y < game.size(); y++) {
          result[x][y] = values[x][y] / (double) distances[x][y];
        }
      }

      return result;
    });
  }

  int[][] getAccumulatedValues(Hero hero) {
    return accumulatedValues.computeIfAbsent(hero,
        h -> Algorithms.findAccumulatedValues(getDynamicDistances(hero), getValues(hero)));
  }

  protected Stream<Point> getBarriers() {
    return Stream.concat(game.getWalls().stream(), game.getStarts().stream());
  }

  protected Stream<Hero> getAliveActiveHeroes() {
    return game.getHeroes().stream().filter(h -> h.isActive() && h.isAlive());
  }

  protected Stream<Hero> getAliveActiveEnemies() {
    Hero myHero = getMyHero();
    return getAliveActiveHeroes().filter(h -> h != myHero);
  }

  protected Hero getMyHero() {
    return game.getHeroes().get(0);
  }

  protected String getTargetPointType(Point point) {
    Point p = game.getOn(point);

    if (p instanceof Apple) {
      return "APPLE";
    }
    if (p instanceof Gold) {
      return "GOLD";
    }
    if (p instanceof Stone) {
      return "STONE";
    }
    if (p instanceof FlyingPill) {
      return "FLIGHT";
    }
    if (p instanceof FuryPill) {
      return "FURY";
    }
    if (p instanceof Tail) {
      return "ENEMY";
    }

    return "NOTHING";
  }

////  private int getPointValue(Point point, int distanceToPoint) {
////    Point p = game.getOn(point);
////    if (p instanceof Apple) {
////      return 3;
////    }
////    if (p instanceof Gold) {
////      return 5;
////    }
////    if (p instanceof Stone && getMyHero().getFuryCount() >= distanceToPoint) {
////      return 12;
////    }
////    if (p instanceof Stone && getMyHero().size() >= 5) {
////      return 10;
////    }
////    if (p instanceof FlyingPill) {
////      return -10;
////    }
////    if (p instanceof FuryPill) {
////      return Math.max(
////          estimatePointsForFuryShitLoop(getMyHero()),
////          estimatePointsForFuryStonesAround(p, game.furyCount().getValue()));
////    }
////
////    // enemy head
////    // enemy neck
////    // enemy body if I'm furious
////
////    return 0;
////  }
//
//  private int estimatePointsForFuryStonesAround(Point point, int radius) {
//    int stonesWithinRadius = (int) game.getStones().stream()
//        .filter(stone -> GameHelper.getManhattanDistance(stone, point) < radius)
//        .count();
//
//    return Mechanics.STONE_REWARD * Math.min(3, stonesWithinRadius);
//  }

  private int getTrueLength(Hero hero) {
    return hero.size() + hero.getGrowBy();
  }

  public double[][] getClosestAdjustedValues(Hero hero) {
    return closestAdjustedValues.computeIfAbsent(hero, h -> {
      double[][] values = getDistanceAdjustedValues(hero);
      double[][] result = new double[game.size()][game.size()];

      for (int x = 0; x < game.size(); x++) {
        for (int y = 0; y < game.size(); y++) {
          if (getDynamicClosestHero(PointImpl.pt(x, y)) == hero) {
            result[x][y] = values[x][y];
          }
        }
      }

      return result;
    });
  }

  private Hero getDynamicClosestHero(Point point) {
    return getAliveActiveHeroes().min((h1, h2) -> {
      int dist1 = getDynamicDistances(h1)[point.getX()][point.getY()];
      int dist2 = getDynamicDistances(h1)[point.getX()][point.getY()];
      int cmp = Integer.compare(dist1, dist2);

      if (cmp == 0) {
        cmp = Mechanics.wouldSurviveHeadToHead(h1, h2, dist1) ? -1 : 1;
      }

      return cmp;
    }).orElseGet(null);
  }
}
