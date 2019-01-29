package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;

import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;

public class Snake {
    protected boolean isMe;
    protected int length;
    protected Direction direction;
    protected boolean isFurious;

    public Snake(boolean isMe, int length, Direction direction, boolean isFurious){
        this.isMe = isMe;
        this.length = length;
        this.direction = direction;
        this.isFurious = isFurious;
    }

    // any part of the body should work
    public static Snake identify(int x, int y, Board board) {
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

            if ((compatibleCounter == 0 || length == 1 && compatibleCounter == 1) && !headElements.contains(e) && !tailElements.contains(e))
                throw new IllegalStateException("identify: abrupt snake ending at " + p.getX() + " " + p.getY());

            if (compatibleCounter > 1 && !(length == 1 && bodyElements.contains(e)))
                throw new IllegalStateException("identify: more than one compatible snake parts found for point at " + p.getX() + " " + p.getY());
        }

        if (direction == null) {
            throw new IllegalStateException("identify: could not identify direction");
        }

        return new Snake(isMe, length, direction, isFurious);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snake snake = (Snake) o;
        return isMe == snake.isMe &&
                length == snake.length &&
                isFurious == snake.isFurious &&
                direction == snake.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isMe, length, direction, isFurious);
    }

    @Override
    public String toString() {
        return "Snake{" +
                "isMe=" + isMe +
                ", length=" + length +
                ", direction=" + direction +
                ", isFurious=" + isFurious +
                '}';
    }
}
