package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MyBoard extends Board {
    Direction headDirection = Direction.RIGHT;

    protected void refreshDirection() {
        if (!get(Elements.HEAD_SLEEP, Elements.HEAD_RIGHT).isEmpty()) {
            // in case of a new round also reset to right
            headDirection = Direction.RIGHT;
        } else if (!get(Elements.HEAD_DOWN).isEmpty()) {
            headDirection = Direction.DOWN;
        } else if (!get(Elements.HEAD_RIGHT).isEmpty()) {
            headDirection = Direction.RIGHT;
        } else if (!get(Elements.HEAD_LEFT).isEmpty()) {
            headDirection = Direction.LEFT;
        } else if (!get(Elements.HEAD_UP).isEmpty()) {
            headDirection = Direction.UP;
        }
    }

    protected void markDeadEnds() {
        // TODO: implement dynamic deadend prediction (probably should predict where enemy snake is moving)
        boolean[][] nonPassable = new boolean[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                nonPassable[x][y] = isBarrierAt(x, y) || isStoneAt(x, y) && !areWeFurious() && getMySnakeLength() - STONE_LENGTH_COST < MIN_SNAKE_LENGTH;
            }
        }

        boolean updated;
        Direction[] directions = Direction.onlyDirections().toArray(new Direction[0]);

        do {
            updated = false;
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (!nonPassable[x][y]) {
                        int passableNeighbors = 0;
                        for (int d = 0; d < 4; d++) {
                            int nx = directions[d].changeX(x);
                            int ny = directions[d].changeY(y);
                            if (isWithinBoard(nx, ny) && !nonPassable[nx][ny])
                                ++passableNeighbors;
                        }

                        if (passableNeighbors <= 1) {
                            nonPassable[x][y] = true;
                            updated = true;

                            if (Elements.PASSABLE.contains(getAt(x, y))) {
                                set(x, y, Elements.WALL.ch());
                            }
                        }
                    }
                }
            }
        } while (updated);
    }

    public String getNextStep() {
        if (isGameOver()) {
            System.out.println("GAME OVER");
            return "";
        } else {
            refreshMyHead();
            refreshDirection();
            markDeadEnds();
            int[][][] dir = getDirectionalDistances();
            int[][] nondir = getNonDirectionalDistances(dir);
            Point closestPowerUp = getClosestPowerUp(nondir);
            Direction newDirection = getFirstStepNonDirectional(closestPowerUp, nondir);
            if (newDirection.value() < 4) {
                headDirection = newDirection;
            }

            return headDirection.toString();
        }
    }

    public Point getClosestPowerUp(int[][] nondir) {
        List<Point> points = getMySnakeLength() - STONE_LENGTH_COST >= MIN_SNAKE_LENGTH ? this.get(Elements.STONE) : new ArrayList<>();
        if (points.isEmpty() && areWeFurious()) {
            points = this.get(Elements.STONE).stream().filter(p -> distanceFromMe(p) < FURY_LENGTH).collect(Collectors.toList());
        }

        if (points.isEmpty()) {
            points = this.get(Elements.GOLD, Elements.APPLE);
            if (points.isEmpty()) {
                points = this.get(Elements.FURY_PILL);
                if (points.isEmpty()) {
                    points = this.get(Elements.FLYING_PILL);
                    if (points.isEmpty()) {
                        points = this.get(Elements.NONE);
                    }
                }
            }
        }

        return points.stream().min(Comparator.comparingInt(p -> nondir[p.getX()][p.getY()])).get();
    }

    public Direction getFirstStepTo(Point p, int[][][] dir) {
        System.out.printf("getFirstStepTo starts search for %d %d\n", p.getX(), p.getY());

        Direction lastDirection = Direction.LEFT;
        Point currentHead = getMe();
        Point currentP = p.copy();
        while (!currentP.itsMe(currentHead)) {
            for (Direction d : Direction.onlyDirections()) {
                if (d.inverted() != lastDirection || currentP.itsMe(p)) {
                    if (dir[currentP.getX()][currentP.getY()][d.value()] < dir[currentP.getX()][currentP.getY()][lastDirection.value()]) {
                        lastDirection = d;
                    }
                }
            }

            currentP.change(lastDirection.inverted());
        }

        System.out.println("getFirstStepTo finished\n");
        return lastDirection;
    }

    public Direction getFirstStepNonDirectional(Point p, int[][] nondir) {
        System.out.printf("getFirstStepNonDirectional search for %d %d\n", p.getX(), p.getY());
        if (nondir[p.getX()][p.getY()] == Integer.MAX_VALUE) {
            System.out.println("getFirstStepNonDirectional: path is BLOCKED");
            return Direction.ACT;
        }

        Point newP = p;
        do {
            p = newP;
            for (Direction d : Direction.onlyDirections()) {
                newP = p.copy();
                newP.change(d);

                if (nondir[newP.getX()][newP.getY()] == nondir[p.getX()][p.getY()] - 1) {
                    if (nondir[newP.getX()][newP.getY()] == 0) {
                        System.out.println("getFirstStepNonDirectional: SUCCESS");
                        return d.inverted();
                    }

                    break;
                }
            }
        } while (nondir[newP.getX()][newP.getY()] == nondir[p.getX()][p.getY()] - 1);

        throw new IllegalStateException("getFirstStepNonDirectional should never reach this line");
    }

    public int[][] getNonDirectionalDistances(int[][][] dir) {
        int[][] nondir = new int[size][size];
        for (int y = size; y --> 0; ) {
            for (int x = 0; x < size; x++) {
                nondir[x][y] = Integer.MAX_VALUE;

                for (int k = 0; k < 4; k++) {
                    nondir[x][y] = Math.min(nondir[x][y], dir[x][y][k]);
                }

                if (nondir[x][y] < Integer.MAX_VALUE) {
                    System.out.printf("%d", nondir[x][y]);
                } else {
                    System.out.print("☼");
                }
            }

            System.out.println();
        }

        return nondir;
    }

    public int[][][] getDirectionalDistances() {
        System.out.println("getDirectionalDistances started");

        int[][][] dir = new int[size][size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int d = 0; d < 4; d++) {
                    dir[x][y][d] = Integer.MAX_VALUE;
                }
            }
        }

        Point head = getMe();

        System.out.printf("Me: %d %d\n", head.getX(), head.getY());

        ArrayDeque<HeadPosition> q = new ArrayDeque<>();
        q.add(new HeadPosition(head.getX(), head.getY(), headDirection.value()));
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.add(0);

        boolean[][][] queued = new boolean[size][size][4];
        queued[head.getX()][head.getY()][headDirection.value()] = true;

        while (!q.isEmpty()) {
            HeadPosition p = q.remove();
            int dist = d.remove();
            if (dist < dir[p.x][p.y][p.d]) {
                dir[p.x][p.y][p.d] = dist;

                for (Direction newDir : Direction.onlyDirections()) {
                    // snake cannot turn back
                    if (newDir.inverted().value() != p.d) {
                        Point newPoint = new PointImpl(p.x, p.y);
                        newPoint.change(newDir);
                        if (!isNotPassableOrRisky(newPoint)) {
                            HeadPosition np = new HeadPosition(newPoint.getX(), newPoint.getY(), newDir.value());
                            if (queued[np.getX()][np.getY()][np.getDirection()] == false) {
                                queued[np.getX()][np.getY()][np.getDirection()] = true;
                                q.add(np);
                                d.add(dist + 1);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("getDirectionalDistances finished");
        return dir;
    }
}
