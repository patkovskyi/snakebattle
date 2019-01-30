package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;

import java.util.*;
import java.util.stream.Collectors;

public class MyBoard extends Board {
    int[][] lucrativity;
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

            if (shitABrick()) {
                --stoneCount;
                headDirection = headDirection.clockwise();
                return "ACT, " + headDirection.toString();
            }

            int[][] distances = getDistances();
            Point closestPowerUp = getBestPowerUp(distances);
            Direction newDirection = getFirstStepNonDirectional(closestPowerUp, distances);
            if (newDirection.value() < 4) {
                headDirection = newDirection;
                refreshWhatsEaten(newDirection);
            }

            return headDirection.toString();
        }
    }

    private boolean shitABrick() {
        Point np = getMe();
        np.change(headDirection.clockwise());
        if (stoneCount > 0 && !stoneEatenLastRound && canEatStone() && !isNotPassableOrRisky(np)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean canEatStone() {
        return areWeFurious() || (getMySnakeLength() - STONE_LENGTH_COST >= MIN_SNAKE_LENGTH);
    }

    private void refreshWhatsEaten(Direction newDirection) {
        Point np = getMe();
        np.change(newDirection);
        if (getAt(np) == Elements.STONE) {
            stoneEatenLastRound = true;
            ++stoneCount;
        } else {
            stoneEatenLastRound = false;
        }
    }

    public Point getBestPowerUp(int[][] nondir) {
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

    public Direction getFirstStepNonDirectional(Point p, int[][] nondir) {
        System.out.printf("getFirstStepNonDirectional search for %d %d\n", p.getX(), p.getY());
        if (nondir[p.getX()][p.getY()] == Integer.MAX_VALUE) {
            System.out.println("getFirstStepNonDirectional: path is BLOCKED");
            return Direction.ACT;
        }

        Point bestP = p;
        do {
            p = bestP;
            bestP = null;
            for (Direction d : Direction.onlyDirections()) {
                Point newP = p.copy();
                newP.change(d);

                if (nondir[newP.getX()][newP.getY()] == nondir[p.getX()][p.getY()] - 1) {
                    if (nondir[newP.getX()][newP.getY()] == 0) {
                        System.out.println("getFirstStepNonDirectional: SUCCESS");
                        return d.inverted();
                    }

                    if (bestP == null || lucrativity[newP.getX()][newP.getY()] > lucrativity[bestP.getX()][bestP.getY()]) {
                        bestP = newP;
                    }
                }
            }
        } while (nondir[bestP.getX()][bestP.getY()] == nondir[p.getX()][p.getY()] - 1);

        throw new IllegalStateException("getFirstStepNonDirectional should never reach this line");
    }

    public int[][] getDistances() {
        System.out.println("getDistances started");
        int[][] dist = new int[size][size];
        lucrativity  = new int[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                dist[x][y] = Integer.MAX_VALUE;
            }
        }

        Point head = getMe();
        System.out.printf("Me: %d %d\n", head.getX(), head.getY());

        int step = 0;
        PriorityQueue<Point> openSet = new PriorityQueue<>((p1, p2) -> Integer.compare(lucrativity[p2.getX()][p2.getY()], lucrativity[p1.getX()][p1.getY()]));
        openSet.add(head);
        boolean[][] visited = new boolean[size][size];

        while (!openSet.isEmpty()) {
            PriorityQueue<Point> closedSet = openSet;
            openSet = new PriorityQueue<>((p1, p2) -> Integer.compare(lucrativity[p2.getX()][p2.getY()], lucrativity[p1.getX()][p1.getY()]));

            while (!closedSet.isEmpty()) {
                Point p = closedSet.remove();
                if (!visited[p.getX()][p.getY()]) {
                    visited[p.getX()][p.getY()] = true;
                    dist[p.getX()][p.getY()] = step;

                    for (Direction ndir : Direction.onlyDirections()) {
                        if (step > 0 || ndir != headDirection.inverted()) {
                            Point np = p.copy();
                            np.change(ndir);

                            if (!isNotPassableOrRisky(np) && step + 1 < dist[np.getX()][np.getY()]) {
                                lucrativity[np.getX()][np.getY()] = Math.max(lucrativity[p.getX()][p.getY()] + getPointLucrativity(np), lucrativity[np.getX()][np.getY()]);
                                openSet.add(np);
                            }
                        }
                    }
                }
            }

            ++step;
        }

        System.out.println("Distances: ");
        for (int y = size; y --> 0; ) {
            for (int x = 0; x < size; x++) {
                if (dist[x][y] < Integer.MAX_VALUE) {
                    System.out.printf("%d", dist[x][y]);
                } else {
                    System.out.printf("*");
                }
            }
            System.out.println();
        }

        System.out.println("Lucrativity: ");
        for (int y = size; y --> 0; ) {
            for (int x = 0; x < size; x++) {
                System.out.printf("%d", lucrativity[x][y]);
            }
            System.out.println();
        }

        System.out.println("getDistances finished");
        return dist;
    }

    protected int getPointLucrativity(Point p) {
        return Elements.POWER_UPS.contains(getAt(p)) ? 1 : 0;
    }
}
