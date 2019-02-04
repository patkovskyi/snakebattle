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
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.services.settings.SimpleParameter;
import com.codenjoy.dojo.snakebattle.model.Player;
import com.codenjoy.dojo.snakebattle.model.board.SnakeBoard;
import com.codenjoy.dojo.snakebattle.model.board.Timer;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import com.codenjoy.dojo.snakebattle.model.level.LevelImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmulatingSolver implements Solver<Board> {
  private final Random random = new Random();
  private Hero hero;
  private List<Hero> enemies;

  public EmulatingSolver() {}

  @Override
  public String get(Board board) {
    SnakeBoard snakeBoardFromServer = getSnakeBoard(board.boardAsString());
    if (hero != null) {
      System.out.printf("Hero: %s\n", hero.toString());
    }

    for (int i = 0; i < enemies.size(); i++) {
      System.out.printf("Enemy %d: %s\n", i, enemies.get(i).toString());
    }

    return getRandomDirection().toString();
  }

  private SnakeBoard getSnakeBoard(String boardString) {
    LevelImpl level = new LevelImpl(boardString);
    SnakeBoard game =
        new SnakeBoard(
            level,
            new RandomDice(),
            new Timer(new SimpleParameter<>(0)),
            new Timer(new SimpleParameter<>(300)),
            new SimpleParameter<>(5),
            new SimpleParameter<>(10),
            new SimpleParameter<>(10),
            new SimpleParameter<>(3));

    Hero hero = level.getHero();
    if (hero != null) {
      hero.setActive(true);
      Player heroPlayer =
          new Player(event -> System.out.printf("Hero event: %s\n", event.toString()));
      game.newGame(heroPlayer);
      heroPlayer.setHero(hero);
      hero.init(game);
      this.hero = game.getHeroes().get(0);
    }

    List<Hero> enemies = level.getEnemies();
    this.enemies = new ArrayList<>();
    for (int i = 0; i < enemies.size(); i++) {
      Hero enemy = enemies.get(i);
      enemy.setActive(true);
      final int j = i + 1;
      Player enemyPlayer =
          new Player(event -> System.out.printf("Enemy %d event: %s\n", j, event.toString()));
      game.newGame(enemyPlayer);
      enemyPlayer.setHero(enemy);
      enemy.init(game);
      this.enemies.add(game.getHeroes().get(j));
    }

    return game;
  }

  private Direction getRandomDirection() {
    return Direction.onlyDirections().get(random.nextInt(4));
  }
}
