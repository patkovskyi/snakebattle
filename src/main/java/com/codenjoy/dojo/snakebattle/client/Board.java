package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import java.util.Collection;
import lombok.Getter;

/**
 * This class simply captures string representation of the game board. It does not make any
 * assumptions and is not stateful (does not track previous states in any way).
 */
public class Board extends AbstractBoard<Elements> {

  @Getter
  private Collection<Point> gold;

  @Getter
  private Collection<Point> apples;

  @Getter
  private Collection<Point> stones;

  @Getter
  private Collection<Point> furyPills;

  @Getter
  private Collection<Point> flyingPills;

  @Getter
  private Collection<Point> enemySnakeHeads;

  @Override
  public Elements valueOf(char ch) {
    return Elements.valueOf(ch);
  }

  @Override
  protected int inversionY(int y) {
    return size - 1 - y;
  }

  @Override
  public Board forString(String boardString) {
    super.forString(boardString);

    gold = get(Elements.GOLD);
    apples = get(Elements.APPLE);
    stones = get(Elements.STONE);
    furyPills = get(Elements.FURY_PILL);
    flyingPills = get(Elements.FLYING_PILL);
    enemySnakeHeads = get(Elements.ENEMY_HEAD.toArray(new Elements[0]));
    return this;
  }

  public boolean containsPoint(Point p) {
    return !p.isOutOf(size);
  }
}
