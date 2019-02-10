package com.codenjoy.dojo.snakebattle.model;

import java.util.stream.Stream;
import lombok.Getter;

public enum HeroAction {
  LEFT(0, "LEFT"),
  RIGHT(1, "RIGHT"),
  UP(2, "UP"),
  DOWN(3, "DOWN"),
  LEFT_STONE(4, "LEFT, ACT"),
  RIGHT_STONE(5, "RIGHT, ACT"),
  UP_STONE(6, "UP, ACT"),
  DOWN_STONE(7, "DOWN, ACT"),
  SUICIDE(8, "ACT(0)");

  private final int value;

  @Getter
  private final String str;

  HeroAction(int value, String str) {
    this.value = value;
    this.str = str;
  }

  public static HeroAction valueOf(int val) {
    return Stream.of(HeroAction.values()).filter(v -> v.value == val).findFirst().get();
  }
}
