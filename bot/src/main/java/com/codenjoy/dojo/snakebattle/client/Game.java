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

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.old.BoardSnake;
import com.codenjoy.dojo.snakebattle.model.Elements;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

public class Game {

  public static final int FURY_LENGTH = 10;
  public static final int FLIGHT_LENGTH = 10;

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
  private BoardSnake myBoardSnake;

  @Getter
  private Collection<BoardSnake> enemyBoardSnakes;

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
    enemyBoardSnakes = new HashSet<>();
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
    this.board = new Board().forString(board.boardAsString());
    updateMyself();
    updateRoundState();
    updateEnemySnakes();
    updateMapObjects();
  }

  public int size() {
    return board.size();
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
      myBoardSnake = BoardSnake.identify(myHead, board);
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
          (!enemyBoardSnakes.isEmpty() || !board.get(Elements.HEAD_DEAD).isEmpty());
    }
  }

  private void updateFuryCount() {
    if (furyPills.contains(myHead)) {
      myFuryCount += FURY_LENGTH;
    }

    Elements myHeadElement = board.getAt(myHead);
    if (myHeadElement != Elements.HEAD_EVIL && Elements.MY_HEAD.contains(myHeadElement)) {
      System.out.printf("myFuryCount (%d) was out of sync with myHead (%s)\n", myFuryCount,
          myHeadElement);
      myFuryCount = 0;
    }

    if (myFuryCount > 0) {
      --myFuryCount;
    }
  }

  private void updateFlyingCount() {
    if (flyingPills.contains(myHead)) {
      myFlyingCount += FLIGHT_LENGTH;
    }

    Elements myHeadElement = board.getAt(myHead);
    if (myHeadElement != Elements.HEAD_FLY && Elements.MY_HEAD.contains(myHeadElement)) {
      System.out.printf("myFlyingCount (%d) was out of sync with myHead (%s)\n", myFlyingCount,
          myHeadElement);
      myFlyingCount = 0;
    }

    if (myFlyingCount > 0) {
      --myFlyingCount;
    }
  }

  private void updateStoneCount() {
    if (stones.contains(myHead) && myFlyingCount == 0) {
      ++myStoneCount;
    }
  }

  private void updateEnemySnakes() {
    enemyBoardSnakes = board.getEnemySnakeHeads().stream()
        .map(head -> BoardSnake.identify(head, board))
        .collect(Collectors.toUnmodifiableSet());
  }

  private void updateMapObjects() {
    gold = board.get(Elements.GOLD);
    apples = board.get(Elements.APPLE);
    stones = board.get(Elements.STONE);
    furyPills = board.get(Elements.FURY_PILL);
    flyingPills = board.get(Elements.FLYING_PILL);
  }
}
