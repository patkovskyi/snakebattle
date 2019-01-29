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
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;

/**
 * Класс, обрабатывающий строковое представление доски.
 * Содержит ряд унаследованных методов {@see AbstractBoard},
 * но ты можешь добавить сюда любые свои методы на их основе.
 */
public class Board extends AbstractBoard<Elements> {

    protected final int DYNAMIC_DANGER_DISTANCE = 3;

    protected Elements[] MY_SNAKE = {
            HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_DEAD, HEAD_EVIL, HEAD_FLY, HEAD_SLEEP,
            TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_UP, TAIL_END_RIGHT, TAIL_INACTIVE,
            BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP
    };

    protected HashSet<Elements> MY_HEAD = Stream.of(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_DEAD, HEAD_EVIL, HEAD_FLY, HEAD_SLEEP)
            .collect(Collectors.toCollection(HashSet::new));

    protected HashSet<Elements> ENEMY_HEAD = Stream.of(ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY)
            .collect(Collectors.toCollection(HashSet::new));

    public int getMySnakeLength() {
        return get(MY_SNAKE).size();
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

    public boolean isWithinBoard(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    public boolean isProblematic(Point p) {
        return isProblematic(p.getX(), p.getY());
    }

    protected boolean isNextStepCollisionPossible(int x, int y) {
        HashSet<Elements> neighbors = new HashSet<>(getNear(x, y));
        neighbors.remove(ENEMY_HEAD_FLY);
        neighbors.remove(HEAD_FLY);

        // add length comparison ?
        return neighbors.stream().anyMatch(ENEMY_HEAD::contains) && neighbors.stream().anyMatch(MY_HEAD::contains);
    }

    public boolean isProblematic(int x, int y) {
        return !isWithinBoard(x, y) || isBarrierAt(x, y) || isStoneAt(x, y) && getMySnakeLength() < 5 ||
                // heuristic for dangerous dynamic barriers
                (isDynamicBarrier(x, y) && getManhattanDistance(getMe(), new PointImpl(x, y)) < DYNAMIC_DANGER_DISTANCE)
                || isNextStepCollisionPossible(x, y);
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

    public Point getMe() {
        return get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY).get(0);
    }

    public boolean isNewRound() {
        return !get(Elements.HEAD_SLEEP).isEmpty();
    }

    public boolean isGameOver() {
        return getMyHead().isEmpty();
    }

    private List<Point> getMyHead() {
        return get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY);
    }
}
