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
    game.updateFromBoard(board);
    String answer = findBestAction();
    System.out.printf("Finding answer took %d ms\n", System.currentTimeMillis() - startTime);
    return answer;
  }

  private String findBestAction() {
    String answer = "";
    if (game.isAlive()) {
      // find best answer
      return "SOMETHING";
    }

    return answer;
  }
}
