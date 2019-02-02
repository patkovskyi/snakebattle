package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.codenjoy.dojo.snakebattle.model.Snake;
import java.util.Collection;
import java.util.HashSet;

public class Game {
  private boolean hasRoundStarted;
  private boolean areWeAlive;
  private Collection<Point> gold;
  private Collection<Point> apples;
  private Collection<Point> stones;
  private Collection<Point> furyPills;
  private Collection<Point> flyingPills;
  private Collection<Snake> snakes;

  public Game() {
    gold = new HashSet<>();
    apples = new HashSet<>();
    stones = new HashSet<>();
    furyPills = new HashSet<>();
    flyingPills = new HashSet<>();
    snakes = new HashSet<>();
  }

  public void update(LightBoard updatedBoard) {
    updateMapObjects(updatedBoard);
  }

  private void updateMapObjects(LightBoard updatedBoard) {
    gold = updatedBoard.get(Elements.GOLD);
    apples = updatedBoard.get(Elements.APPLE);

    // TODO: think about snake-flying-over-stone situation
    stones = updatedBoard.get(Elements.STONE);
    for (Point stone : stones) {

    }

    furyPills = updatedBoard.get(Elements.FURY_PILL);
    flyingPills = updatedBoard.get(Elements.FLYING_PILL);
  }
}
