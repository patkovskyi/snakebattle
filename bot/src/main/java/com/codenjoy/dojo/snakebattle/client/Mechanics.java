package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.DynamicObstacle;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;

public class Mechanics {

  public static final int MIN_SNAKE_LENGTH = 2;
  public static int APPLE_REWARD = 1;
  public static int GOLD_REWARD = 10;
  public static int STONE_REWARD = 5;
  public static int STONE_LENGTH_PENALTY = 3;
  public static int BLOOD_REWARD_PER_CELL = 10;

  static boolean wouldSurviveHeadToHead(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroLonger = hero.size() + hero.getGrowBy() >= enemy.size() + enemy.getGrowBy()
        + MIN_SNAKE_LENGTH;

    boolean heroFury = hero.getFuryCount() >= ticksToCollision;
    boolean enemyFury = enemy.getFuryCount() >= ticksToCollision;
    boolean heroFly = hero.getFlyingCount() >= ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() >= ticksToCollision;

    return heroFly || enemyFly || heroFury && !enemyFury || heroFury == enemyFury && heroLonger;
  }

  static boolean wouldSurviveHeadToBody(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroFury = hero.getFuryCount() >= ticksToCollision;
    boolean heroFly = hero.getFlyingCount() >= ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() >= ticksToCollision;

    return heroFury || heroFly || enemyFly;
  }

  static DynamicObstacle whatWillBeOnThisPoint(Hero hero, Point point, int inRounds) {
    int trueBodyIndex = Math.max(-1, hero.getBodyIndex(point) + hero.getGrowBy() - inRounds);
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
}
