package com.codenjoy.dojo.snakebattle.model;

import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.hero.Hero;
import lombok.Data;

@Data
public class MeetingPoint {
  private final Point point;
  private final Hero enemy;
}
