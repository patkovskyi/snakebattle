package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.DynamicObstacle;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;

public class Mechanics {

  public static final int MIN_SNAKE_LENGTH = 2;
  public static int APPLE_REWARD = 1;
  public static int GOLD_REWARD = 10;
  public static int STONE_REWARD = 5;
  public static int ROUND_REWARD = 50;
  public static int STONE_LENGTH_PENALTY = 3;
  public static int BLOOD_REWARD_PER_CELL = 10;
  public static int FURY_LENGTH = 9;

  // TODO: think about these dummy values
  public static int SOMEWHAT_NEGATIVE = -42;
  public static int VERY_NEGATIVE = -424242;

  static int getTrueBodyIndex(Hero hero, Point point) {
    return hero.getBodyIndex(point) + hero.getGrowBy();
  }

  static int getTrueLength(Hero hero) {
    return hero.size() + hero.getGrowBy();
  }

  static boolean canPassStone(Hero hero, int ticksToStone) {
    boolean heroFly = hero.getFlyingCount() > ticksToStone;
    return heroFly || canEatStone(hero, ticksToStone);
  }

  static boolean canEatStone(Hero hero, int ticksToStone) {
    boolean heroFury = hero.getFuryCount() > ticksToStone;
    boolean heroLongEnough = getTrueLength(hero) - STONE_LENGTH_PENALTY >= MIN_SNAKE_LENGTH;

    return heroFury || heroLongEnough;
  }

  static boolean canFlyOver(Hero hero, int ticksToCollision) {
    return hero.getFlyingCount() > ticksToCollision;
  }

  static boolean wouldSurviveHeadToHead(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroFly = hero.getFlyingCount() > ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() > ticksToCollision;

    return heroFly || enemyFly || wouldWinHeadToHead(hero, enemy, ticksToCollision);
  }

  static boolean wouldWinHeadToHead(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroLonger = getTrueLength(hero) >= getTrueLength(enemy) + MIN_SNAKE_LENGTH;
    boolean heroFury = hero.getFuryCount() > ticksToCollision;
    boolean enemyFury = enemy.getFuryCount() > ticksToCollision;

    // what if enemy is flying but I am not? it's not a 'win' actually
    return heroFury && !enemyFury || heroFury == enemyFury && heroLonger;
  }

  static boolean wouldSurviveHeadToBody(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroFly = hero.getFlyingCount() > ticksToCollision;
    boolean enemyFly = enemy.getFlyingCount() > ticksToCollision;

    return heroFly || enemyFly || wouldWinHeadToBody(hero, enemy, ticksToCollision);
  }

  static boolean wouldWinHeadToBody(Hero hero, Hero enemy, int ticksToCollision) {
    boolean heroFury = hero.getFuryCount() > ticksToCollision;
    return heroFury;
  }

  static DynamicObstacle whatWillBeOnThisPoint(Hero hero, Point point, int inRounds) {
    int trueBodyIndex = getTrueBodyIndex(hero, point) - inRounds;
    if (trueBodyIndex == -1) {
      return DynamicObstacle.PossibleStone;
    }

    if (trueBodyIndex < -1) {
      return DynamicObstacle.Nothing;
    }

    int trueDistanceFromHead = getTrueLength(hero) - 1 - trueBodyIndex;

    // TODO: this place assumes head == neck
    if (trueDistanceFromHead <= 1) {
      return DynamicObstacle.Neck;
    } else {
      return DynamicObstacle.Body;
    }
  }

  static boolean enemyShorterByMinSnakeLength(Hero hero, Hero enemy) {
    return getTrueLength(hero) - MIN_SNAKE_LENGTH >= getTrueLength(enemy);
  }

  static boolean isLateGame(SnakeBoard game) {
    return GameHelper.getTick(game) >= 100;
  }

  static boolean muchLonger(Hero hero, Hero enemy) {
    return getTrueLength(hero) - 5 >= getTrueLength(enemy);
  }
}
