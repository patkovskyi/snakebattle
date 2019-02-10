package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.HeroAction;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.Random;

public class RandomAnalysis extends Analysis {

  private final Random random = new Random();

  protected RandomAnalysis(SnakeBoard game) {
    super(game);
  }

  @Override
  public HeroAction findBestAction() {
    return HeroAction.valueOf(getRandomDirection().value());
  }

  private Direction getRandomDirection() {
    if (game != null) {
      Hero me = game.getHeroes().get(0);
      for (int i = 0; i < 3; i++) {
        Direction newDirection = me.getRelativeDirection(i);
        Point newHead = me.head().copy();
        newHead.change(newDirection);

        if (!game.isBarrier(newHead)) {
          return newDirection;
        }
      }
    }

    return Direction.onlyDirections().get(random.nextInt(4));
  }
}
