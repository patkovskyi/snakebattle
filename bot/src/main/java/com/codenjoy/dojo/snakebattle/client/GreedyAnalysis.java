package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.snakebattle.model.HeroAction;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;

public class GreedyAnalysis extends Analysis {

  protected GreedyAnalysis(SnakeBoard game) {
    super(game);
  }

  @Override
  public HeroAction findBestAction() {
    return null;
  }
}
