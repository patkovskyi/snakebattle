package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.HeroAction;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;

public class GreedyAnalysis extends Analysis {

  protected GreedyAnalysis(SnakeBoard game) {
    super(game);
  }

  @Override
  public HeroAction findBestAction() {
    double[][] distanceAdjustedValues = getClosestAdjustedValues(getMyHero());
    Point target = findMaxPoint(distanceAdjustedValues);

    String targetType = getTargetPointType(target);
    int value = getValues(getMyHero())[target.getX()][target.getY()];
    int distance = getDynamicDistances(getMyHero())[target.getX()][target.getY()];

    System.out.printf("Heading to: [%d %d] (%s | value = %d | distance = %d)\n",
        target.getX(), target.getY(), targetType, value, distance);

    System.out
        .printf("Hero head: [%d %d] (dir = %s, length = %d, alive = %s)\n",
            getMyHero().head().getX(), getMyHero().head().getY(),
            getMyHero().getDirection(), Mechanics.getTrueLength(getMyHero()),
            getMyHero().isAlive());

    if (getMyHero().isAlive()) {
      int addAction = 0;
      if (target.equals(getMyHero().getTailPoint())) {
        addAction = 4;
        getMyHero().reduceStoneCount();
      }

      return HeroAction.valueOf(addAction + findFirstStepTo(getMyHero(), target).value());
    } else {
      return HeroAction.valueOf(getMyHero().getDirection().value());
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
    } while (!bestP.equals(head));

    throw new IllegalStateException("findFirstStepTo should never reach this line");
  }

  private Point findMaxPoint(double[][] distanceAdjustedValues) {
    Point p = null;

    int[][] distances = getDynamicDistances(getMyHero());

    for (int x = 0; x < distanceAdjustedValues.length; x++) {
      for (int y = 0; y < distanceAdjustedValues.length; y++) {
        if (distances[x][y] < Integer.MAX_VALUE) {
          if (p == null || distanceAdjustedValues[x][y] > distanceAdjustedValues[p.getX()][p
              .getY()]) {
            p = PointImpl.pt(x, y);
          }
        }
      }
    }

    return p;
  }
}
