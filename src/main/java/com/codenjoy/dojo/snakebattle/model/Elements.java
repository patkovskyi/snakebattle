package com.codenjoy.dojo.snakebattle.model;

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

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.printer.CharElements;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Elements implements CharElements {
  NONE(' '), // пустое место
  WALL('☼'), // а это стенка
  START_FLOOR('#'), // место старта змей
  OTHER('?'), // этого ты никогда не увидишь :)

  APPLE('○'), // яблоки надо кушать от них становишься длинее
  STONE('●'), // а это кушать не стоит - от этого укорачиваешься
  FLYING_PILL('©'), // таблетка полета - дает суперсилы
  FURY_PILL('®'), // таблетка ярости - дает суперсилы
  GOLD('$'), // золото - просто очки

  // голова твоей змеи в разных состояниях и напрвлениях
  HEAD_DOWN('▼', Direction.UP),
  HEAD_LEFT('◄', Direction.RIGHT),
  HEAD_RIGHT('►', Direction.LEFT),
  HEAD_UP('▲', Direction.DOWN),
  HEAD_DEAD(
      '☻', Direction.UP, Direction.RIGHT, Direction.LEFT, Direction.DOWN), // этот раунд ты проиграл
  HEAD_EVIL(
      '♥',
      Direction.UP,
      Direction.RIGHT,
      Direction.LEFT,
      Direction.DOWN), // скушали таблетку ярости
  HEAD_FLY(
      '♠',
      Direction.UP,
      Direction.RIGHT,
      Direction.LEFT,
      Direction.DOWN), // скушали таблетку полета
  HEAD_SLEEP('&', Direction.LEFT), // змейка ожидает начала раунда

  // хвост твоей змейки
  TAIL_END_DOWN('╙', Direction.UP),
  TAIL_END_LEFT('╘', Direction.RIGHT),
  TAIL_END_UP('╓', Direction.DOWN),
  TAIL_END_RIGHT('╕', Direction.LEFT),
  TAIL_INACTIVE('~', Direction.UP, Direction.RIGHT, Direction.LEFT, Direction.DOWN),

  // туловище твоей змейки
  BODY_HORIZONTAL('═', Direction.LEFT, Direction.RIGHT),
  BODY_VERTICAL('║', Direction.UP, Direction.DOWN),
  BODY_LEFT_DOWN('╗', Direction.LEFT, Direction.DOWN),
  BODY_LEFT_UP('╝', Direction.LEFT, Direction.UP),
  BODY_RIGHT_DOWN('╔', Direction.RIGHT, Direction.DOWN),
  BODY_RIGHT_UP('╚', Direction.RIGHT, Direction.UP),

  // змейки противников
  ENEMY_HEAD_DOWN('˅', Direction.UP),
  ENEMY_HEAD_LEFT('<', Direction.RIGHT),
  ENEMY_HEAD_RIGHT('>', Direction.LEFT),
  ENEMY_HEAD_UP('˄', Direction.DOWN),
  ENEMY_HEAD_DEAD(
      '☺',
      Direction.UP,
      Direction.RIGHT,
      Direction.LEFT,
      Direction.DOWN), // этот раунд противник проиграл
  ENEMY_HEAD_EVIL(
      '♣',
      Direction.UP,
      Direction.RIGHT,
      Direction.LEFT,
      Direction.DOWN), // противник скушал таблетку ярости
  ENEMY_HEAD_FLY(
      '♦',
      Direction.UP,
      Direction.RIGHT,
      Direction.LEFT,
      Direction.DOWN), // противник скушал таблетку полета
  ENEMY_HEAD_SLEEP('ø', Direction.LEFT), // змейка ожидает начала раунда

  // хвосты змеек противников
  ENEMY_TAIL_END_DOWN('¤', Direction.UP),
  ENEMY_TAIL_END_LEFT('×', Direction.RIGHT),
  ENEMY_TAIL_END_UP('æ', Direction.DOWN),
  ENEMY_TAIL_END_RIGHT('ö', Direction.LEFT),
  ENEMY_TAIL_INACTIVE('*', Direction.UP, Direction.RIGHT, Direction.LEFT, Direction.DOWN),

  // туловище змеек противников
  ENEMY_BODY_HORIZONTAL('─', Direction.LEFT, Direction.RIGHT),
  ENEMY_BODY_VERTICAL('│', Direction.UP, Direction.DOWN),
  ENEMY_BODY_LEFT_DOWN('┐', Direction.LEFT, Direction.DOWN),
  ENEMY_BODY_LEFT_UP('┘', Direction.LEFT, Direction.UP),
  ENEMY_BODY_RIGHT_DOWN('┌', Direction.DOWN, Direction.RIGHT),
  ENEMY_BODY_RIGHT_UP('└', Direction.UP, Direction.RIGHT);

  public static Set<Elements> MY_HEAD =
      Stream.of(
              HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_DEAD, HEAD_EVIL, HEAD_FLY, HEAD_SLEEP)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> MY_BODY =
      Stream.of(
              BODY_HORIZONTAL,
              BODY_VERTICAL,
              BODY_LEFT_DOWN,
              BODY_LEFT_UP,
              BODY_RIGHT_DOWN,
              BODY_RIGHT_UP)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> MY_TAIL =
      Stream.of(TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_UP, TAIL_END_RIGHT, TAIL_INACTIVE)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> MY_SNAKE =
      Stream.concat(MY_HEAD.stream(), Stream.concat(MY_BODY.stream(), MY_TAIL.stream()))
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> ENEMY_HEAD =
      Stream.of(
              ENEMY_HEAD_DOWN,
              ENEMY_HEAD_LEFT,
              ENEMY_HEAD_RIGHT,
              ENEMY_HEAD_UP,
              ENEMY_HEAD_EVIL,
              ENEMY_HEAD_FLY,
              ENEMY_HEAD_SLEEP)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> ENEMY_BODY =
      Stream.of(
              ENEMY_BODY_HORIZONTAL,
              ENEMY_BODY_VERTICAL,
              ENEMY_BODY_LEFT_DOWN,
              ENEMY_BODY_LEFT_UP,
              ENEMY_BODY_RIGHT_DOWN,
              ENEMY_BODY_RIGHT_UP)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> ENEMY_TAIL =
      Stream.of(
              ENEMY_TAIL_END_DOWN,
              ENEMY_TAIL_END_LEFT,
              ENEMY_TAIL_END_UP,
              ENEMY_TAIL_END_RIGHT,
              ENEMY_TAIL_INACTIVE)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> ENEMY_SNAKE =
      Stream.concat(ENEMY_HEAD.stream(), Stream.concat(ENEMY_BODY.stream(), ENEMY_TAIL.stream()))
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> PASSABLE =
      Stream.of(NONE, APPLE, GOLD, STONE, FLYING_PILL, FURY_PILL)
          .collect(Collectors.toUnmodifiableSet());

  public static Set<Elements> POWER_UPS =
      Stream.of(APPLE, GOLD, FLYING_PILL, FURY_PILL).collect(Collectors.toUnmodifiableSet());

  final char ch;
  final List<Direction> compatibleDirections;

  Elements(char ch) {
    this.ch = ch;
    this.compatibleDirections = new ArrayList<>();
  }

  Elements(char ch, Direction... compatibleDirections) {
    this.ch = ch;
    this.compatibleDirections = List.of(compatibleDirections);
  }

  public static Elements valueOf(char ch) {
    for (Elements el : Elements.values()) {
      if (el.ch == ch) {
        return el;
      }
    }
    throw new IllegalArgumentException("No such element for " + ch);
  }

  @Override
  public char ch() {
    return ch;
  }

  public List<Direction> compatible() {
    return compatibleDirections;
  }

  @Override
  public String toString() {
    return String.valueOf(ch);
  }

  public boolean isCompatible(Direction direction, Elements anotherSnakePart) {
    if (!Direction.onlyDirections().contains(direction)) {
      throw new IllegalArgumentException(
          "You can only call this method with directions, but you called with "
              + direction.toString());
    }

    return (MY_SNAKE.contains(this) && MY_SNAKE.contains(anotherSnakePart)
            || ENEMY_SNAKE.contains(this) && ENEMY_SNAKE.contains(this))
        && compatibleDirections.contains(direction)
        && anotherSnakePart.compatibleDirections.contains(direction.inverted());
  }
}
