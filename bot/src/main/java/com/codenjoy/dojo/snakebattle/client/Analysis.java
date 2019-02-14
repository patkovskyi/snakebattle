package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.DynamicObstacle;
import com.codenjoy.dojo.snakebattle.model.HeroAction;
import com.codenjoy.dojo.snakebattle.model.MeetingPoint;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.hero.Tail;
import com.codenjoy.dojo.snakebattle.model.objects.Apple;
import com.codenjoy.dojo.snakebattle.model.objects.FlyingPill;
import com.codenjoy.dojo.snakebattle.model.objects.FuryPill;
import com.codenjoy.dojo.snakebattle.model.objects.Gold;
import com.codenjoy.dojo.snakebattle.model.objects.Stone;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Analysis {

  protected final SnakeBoard game;
  private final Map<Hero, boolean[][]> staticObstacles;
  private final Map<Hero, boolean[][]> dynamicObstacles;
  private final Map<Hero, int[][]> staticDistances;
  private final Map<Hero, int[][]> dynamicDistances;
  private final Map<Hero, int[][]> values;
  private final Map<Hero, double[][]> distanceAdjustedValues;
  private final Map<Hero, int[][]> accumulatedValues;
  private final Map<Hero, double[][]> accumulatedDistanceAdjustedValues;
  private final Map<Hero, Optional<Point>> assumedTarget;
  // not used
  private final Map<Hero, double[][]> closestAdjustedValues;
  private boolean[][] barriers;

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
    accumulatedDistanceAdjustedValues = new HashMap<>();
    assumedTarget = new HashMap<>();
  }

  public HeroAction findBestAction() {
    Hero me = getMyHero();

    Point target = findMaxPoint(me);
    if (target != null) {
      String targetType = getTargetPointType(target);
      int value = getValues(me)[target.getX()][target.getY()];
      int accValue = getAccumulatedValues(me)[target.getX()][target.getY()];
      int distance = getDynamicDistances(me)[target.getX()][target.getY()];

      System.out
          .printf("Heading to: [%d %d] (%s | value = %d | acc.value = %d | distance = %d)\n\n",
              target.getX(), target.getY(), targetType, value, accValue, distance);

      printHeroAnalytics();

      int addAction = 0;
      if (target.equals(me.getTailPoint())) {
        addAction = 4;

        // HACK: this is necessary to count stone eaten when we move onto our own tail since we never see it from server
        game.addToPoint(new Stone(me.getTailPoint()));

        me.reduceStoneCount();
      }
      return HeroAction.valueOf(addAction + findFirstStepTo(me, target).value());
    } else {
      System.out.println("FAIL: target == null, we're in dead end");
      return HeroAction.valueOf(me.getDirection().value());
    }
  }

  boolean[][] getBarriers() {
    if (barriers == null) {
      barriers = new boolean[game.size()][game.size()];
      getBarrierObjects().forEach(b -> barriers[b.getX()][b.getY()] = true);
    }

    return barriers;
  }

  boolean[][] getStaticObstacles(Hero hero) {
    return staticObstacles.computeIfAbsent(hero,
        h -> Algorithms.findDirectionalDeadEnds(getBarriers(), hero.head(), hero.getDirection()));
  }

  boolean[][] getDynamicObstacles(Hero hero) {
    return dynamicObstacles.computeIfAbsent(hero, h -> {
      boolean[][] staticObstacles = getStaticObstacles(hero);
      int[][] staticDistances = getStaticDistances(hero);
      boolean[][] dynObstacles = new boolean[game.size()][game.size()];

      for (int x = 0; x < game.size(); x++) {
        for (int y = 0; y < game.size(); y++) {
          dynObstacles[x][y] = staticObstacles[x][y];
        }
      }

      game.getStones().forEach(s -> {
        int distanceToStone = staticDistances[s.getX()][s.getY()];
        dynObstacles[s.getX()][s.getY()] |= !Mechanics.canPassStone(hero, distanceToStone);
      });

      getAliveActiveHeroes().forEach(
          enemy -> enemy.body().forEach(p -> {
            int roundsToPoint = staticDistances[p.getX()][p.getY()];
            DynamicObstacle obstacle = Mechanics.whatWillBeOnThisPoint(enemy, p, roundsToPoint);
            switch (obstacle) {
              case Neck:
                dynObstacles[p.getX()][p.getY()] |= !Mechanics.wouldSurviveHeadToHead(hero, enemy,
                    roundsToPoint);
                break;
              case Body:
                if (hero != enemy) {
                  dynObstacles[p.getX()][p.getY()] |= !Mechanics.wouldSurviveHeadToBody(hero, enemy,
                      roundsToPoint);
                }

                break;
              case PossibleStone:
                // assume that enemy drops a stone at his tail
                // TODO: this is more like a negative value than obstacle
                if (hero != enemy && hero.getFuryCount() <= roundsToPoint) {
                  dynObstacles[p.getX()][p.getY()] |= true;
                }
                break;
            }
          }));

      // consider going head-to-head with a stronger hero just as bad as insta-death
      getAliveActiveEnemies(hero).forEach(enemy -> {
        Point enemyHead = enemy.head();
        int distanceToEnemyHead = staticDistances[enemyHead.getX()][enemyHead.getY()];
        if ((distanceToEnemyHead == 3 || distanceToEnemyHead == 4) && Mechanics
            .wouldWinHeadToHead(enemy, hero, 2)) {
          getHeadThreatSpear(enemy, 2).forEach(p -> dynObstacles[p.getX()][p.getY()] = true);
        }

        if (distanceToEnemyHead == 2 && !Mechanics.wouldWinHeadToHead(hero, enemy, 1)) {
          getHeadThreatSpear(enemy, 1).forEach(p -> dynObstacles[p.getX()][p.getY()] = true);
        }
      });

      return dynObstacles;
    });
  }

  int[][] getStaticDistances(Hero hero) {
    return staticDistances
        .computeIfAbsent(hero, h -> findStaticDistances(hero, hero.getDirection()));
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

      // APPLES - higher value in late game, higher value if I'm smaller
      boolean imLongest = getAliveActiveEnemies(hero).allMatch(enemy ->
          getTrueLength(enemy) < getTrueLength(hero) - 8);
      game.getApples().forEach(p ->
          values[p.getX()][p.getY()] =
              Mechanics.APPLE_REWARD + (Mechanics.isLateGame(game) ? 10 : 0) + (imLongest ? 0
                  : 10));

      // STONES - a little more complex
      game.getStones().forEach(s -> {
        boolean heroFury = hero.getFuryCount() > distances[s.getX()][s.getY()];
        boolean heroFly = hero.getFlyingCount() > distances[s.getX()][s.getY()];
        boolean heroLong =
            getTrueLength(hero) - Mechanics.STONE_LENGTH_PENALTY >= Mechanics.MIN_SNAKE_LENGTH;

        if (heroFly) {
          values[s.getX()][s.getY()] = 0;
        } else if (heroFury) {
          values[s.getX()][s.getY()] = Mechanics.STONE_REWARD;
        } else if (heroLong) {
          if (!Mechanics.isLateGame(game)) {
            // TODO: think when eating a stone with length is actually good
            if (hero.getStonesCount() < 5 && imLongest) {
              // values[s.getX()][s.getY()] = Mechanics.STONE_REWARD;
              values[s.getX()][s.getY()] = Mechanics.SOMEWHAT_NEGATIVE;
            } else {
              // for now consider eating stones with length bad
              values[s.getX()][s.getY()] = Mechanics.SOMEWHAT_NEGATIVE;
            }
          } else {
            // prefer not to eat stones late in the game
            values[s.getX()][s.getY()] = Mechanics.SOMEWHAT_NEGATIVE;
          }
        } else {
          values[s.getX()][s.getY()] = Mechanics.VERY_NEGATIVE;
        }
      });

      // LEAVE STONE AT TAIL
      Point tail = hero.getTailPoint();
      int roundsToTail = distances[tail.getX()][tail.getY()];
      if (hero.getFuryCount() > roundsToTail && hero.getStonesCount() > 0) {
        values[tail.getX()][tail.getY()] = Mechanics.STONE_REWARD;
      }

      // GUARANTEED KILLS - very useful
      getAliveActiveEnemies(hero).forEach(enemy -> enemy.body().forEach(p -> {
        int roundsToTarget = distances[p.getX()][p.getY()];

        switch (Mechanics.whatWillBeOnThisPoint(enemy, p, roundsToTarget)) {
          case Nothing:
            break;
          case Body:
            if (Mechanics.wouldWinHeadToBody(hero, enemy, roundsToTarget)) {
              int lengthToEat = Mechanics.getTrueBodyIndex(enemy, p) - roundsToTarget;
              values[p.getX()][p.getY()] = Mechanics.BLOOD_REWARD_PER_CELL * lengthToEat;
            } else if (Mechanics.wouldSurviveHeadToBody(hero, enemy, roundsToTarget)) {
              values[p.getX()][p.getY()] = 0;
            } else {
              values[p.getX()][p.getY()] = Mechanics.VERY_NEGATIVE;
            }
            break;
          case Neck:
            if (Mechanics.wouldWinHeadToHead(hero, enemy, roundsToTarget)) {
              int lengthToEat = Mechanics.getTrueLength(enemy);
              values[p.getX()][p.getY()] =
                  Mechanics.ROUND_REWARD + Mechanics.BLOOD_REWARD_PER_CELL * lengthToEat;
            }
        }
      }));

      // FURY - complex, very useful, TODO calculation
      game.getFuryPills().forEach(fpill -> {
        Hero closestHero = getDynamicClosestHero(fpill);
        if (isDynamicReachable(closestHero, fpill)) {
          if (hero == closestHero) {
            int killValue = getFuryPillKillValue(hero, fpill);
            int stoneValue = getFuryPillStoneValue(hero, fpill);
            values[fpill.getX()][fpill.getY()] += Math.max(killValue, stoneValue);
          } else {
            // mark area around as risky
            getThreatRadius(fpill, Mechanics.FURY_LENGTH)
                .forEach(p -> values[p.getX()][p.getY()] += Mechanics.SOMEWHAT_NEGATIVE);
          }
        }
      });

      // set 0 value to targets of other heroes if they are closer to them
      getAliveActiveEnemies(hero).forEach(enemy -> {
        Optional<Point> assumedTarget = getAssumedTarget(enemy);
        if (assumedTarget.isPresent()) {
          Point p = assumedTarget.get();
          if (values[p.getX()][p.getY()] > 0) {
            int distanceFromMe = getDynamicDistanceTo(hero, p);
            int distanceFromEnemy = getStaticDistanceTo(enemy, p);

            if (distanceFromEnemy < distanceFromMe
                || distanceFromEnemy == distanceFromMe
                && !Mechanics.wouldWinHeadToHead(hero, enemy, distanceFromEnemy)
                || distanceFromEnemy - distanceFromMe == 1
                && Mechanics.wouldWinHeadToHead(enemy, hero, distanceFromEnemy)) {
              values[p.getX()][p.getY()] = 0;
            } else if (Mechanics.wouldWinHeadToHead(hero, enemy, distanceFromEnemy)) {
              // bump value if we can actually kill an enemy this way or steal his valuable
              values[p.getX()][p.getY()] *= 2;
            }
          }
        }
      });

      // eat my body parts - negative value
      hero.body().forEach(p -> {
        int distanceToPart = getDynamicDistanceTo(hero, p);
        if (distanceToPart > 0 && !Mechanics.canFlyOver(hero, distanceToPart)) {
          int tailSizeOnCollision = 1 + Mechanics.getTrueBodyIndex(hero, p) - distanceToPart;
          if (tailSizeOnCollision > 0) {
            values[p.getX()][p.getY()] -= 10 * Mechanics.BLOOD_REWARD_PER_CELL * tailSizeOnCollision;
          }
        }
      });

      // FURY HEAD SPEAR - negative values
      getAliveActiveEnemies(hero).filter(e -> e.getFuryCount() > 1).forEach(e -> {
        if (!Mechanics.wouldWinHeadToHead(hero, e, e.getFuryCount() - 1)) {
          List<Point> threatSpear = getHeadThreatSpear(e, e.getFuryCount() - 1);
          threatSpear.forEach(p ->
          {
            // if I can reach this point before his fury runs out
            if (distances[p.getX()][p.getY()] < e.getFuryCount()) {
              // and I don't have fury as long as he does and won't win fury collision
              if (!Mechanics.wouldWinHeadToHead(hero, e, e.getFuryCount() - 1)) {
                // mark this point as bad
                values[p.getX()][p.getY()] += Mechanics.SOMEWHAT_NEGATIVE;
              }
            }
          });
        }
      });

      // straight line enemy interception
      Optional<MeetingPoint> interceptPoint = findClosestInterceptPoint(hero);
      if (interceptPoint.isPresent()) {
        Point p = interceptPoint.get().getPoint();
        Hero enemy = interceptPoint.get().getEnemy();
        values[p.getX()][p.getY()] +=
            (Mechanics.ROUND_REWARD + Mechanics.BLOOD_REWARD_PER_CELL * Mechanics
                .getTrueLength(enemy)) / 4; // divide by 4 because this is less likely then next one
      }

      // complex enemy interception
      Optional<MeetingPoint> smartIntercept = getClosestObjectInterception(hero);
      if (smartIntercept.isPresent()) {
        Point p = smartIntercept.get().getPoint();
        Hero enemy = smartIntercept.get().getEnemy();
        values[p.getX()][p.getY()] +=
            Mechanics.ROUND_REWARD + Mechanics.BLOOD_REWARD_PER_CELL * Mechanics
                .getTrueLength(enemy);
      }

      boolean lastEnemy = getAliveActiveHeroes().count() == 2;
      getAliveActiveEnemies(hero).forEach(enemy -> {
        Optional<Point> target = getAssumedTarget(enemy);
        if (target.isPresent()) {
          Point p = target.get();
          int myDistanceToTarget = getDynamicDistanceTo(hero, p);
          int hisDistanceToTarget = getStaticDistanceTo(enemy, p);

          if (hisDistanceToTarget < myDistanceToTarget && game.isFuryPill(p)) {
            return;
          }

          if (hisDistanceToTarget <= myDistanceToTarget) {
            if (Mechanics.wouldWinHeadToHead(hero, enemy, myDistanceToTarget) &&
                Mechanics.muchLonger(hero, enemy)) {
              values[p.getX()][p.getY()] +=
                  (Mechanics.ROUND_REWARD +
                      Mechanics.BLOOD_REWARD_PER_CELL * Mechanics.getTrueLength(enemy)) / 2;

              if (lastEnemy) values[p.getX()][p.getY()] *= 2;
            }
          }
        }
      });

      return values;
    });
  }

  private int getStaticDistanceTo(Hero hero, Point point) {
    int[][] distance = getStaticDistances(hero);
    return distance[point.getX()][point.getY()];
  }

  private boolean isDynamicReachable(Hero hero, Point point) {
    return getDynamicDistanceTo(hero, point) < Integer.MAX_VALUE;
  }

  private int getDynamicDistanceTo(Hero hero, Point point) {
    int[][] dynDistances = getDynamicDistances(hero);
    return dynDistances[point.getX()][point.getY()];
  }

  private int[][] findStaticDistances(Point point, Direction direction) {
    boolean[][] barriers = getBarriers();
    boolean[][] deadEnds = Algorithms.findDirectionalDeadEnds(barriers, point, direction);
    return Algorithms.findStaticDistances(deadEnds, point, direction);
  }

  private int[][] findStaticDistances(Point point) {
    return findStaticDistances(point, null);
  }

  Optional<MeetingPoint> getClosestObjectInterception(Hero hero) {
    Optional<MeetingPoint> bestIntercept = Optional.empty();

    for (Hero enemy : getAliveActiveEnemies(hero).collect(Collectors.toList())) {
      Optional<Point> intercept = interceptOnPathToTarget(hero, enemy);

      if (intercept.isPresent()) {
        if (bestIntercept.isEmpty() ||
            bestIntercept.isPresent() &&
                getDynamicDistanceTo(hero, intercept.get()) <
                    getDynamicDistanceTo(hero, bestIntercept.get().getPoint())) {
          bestIntercept = Optional.of(new MeetingPoint(intercept.get(), enemy));
        }
      }
    }

    return bestIntercept;
  }

  Optional<Point> interceptOnPathToTarget(Hero hero, Hero enemy) {
    Collection<Point> enemyPath = getChaseOptimisticPathToAssumedTarget(enemy, hero);
    Optional<Point> intercept = Optional.empty();

    if (!enemyPath.isEmpty()) {
      int bestInterceptDistance = Integer.MAX_VALUE;

      for (Point p : enemyPath) {
        int myDistance = getDynamicDistanceTo(hero, p);
        int enemyDistance = getStaticDistanceTo(enemy, p);
        int ticksToCollision = Math.max(myDistance, enemyDistance);

        if (myDistance > 0 && myDistance < bestInterceptDistance
            && enemyDistance > 0 && Math.abs(myDistance - enemyDistance) <= 1
            && Mechanics.wouldWinHeadToHead(hero, enemy, ticksToCollision)) {
          bestInterceptDistance = myDistance;
          intercept = Optional.of(p);
        }
      }
    }

    return intercept;
  }

  private Collection<Point> getChaseOptimisticPathToAssumedTarget(Hero hero, Hero chaser) {
    Optional<Point> assumedEnemyTarget = getAssumedTarget(hero);
    Stack<Point> result = new Stack<>();
    if (assumedEnemyTarget.isPresent()) {
      Point target = assumedEnemyTarget.get();
      int[][] distances = getStaticDistances(hero);
      int[][] chaserDist = getStaticDistances(chaser);

      Point head = hero.head();
      Point bestP = target;

      do {
        result.push(bestP);
        target = bestP;
        bestP = null;
        for (Direction d : Direction.onlyDirections()) {
          Point newP = target.copy();
          newP.change(d);

          if (distances[newP.getX()][newP.getY()] == distances[target.getX()][target.getY()] - 1) {
            if (newP.equals(head)) {
              return result;
            }

            if (bestP == null ||
                chaserDist[newP.getX()][newP.getY()] < chaserDist[bestP.getX()][bestP.getY()]) {
              bestP = newP;
            }
          }
        }
      } while (bestP != null);
    }

    return result;
  }

  // this assumes closeness calculation was done before
  private int getFuryPillKillValue(Hero hero, Point furyPill) {
    int[][] heroDistances = getDynamicDistances(hero);
    int distanceToPill = heroDistances[furyPill.getX()][furyPill.getY()];
    if (distanceToPill == Integer.MAX_VALUE) {
      return 0;
    }

    int[][] distancesFromPill = findStaticDistances(furyPill);
    int longestTailToEat = getAliveActiveEnemies(hero).flatMap(e -> e.body().stream())
        .mapToInt(t ->
            getEnemyTailLengthAt(hero, t, distanceToPill + distancesFromPill[t.getX()][t.getY()]))
        .max().orElse(0);

    // set 10 as base value if we're the closest
    return 10 + Mechanics.BLOOD_REWARD_PER_CELL * longestTailToEat;
  }

  // assumes closeness calculation was done before
  private int getFuryPillStoneValue(Hero hero, Point furyPill) {
    // grossly simplified
    int[][] heroDistances = getDynamicDistances(hero);
    int distanceToPill = heroDistances[furyPill.getX()][furyPill.getY()];
    if (distanceToPill == Integer.MAX_VALUE) {
      return 0;
    }

    int[][] distanceFromPill = findStaticDistances(furyPill);
    int shitValue = Math.min(hero.getStonesCount(), Mechanics.FURY_LENGTH - getTrueLength(hero));
    int surroundStones = (int) game.getStones().stream()
        .filter(s -> distanceFromPill[s.getX()][s.getY()] <= Mechanics.FURY_LENGTH / 2).count();
    int surroundValue = Math.min(3, surroundStones);

    // set 10 as base value if we're the closest
    return 10 + Mechanics.STONE_REWARD * Math.max(shitValue, surroundValue);
  }

  Direction getDirectionAtDestination(Hero hero, Point dest) {
    int[][] distances = getDynamicDistances(hero);
    int[][] acc = getAccumulatedValues(hero);

    Point bestP = null;
    Direction bestDirection = null;
    for (Direction d : Direction.onlyDirections()) {
      Point newP = dest.copy();
      newP.change(d);

      if (distances[newP.getX()][newP.getY()] == distances[dest.getX()][dest.getY()] - 1) {
        if (bestP == null || acc[newP.getX()][newP.getY()] > acc[bestP.getX()][bestP.getY()]) {
          bestP = newP;
          bestDirection = d.inverted();
        }
      }
    }

    return bestDirection;
  }

  private int getEnemyTailLengthAt(Hero excludeHero, Point point, int inRounds) {
    return getAliveActiveEnemies(excludeHero).filter(e -> e.getFlyingCount() <= inRounds)
        .mapToInt(e -> Mechanics.getTrueBodyIndex(e, point) - inRounds).max().orElse(0);
  }

  double[][] getDistanceAdjustedValues(Hero hero) {
    return distanceAdjustedValues.computeIfAbsent(hero, h ->
        makeDynamicDistanceAdjustment(hero, getValues(hero)));
  }

  int[][] getAccumulatedValues(Hero hero) {
    return accumulatedValues.computeIfAbsent(hero,
        h -> Algorithms.findAccumulatedValues(getDynamicDistances(hero), getValues(hero)));
  }

  // assumption that enemy is dumb and moves to his closest (by static distance) value target
  Optional<Point> getAssumedTarget(Hero hero) {
    return assumedTarget.computeIfAbsent(hero, h -> {
      Stream<Point> valuables = getValuableObjects(hero);
      return valuables.min(Comparator.comparingInt(p -> getStaticDistanceTo(hero, p)));
    });
  }

  private Stream<Point> getValuableObjects(Hero hero) {
    // for now only think about apples, gold and fury pills
    return Stream.concat(game.getGold().stream(),
        Stream.concat(game.getApples().stream(), game.getFuryPills().stream()));
  }

  double[][] getAccumulatedDistanceAdjustedValues(Hero hero) {
    return accumulatedDistanceAdjustedValues.computeIfAbsent(hero,
        h -> makeDynamicDistanceAdjustment(hero, getAccumulatedValues(hero)));
  }

  private double[][] makeDynamicDistanceAdjustment(Hero hero, int[][] values) {
    double[][] result = new double[game.size()][game.size()];
    int[][] distances = getDynamicDistances(hero);

    for (int x = 0; x < game.size(); x++) {
      for (int y = 0; y < game.size(); y++) {
        if (distances[x][y] != 0) {
          result[x][y] = values[x][y] / (double) distances[x][y];
        }
      }
    }

    return result;
  }

  private Optional<MeetingPoint> findClosestInterceptPoint(Hero hero) {
    List<MeetingPoint> meetingPoints = new ArrayList<>();
    int[][] heroDistances = getDynamicDistances(hero);

    getAliveActiveEnemies(hero).forEach(enemy -> {
      Direction enemyDirection = enemy.getDirection();
      Point meetingPoint = enemy.head().copy();
      int distanceFromEnemy = 0;

      while (!meetingPoint.isOutOf(game.size()) && distanceFromEnemy < 10) {
        meetingPoint.change(enemyDirection);
        ++distanceFromEnemy;
        int distanceFromHero = heroDistances[meetingPoint.getX()][meetingPoint.getY()];

        if (game.isBarrier(meetingPoint)) {
          break; // stop path projection
        }

        if (Math.abs(distanceFromEnemy - distanceFromHero) <= 1) {
          if (Mechanics
              .wouldWinHeadToHead(hero, enemy, Math.max(distanceFromEnemy, distanceFromHero))) {
            meetingPoints.add(new MeetingPoint(meetingPoint, enemy));
            break;
          }
        }
      }
    });

    return meetingPoints.stream().min(
        Comparator.comparingInt(p -> heroDistances[p.getPoint().getX()][p.getPoint().getY()]));
  }

  protected Stream<Point> getBarrierObjects() {
    return Stream.concat(game.getWalls().stream(), game.getStarts().stream());
  }

  protected Stream<Hero> getAliveActiveHeroes() {
    return game.getHeroes().stream().filter(h -> h.isActive() && h.isAlive());
  }

  protected Stream<Hero> getAliveActiveEnemies(Hero hero) {
    return getAliveActiveHeroes().filter(h -> h != hero);
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

  private int getTrueLength(Hero hero) {
    return hero.size() + hero.getGrowBy();
  }

  private Hero getClosestHeadToHeadEnemy(Hero hero) {
    return getAliveActiveEnemies(hero).min(new HeroDistanceComparator(hero.head())).orElse(null);
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
      int dist1 = getDynamicDistanceTo(h1, point);
      int dist2 = getDynamicDistanceTo(h2, point);
      int cmp = Integer.compare(dist1, dist2);

      if (cmp == 0) {
        cmp = Mechanics.wouldSurviveHeadToHead(h1, h2, dist1) ? -1 : 1;
      }

      return cmp;
    }).orElse(null);
  }

  private List<Point> getHeadThreatSpear(Hero hero, int radius) {
    int[][] distances = getStaticDistances(hero);
    List<Point> result = new ArrayList<>();
    for (int x = 0; x < game.size(); x++) {
      for (int y = 0; y < game.size(); y++) {
        if (distances[x][y] <= radius) {
          result.add(PointImpl.pt(x, y));
        }
      }
    }

    return result;
  }

  private List<Point> getThreatRadius(Point point, int radius) {
    int[][] distances = Algorithms.findStaticDistances(getBarriers(), point, null);
    List<Point> result = new ArrayList<>();
    for (int x = 0; x < game.size(); x++) {
      for (int y = 0; y < game.size(); y++) {
        if (distances[x][y] <= radius) {
          result.add(PointImpl.pt(x, y));
        }
      }
    }

    return result;
  }

  private void printHeroAnalytics() {
    for (int i = 0; i < game.getHeroes().size(); i++) {
      Hero hero = game.getHeroes().get(i);
      System.out
          .printf(
              "Hero[%d] head: [%d %d] (dir = %s, length = %d, stones = %d, fury = %d, flight = %d, alive = %s, active = %s)\n",
              i,
              hero.head().getX(), hero.head().getY(),
              hero.getDirection(), Mechanics.getTrueLength(hero), hero.getStonesCount(),
              hero.getFuryCount(), hero.getFlyingCount(),
              hero.isAlive(), hero.isActive());
    }
  }

  private Direction findFirstStepTo(Hero hero, Point target) {
    int[][] distances = getDynamicDistances(hero);
    int[][] acc = getAccumulatedValues(hero);

    Point head = hero.head();
    Point bestP = target;

    do {
      target = bestP;
      bestP = null;
      for (Direction d : Direction.onlyDirections()) {
        Point newP = target.copy();
        newP.change(d);

        if (distances[newP.getX()][newP.getY()] == distances[target.getX()][target.getY()] - 1) {
          if (newP.equals(head)) {
            return d.inverted();
          }

          if (bestP == null || acc[newP.getX()][newP.getY()] > acc[bestP.getX()][bestP.getY()]) {
            bestP = newP;
          }
        }
      }
    } while (bestP != null);

    System.out.print("FAIL: ended up in deadend or could not find legit path.");
    return hero.getDirection();
  }

  private Point findMaxPoint(Hero hero) {
    Point p = null;

    double[][] distanceAdjustedValues = getAccumulatedDistanceAdjustedValues(hero);
    int[][] distances = getDynamicDistances(hero);
    for (int x = 0; x < distanceAdjustedValues.length; x++) {
      for (int y = 0; y < distanceAdjustedValues.length; y++) {
        if (distances[x][y] != 0 && distances[x][y] < Integer.MAX_VALUE) {
          if (p == null ||
              distanceAdjustedValues[x][y] > distanceAdjustedValues[p.getX()][p.getY()]) {
            p = PointImpl.pt(x, y);
          }
        }
      }
    }

    return p;
  }

  // closest - first
  private class HeroDistanceComparator implements Comparator<Hero> {

    private final int x;
    private final int y;

    public HeroDistanceComparator(Point pointForDistanceComparison) {
      x = pointForDistanceComparison.getX();
      y = pointForDistanceComparison.getY();
    }

    @Override
    public int compare(Hero o1, Hero o2) {
      int[][] d1 = getDynamicDistances(o1);
      int[][] d2 = getDynamicDistances(o2);

      int cmp = Integer.compare(d1[x][y], d2[x][y]);

      if (cmp == 0) {
        cmp = new HeroStrengthComparator(d1[x][y]).compare(o1, o2);
      }

      return cmp;
    }
  }

  // stronger - first, assume head-to-head collision
  private class HeroStrengthComparator implements Comparator<Hero> {

    private final int ticksToCollision;

    public HeroStrengthComparator(int ticksToCollision) {
      this.ticksToCollision = ticksToCollision;
    }

    @Override
    public int compare(Hero o1, Hero o2) {
      boolean o1Fury = o1.getFuryCount() > ticksToCollision;
      boolean o2Fury = o2.getFuryCount() > ticksToCollision;

      int cmp = 0;

      if (o1Fury && !o2Fury) {
        cmp = -1;
      } else if (!o1Fury && o2Fury) {
        cmp = 1;
      }

      if (cmp == 0) {
        cmp = Integer.compare(getTrueLength(o2), getTrueLength(o1));
      }

      return cmp;
    }
  }
}
