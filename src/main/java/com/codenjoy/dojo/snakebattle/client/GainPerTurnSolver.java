package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.client.Solver;

public class GainPerTurnSolver implements Solver<Board> {

  private Game game;

  public GainPerTurnSolver() {
    game = new Game();
  }

  @Override
  public String get(Board board) {
    long startTime = System.currentTimeMillis();
    String answer = "";
    game.updateFromBoard(board);
    if (game.isAlive()) {
      // find best answer
      answer = "SOMETHING";
    }

    System.out.printf("Finding solution took %d ms\n", System.currentTimeMillis() - startTime);
    return answer;
  }
}
