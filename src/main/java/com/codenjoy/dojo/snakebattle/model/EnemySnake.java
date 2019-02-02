package com.codenjoy.dojo.snakebattle.model;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.client.Board;

public class EnemySnake {

  /**
   * Detected length, not always true.
   */
  private int length;
  private Direction direction;
  private boolean isFurious;
  private boolean isFlying;

  /**
   * For now we're conservative and aren't making many assumptions.
   * If we can't positively identify a snake, we return null.
   */
  public static EnemySnake identifyFromHead(Point head, Board board) {
    return null;
  }
}
