package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import java.util.Collection;
import lombok.Getter;

public class LightBoard extends AbstractBoard<Elements> {
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

  @Override
  public Elements valueOf(char ch) {
    return Elements.valueOf(ch);
  }

  @Override
  public LightBoard forString(String boardString) {
    super.forString(boardString);

    gold = get(Elements.GOLD);
    apples = get(Elements.APPLE);
    stones = get(Elements.STONE);
    furyPills = get(Elements.FURY_PILL);
    flyingPills = get(Elements.FLYING_PILL);
    return this;
  }

}
