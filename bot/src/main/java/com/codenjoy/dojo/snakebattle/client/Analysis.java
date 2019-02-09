package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.stream.Stream;

public class Analysis {

  private final SnakeBoard game;

  private Analysis(SnakeBoard game) {
    this.game = game;
  }

  public static Analysis create(SnakeBoard game, Hero hero) {
    // TODO: check if game is in valid state and hero is alive & active ?
    return new Analysis(game);
  }

  public boolean[][] getStaticDeadEnds() {
    boolean[][] barriers = new boolean[game.size()][game.size()];
    getBarriers().forEach(b -> barriers[b.getX()][b.getY()] = true);
    return Algorithms.findStaticDeadEnds(barriers);
  }

  private Stream<Point> getBarriers() {
    return Stream.concat(game.getWalls().stream(), game.getStarts().stream());
  }

  private Hero getHero() {
    return game.getHeroes().get(0);
  }
}
