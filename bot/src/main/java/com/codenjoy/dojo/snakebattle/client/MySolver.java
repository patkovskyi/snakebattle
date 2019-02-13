package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import java.util.List;
import java.util.stream.Collectors;

public class MySolver implements Solver<MyBoard> {

  private SnakeBoard game;

  private static void printRoundStatus(SnakeBoard game) {
    System.out.printf("TICK %d\n", GameHelper.getTick(game));
    Hero me = game.getHeroes().get(0);
    List<Hero> heroesOnBoard =
        game.getHeroes().stream().filter(h -> isOnBoard(h, game)).collect(Collectors.toList());

    boolean meDead = !me.isAlive();
    boolean allDead = heroesOnBoard.stream().allMatch(h -> !h.isAlive());
    boolean imTheOnlyOnBoard = heroesOnBoard.size() == 1 && heroesOnBoard.contains(me);
    boolean imTheLongest = heroesOnBoard.stream().allMatch(h -> h.size() <= me.size());

    if (meDead) {
      if (allDead) {
        if (imTheOnlyOnBoard) {
          System.out.println("ROUND WON (I'm the last survivor)");
        } else if (imTheLongest) {
          System.out.println("ROUND WON (I'm the longest at timeout)");
        } else {
          System.out.println("ROUND LOST (I'm not the longest at timeout)");
        }
      } else {
        System.out.println("ROUND LOST (I died, someone survived)");
      }
    }
  }

  private static boolean isOnBoard(Hero hero, SnakeBoard game) {
    return !hero.isOutOf(game.size());
  }

  @Override
  public String get(MyBoard boardFromServer) {
    if (game == null || boardFromServer.isNewRound()) {
      // initialize new game
      game = GameHelper.initializeGame(boardFromServer);
    } else {
      game = GameHelper.continueGame(game, boardFromServer);

      if (game == null) {
        // re-sync required :(
        game = GameHelper.initializeGame(boardFromServer);
      }
    }

    printRoundStatus(game);

    if (aliveAndActive()) {
      Analysis analysis = new Analysis(game);
      return analysis.findBestAction().getStr();
    } else {
      return "right";
    }
  }

  private boolean aliveAndActive() {
    return
        game.getHeroes().size() > 0 &&
            game.getHeroes().get(0).isAlive() && game.getHeroes().get(0).isActive();
  }
}
