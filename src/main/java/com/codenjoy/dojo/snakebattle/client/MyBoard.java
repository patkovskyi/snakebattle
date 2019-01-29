package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.model.Elements;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;

public class MyBoard extends Board {
    Direction headDirection = Direction.RIGHT;

    public String getNextStep() {
        if (isGameOver()) {
            System.out.println("GAME OVER");
            return "";
        } else {
            if (!get(Elements.HEAD_SLEEP).isEmpty()) {
                // reset on new round
                headDirection = Direction.RIGHT;
            }

            int[][][] dir = getDirectionalDistances();
            int[][] nondir = getNonDirectionalDistances(dir);
            Point closestPowerUp = getClosestPowerUp(nondir);
            Direction newDirection = getFirstStepTo(closestPowerUp, dir);
            if (newDirection.value() < 4) {
                headDirection = newDirection;
            }

            return headDirection.toString();
        }
    }

    public Point getClosestPowerUp(int[][] nondir) {
        List<Point> points = this.get(Elements.GOLD, Elements.APPLE);
        if (points.isEmpty()) {
            points = this.get(Elements.FLYING_PILL, Elements.FURY_PILL);
            if (points.isEmpty()) {
                points = this.get(Elements.NONE);
            }
        }

        return points.stream().min(Comparator.comparingInt(p -> nondir[p.getX()][p.getY()])).get();
    }

    public Direction getFirstStepTo(Point p, int[][][] dir) {
        System.out.printf("Searching path to %d %d\n", p.getX(), p.getY());

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

        return lastDirection;
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
                    System.out.printf("%d", nondir[x][y] / 10);
                } else {
                    System.out.print("☼");
                }
            }

            System.out.println();
        }

        return nondir;
    }

    public int[][][] getDirectionalDistances() {
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
                        if (!isProblematic(newPoint)) {
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

        return dir;
    }
}
