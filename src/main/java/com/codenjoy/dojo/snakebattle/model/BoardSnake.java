package com.codenjoy.dojo.snakebattle.model;

import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_EVIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.ENEMY_HEAD_FLY;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_EVIL;
import static com.codenjoy.dojo.snakebattle.model.Elements.HEAD_FLY;
import static com.codenjoy.dojo.snakebattle.model.Elements.MY_HEAD;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.client.Board;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Snake as returned by board, no assumptions in case of overlays (for now).
 */
@Data
public class BoardSnake {

  private final boolean isMe;
  private final Direction direction;
  private final boolean isFurious;
  private final boolean isFlying;
  // head-first order
  private final List<Point> parts;

  public static BoardSnake identifyFromHead(Point head, Board board) {
    Elements headElement = board.getAt(head.getX(), head.getY());

    boolean isMe;
    if (MY_HEAD.contains(headElement)) {
      isMe = true;
    } else if (ENEMY_HEAD.contains(headElement)) {
      isMe = false;
    } else {
      System.out.printf("ERROR: head point is not a snake head");
      return null;
    }

    boolean isFurious = headElement == HEAD_EVIL || headElement == ENEMY_HEAD_EVIL;
    boolean isFlying = headElement == HEAD_FLY || headElement == ENEMY_HEAD_FLY;
    List<Point> parts = new ArrayList<>();
    discoverSnakeParts(head, null, board, parts);

    if (parts.size() == 0) {
      return null;
    }

    Direction direction = detectDirection(parts, board);
    if (direction == null) {
      return null;
    }

    return new BoardSnake(isMe, direction, isFurious, isFlying, parts);
  }

  private static void discoverSnakeParts(Point currentPoint, Direction lastDirection, Board board,
      List<Point> parts) {
    Elements currentElement = board.getAt(currentPoint);
    boolean foundCompatible = false;
    for (Direction nextDirection : currentElement.compatibleDirections()) {
      if (lastDirection != null && !nextDirection.equals(lastDirection.inverted())) {
        Point nextPoint = currentPoint.copy();
        nextPoint.change(nextDirection);
        if (board.containsPoint(nextPoint)) {
          Elements nextElement = board.getAt(nextPoint);
          if (currentElement.isCompatible(nextDirection, nextElement)) {
            if (!foundCompatible) {
              parts.add(nextPoint);
              discoverSnakeParts(nextPoint, nextDirection, board, parts);
              foundCompatible = true;
            } else {
              System.out.printf("ERROR: Found more than one compatible snake part for %d,%d",
                  currentPoint.getX(), currentPoint.getY());
            }
          }
        }
      }
    }

    if (foundCompatible == false && !currentElement.isTail()) {
      System.out.printf("WARNING: abrupt snake ending at %d,%d", currentPoint.getX(),
          currentPoint.getY());
    }
  }

  private static Direction detectDirection(List<Point> parts, Board board) {
    Point head = parts.get(0);
    if (board.getAt(head).compatibleDirections().size() == 1) {
      return board.getAt(head).compatibleDirections().get(0).inverted();
    }

    Point neck = parts.get(1);
    for (Direction d : board.getAt(neck).compatibleDirections()) {
      Point couldBeHead = neck.copy();
      couldBeHead.change(d);
      if (couldBeHead.equals(head)) {
        return d;
      }
    }

    System.out.printf("ERROR: failed to detect head direction at %d,%d",
        parts.get(0).getX(), parts.get(0).getY());
    return null;
  }

  public int size() {
    return parts.size();
  }

  public int distanceFromTail(Point p) {
    int index = parts.indexOf(p);
    if (index < 0) {
      return index;
    } else {
      return parts.size() - index - 1;
    }
  }
}
