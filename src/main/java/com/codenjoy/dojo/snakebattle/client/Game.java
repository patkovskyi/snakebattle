package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.codenjoy.dojo.snakebattle.model.EnemySnake;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

public class Game {

  @Getter
  private boolean alive;

  @Getter
  private boolean roundLost;

  @Getter
  private int ticks;

  @Getter
  private Point myHead;

  @Getter
  private int myFuryCount;

  @Getter
  private int myFlyingCount;

  @Getter
  private int myStoneCount;

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
  private Collection<EnemySnake> enemySnakes;

  private Board board;
  // expectations for next step, help to detect board-out-of-sync scenarios
  private Point myExpectedHead;
  private Direction myExpectedDirection;

  public Game() {
    gold = new HashSet<>();
    apples = new HashSet<>();
    stones = new HashSet<>();
    furyPills = new HashSet<>();
    flyingPills = new HashSet<>();
    enemySnakes = new HashSet<>();
  }

  public void registerMyMove(Direction direction, boolean leaveStone) {
    if (!Direction.onlyDirections().contains(direction)) {
      throw new IllegalArgumentException(
          "First argument must be a direction, but was " + direction.toString());
    }

    if (leaveStone && myStoneCount > 0) {
      --myStoneCount;
    }

    myExpectedDirection = direction;
    myExpectedHead = myHead.copy();
    myExpectedHead.change(direction);
  }

  public void updateFromBoard(Board board) {
    this.board = board;
    updateMyself();
    updateRoundState();
    updateEnemySnakes();
    updateMapObjects();
  }

  private void updateMyself() {
    List<Point> myHeads = board.get(Elements.MY_HEAD.toArray(new Elements[0]));
    if (myHeads.isEmpty()) {
      alive = false;
    } else {
      alive = true;
      roundLost = false;
      myHead = myHeads.get(0);
      updateFuryCount();
      updateFlyingCount();
      updateStoneCount();
    }
  }

  private void updateRoundState() {
    boolean newRound = board.get(Elements.HEAD_SLEEP).size() > 0;
    if (newRound) {
      ticks = 0;
      roundLost = false;
    } else {
      ++ticks;
      roundLost = !alive && board.get(Elements.HEAD_SLEEP).isEmpty() &&
          (!enemySnakes.isEmpty() || !board.get(Elements.HEAD_DEAD).isEmpty());
    }
  }

  private void updateFuryCount() {
    if (board.getAt(myHead) == Elements.HEAD_EVIL) {
      if (myFuryCount == 0) {
        myFuryCount = 9;
      } else {
        --myFuryCount;
      }
    } else {
      if (myFuryCount > 0) {
        System.out.printf("furyCount contingency loss at %d %d", myHead.getX(), myHead.getY());
      }
      myFuryCount = 0;
    }
  }

  private void updateFlyingCount() {
    if (board.getAt(myHead) == Elements.HEAD_FLY) {
      if (myFlyingCount == 0) {
        myFlyingCount = 9;
      } else {
        --myFlyingCount;
      }
    } else {
      if (myFlyingCount > 0) {
        System.out.printf("flyingCount contingency loss at %d %d", myHead.getX(), myHead.getY());
      }
      myFlyingCount = 0;
    }
  }

  private void updateStoneCount() {
    if (stones.contains(myHead)) {
      ++myStoneCount;
    }
  }

  private void updateEnemySnakes() {
    enemySnakes = board.getEnemySnakeHeads().stream()
        .map(head -> EnemySnake.identifyFromHead(head, board))
        .collect(Collectors.toUnmodifiableSet());
  }

  private void updateMapObjects() {
    gold = board.get(Elements.GOLD);
    apples = board.get(Elements.APPLE);

    if (stones.contains(myHead)) {
      ++myStoneCount;
    }

    // TODO: think about snake-flying-over-stone situation
    stones = board.get(Elements.STONE);
    furyPills = board.get(Elements.FURY_PILL);
    flyingPills = board.get(Elements.FLYING_PILL);
  }
}
