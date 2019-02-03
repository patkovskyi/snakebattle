package com.codenjoy.dojo.snakebattle.client;

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

  public Game() {
    gold = new HashSet<>();
    apples = new HashSet<>();
    stones = new HashSet<>();
    furyPills = new HashSet<>();
    flyingPills = new HashSet<>();
    enemySnakes = new HashSet<>();
  }

  public void updateFromBoard(Board board) {
    updateMyself(board);
    updateRoundState(board);
    updateEnemySnakes(board);
    updateMapObjects(board);
  }

  private void updateMyself(Board board) {
    List<Point> myHeads = board.get(Elements.MY_HEAD.toArray(new Elements[0]));
    if (myHeads.isEmpty()) {
      alive = false;
    } else {
      alive = true;
      roundLost = false;
      if (myFuryCount > 0) {
        --myFuryCount;
      }
      if (myFlyingCount > 0) {
        --myFlyingCount;
      }
      myHead = myHeads.get(0);
    }
  }

  private void updateRoundState(Board board) {
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

  private void updateEnemySnakes(Board board) {
    enemySnakes = board.getEnemySnakeHeads().stream()
        .map(head -> EnemySnake.identifyFromHead(head, board))
        .collect(Collectors.toUnmodifiableSet());
  }

  private void updateMapObjects(Board board) {
    gold = board.get(Elements.GOLD);
    apples = board.get(Elements.APPLE);

    // TODO: think about snake-flying-over-stone situation
    stones = board.get(Elements.STONE);
    furyPills = board.get(Elements.FURY_PILL);
    flyingPills = board.get(Elements.FLYING_PILL);
  }
}
