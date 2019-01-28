package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.Direction;

public class HeadPosition {
    int x, y, d;

    public HeadPosition(int x, int y, int d) {
        this.x = x;
        this.y = y;
        this.d = d;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDirection() {
        return d;
    }
}
