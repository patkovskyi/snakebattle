package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
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


import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;

/**
 * Класс, обрабатывающий строковое представление доски.
 * Содержит ряд унаследованных методов {@see AbstractBoard},
 * но ты можешь добавить сюда любые свои методы на их основе.
 */
public class Board extends AbstractBoard<Elements> {
    protected final int STONE_LENGTH_COST = 3;
    protected final int MIN_SNAKE_LENGTH = 2;
    protected final int FURY_LENGTH = 10;
    protected Point myHead;
    protected boolean stoneEatenLastRound;
    protected int stoneCount = 0;
    Direction headDirection = Direction.RIGHT;

    public List<Point> getNeighborPoints(Point p) {
        List<Point> l = new ArrayList<>();
        for (Direction d : Direction.onlyDirections()) {
            Point np = p.copy();
            np.change(d);
            if (isWithinBoard(np)) {
                l.add(np);
            }
        }

        return l;
    }

    public List<Point> getNeighborPoints(int x, int y) {
        return getNeighborPoints(new PointImpl(x, y));
    }

    public int getMySnakeLength() {
        int displayedLength = get(MY_SNAKE.toArray(new Elements[0])).size();
        return stoneEatenLastRound ? displayedLength - 3 : displayedLength;
    }

    public boolean areWeFurious() {
        return getAt(getMe()) == HEAD_EVIL;
    }

    @Override
    public Elements valueOf(char ch) {
        return Elements.valueOf(ch);
    }

    public boolean isBarrierAt(Point p) {
        return isBarrierAt(p.getX(), p.getY());
    }

    public boolean isBarrierAt(int x, int y) {
        return isAt(x, y, WALL, START_FLOOR, ENEMY_HEAD_SLEEP, ENEMY_TAIL_INACTIVE, ENEMY_HEAD_DEAD, TAIL_INACTIVE);
    }

    public boolean isStoneAt(Point p) {
        return isStoneAt(p.getX(), p.getY());
    }

    public boolean isStoneAt(int x, int y) {
        return isAt(x, y, STONE);
    }

    public boolean isDynamicBarrier(Point p) {
        return isDynamicBarrier(p.getX(), p.getY());
    }

    public boolean isDynamicBarrier(int x, int y) {
        return isAt(x, y, BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP,
                ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_HEAD_EVIL,
                ENEMY_TAIL_END_DOWN, ENEMY_TAIL_END_LEFT, ENEMY_TAIL_END_UP, ENEMY_TAIL_END_RIGHT, ENEMY_TAIL_INACTIVE,
                ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP);
    }

    public boolean isWithinBoard(Point p) {
        return isWithinBoard(p.getX(), p.getY());
    }

    public boolean isWithinBoard(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    public boolean isNotPassableOrRisky(Point p) {
        return isNotPassableOrRisky(p.getX(), p.getY());
    }

    public int distanceFromMe(Point p) {
        return getManhattanDistance(getMe(), p);
    }

    public int distanceFromMe(int x, int y) {
        return getManhattanDistance(getMe(), new PointImpl(x, y));
    }

    protected boolean isNextStepCollisionPossible(int x, int y) {
        if (distanceFromMe(x, y) == 1) {
            if (isBarrierAt(x, y)) return true;
            boolean weAreFurious = areWeFurious();
            if (!weAreFurious && isStoneAt(x, y) && (getMySnakeLength() - STONE_LENGTH_COST < MIN_SNAKE_LENGTH)) return true;

            Elements e = getAt(x, y);
            if (MY_BODY.contains(e)) return true; // actually we can cut a piece of ourselves and survive, but we'll implement it later
            if (!weAreFurious) {
                if (ENEMY_BODY.contains(e) || ENEMY_HEAD.contains(e)) return true;
            }

            // check head-to-head collision danger
            HashSet<Elements> neighbors = new HashSet<>(getNear(x, y));
            neighbors.remove(ENEMY_HEAD_FLY);
            neighbors.remove(HEAD_FLY);

            if (neighbors.stream().anyMatch(MY_HEAD::contains)) {
                for (Point p : getNeighborPoints(x, y)) {
                    Elements enemyHeadPoint = getAt(p);
                    if (ENEMY_HEAD.contains(enemyHeadPoint)) {
                        Snake enemySnake = Snake.identify(p.getX(), p.getY(), this);

                        if (!weAreFurious && enemySnake.isFurious) return true;
                        if (weAreFurious == enemySnake.isFurious && getMySnakeLength() - enemySnake.length < MIN_SNAKE_LENGTH) {
                            return true;
                        }
                    }
                }
            }
        }

        // no immediate danger
        return false;
    }

    public boolean isNotPassableOrRisky(int x, int y) {
        return !isWithinBoard(x, y) ||
                isBarrierAt(x, y) ||
                // TODO: should check for how long we stay furious here
                isStoneAt(x, y) && getMySnakeLength() < 5 && !(areWeFurious() && distanceFromMe(x, y) < FURY_LENGTH) ||
                isNextStepCollisionPossible(x, y);
    }

    public boolean isPowerUp(Point p) {
        return isPowerUp(p.getX(), p.getY());
    }

    public boolean isPowerUp(int x, int y) {
        return isAt(x, y, GOLD, APPLE);
    }

    public int getManhattanDistance(Point p1, Point p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    @Override
    protected int inversionY(int y) {
        return size - 1 - y;
    }

    protected void refreshMyHead() {
        myHead = get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY).get(0);

        // refresh counters
        if (!get(HEAD_SLEEP).isEmpty()) {
            stoneEatenLastRound = false;
            stoneCount = 0;
            headDirection = Direction.RIGHT;
        }
    }

    public Point getMe() {
        return myHead.copy();
    }

    public boolean isGameOver() {
        return getMyHead().isEmpty();
    }

    private List<Point> getMyHead() {
        return get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY);
    }
}
