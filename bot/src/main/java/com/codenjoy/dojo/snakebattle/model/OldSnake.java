package com.codenjoy.dojo.snakebattle.model;

import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_BODY;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_EVIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_SNAKE;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_TAIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_EVIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_BODY;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_HEAD;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_SNAKE;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_TAIL;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.client.ClosestBestBoard;
import java.util.Set;
import java.util.Stack;
import lombok.Data;

@Data
public class OldSnake {
  private final boolean isMe;
  private final int length;
  private final Direction direction;
  private final boolean isFurious;

  // any part of the body should work
  public static OldSnake identify(int x, int y, ClosestBestBoard board) {
    Elements element = board.getAt(x, y);

    boolean isMe;
    Set<Elements> headElements, bodyElements, tailElements;
    if (MY_SNAKE.contains(element)) {
      headElements = MY_HEAD;
      bodyElements = MY_BODY;
      tailElements = MY_TAIL;
      isMe = true;
    } else if (ENEMY_SNAKE.contains(element)) {
      headElements = ENEMY_HEAD;
      bodyElements = ENEMY_BODY;
      tailElements = ENEMY_TAIL;
      isMe = false;
    } else {
      // not a snake
      return null;
    }

    int length = 0;
    Direction direction = null;
    boolean isFurious = false;
    Stack<Point> q = new Stack<>();
    q.add(new PointImpl(x, y));
    boolean[][] visited = new boolean[board.size()][board.size()];
    while (!q.isEmpty()) {
      Point p = q.pop();
      ++length;
      Elements e = board.getAt(p);
      visited[p.getX()][p.getY()] = true;

      if (e == HEAD_EVIL || e == ENEMY_HEAD_EVIL) {
        isFurious = true;
      }

      // TODO: fix this hack for identifying direction
      if (headElements.contains(e)) {
        direction = e.compatibleDirections().get(0).inverted();
      }

      int compatibleCounter = 0;
      for (Direction d : Direction.onlyDirections()) {
        Point n = p.copy();
        n.change(d);
        if (board.isWithinBoard(n) && !visited[n.getX()][n.getY()]) {
          Elements ne = board.getAt(n);
          if (e.isCompatible(d, ne)) {
            ++compatibleCounter;
            q.add(n);

            // identify direction by looking at head (ne) and 'neck' (e)
            if (headElements.contains(ne)) {
              direction = d;
            }

            // identify direction by looking at head (e) and 'neck' (ne)
            if (headElements.contains(e)) {
              direction = d.inverted();
            }
          }
        }
      }

      if ((compatibleCounter == 0 || length == 1 && compatibleCounter == 1)
          && !headElements.contains(e)
          && !tailElements.contains(e))
        throw new IllegalStateException(
            "identify: abrupt snake ending at " + p.getX() + " " + p.getY());

      if (compatibleCounter > 1 && !(length == 1 && bodyElements.contains(e)))
        throw new IllegalStateException(
            "identify: more than one compatibleDirections snake parts found for point at "
                + p.getX()
                + " "
                + p.getY());
    }

    if (direction == null) {
      throw new IllegalStateException("identify: could not identify direction");
    }

    return new OldSnake(isMe, length, direction, isFurious);
  }
}
