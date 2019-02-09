package com.codenjoy.dojo.snakebattle.client;

enum DynamicObstacle {
  Nothing(0),
  Body(1),
  Neck(2);

  private final int value;

  DynamicObstacle(int value) {
    this.value = value;
  }
}
