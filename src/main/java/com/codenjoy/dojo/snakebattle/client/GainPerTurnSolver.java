package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Direction;

public class GainPerTurnSolver implements Solver<LightBoard> {

  @Override
  public String get(LightBoard board) {
    System.out.println("Number of apples: " + board.getApples().size());
    return Direction.random().toString();
  }
}
