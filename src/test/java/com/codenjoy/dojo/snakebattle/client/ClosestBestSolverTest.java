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
 * You trivialCases have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Direction;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class ClosestBestSolverTest {
  @Rule public TestRule globalTimeout = new DisableOnDebug(new Timeout(500, TimeUnit.MILLISECONDS));
  private Solver<ClosestBestBoard> ai;

  @Before
  public void setup() {
    ai = new ClosestBestSolver();
  }

  @Test
  public void trivialCases() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "╘►     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.RIGHT);

    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼#╘►  ○☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.RIGHT);

    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼ ╘►  ☼"
            + "☼☼     ☼"
            + "☼☼$    ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.DOWN);

    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼  ○  ☼"
            + "☼☼ ╘►  ☼"
            + "☼☼     ☼"
            + "☼☼$    ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.UP);

    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼    ○☼"
            + "☼☼     ☼"
            + "☼☼  ▲  ☼"
            + "☼☼$ ╙  ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void preferApplesOrGoldWhenShort() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼         ○                 ☼"
            + "☼#                           ☼"
            + "☼☼  ○    ●         ◄══╕      ☼"
            + "☼☼                           ☼"
            + "☼☼ ○         ●    ○          ☼"
            + "☼☼     ☼☼☼☼☼                 ☼"
            + "☼☼     ☼                     ☼"
            + "☼#     ☼☼☼     ○  ☼☼☼*ø      ☼"
            + "☼☼     ☼          ☼   ☼      ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼                ☼         $☼"
            + "☼☼       ○                   ☼"
            + "*ø             ○      ○      ☼"
            + "☼☼           ○               ☼"
            + "☼☼        ☼☼☼  ®             ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼"
            + "☼#   æ  ☼   ☼     ☼ $☼  ☼    ☼"
            + "☼☼   └>   ®       ☼     ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                   ●       ☼"
            + "☼☼             ●         ○   ☼"
            + "☼#  ○                        ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void preferStonesIfLong() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼         ○                 ☼"
            + "☼#                           ☼"
            + "☼☼  ○    ●         ◄═══╕     ☼"
            + "☼☼                           ☼"
            + "☼☼ ○         ○    ○          ☼"
            + "☼☼     ☼☼☼☼☼                 ☼"
            + "☼☼     ☼                     ☼"
            + "☼#     ☼☼☼     ○  ☼☼☼*ø      ☼"
            + "☼☼     ☼          ☼   ☼      ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼                ☼         $☼"
            + "☼☼       ○                   ☼"
            + "*ø             ○      ○      ☼"
            + "☼☼           ○               ☼"
            + "☼☼        ☼☼☼  ®             ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼"
            + "☼#   æ  ☼   ☼     ☼ $☼  ☼    ☼"
            + "☼☼   └>   ®       ☼     ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                   ●       ☼"
            + "☼☼             ●         ○   ☼"
            + "☼#  ○                        ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void investigation1() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼®           ®  ® ▲         ☼"
            + "☼#                 ║ ©       ☼"
            + "☼☼×>     ●        ╔╝    ○  ○ ☼"
            + "☼☼       ╘══╗     ║     ○    ☼"
            + "☼☼  ●       ║●  ╔═╝          ☼"
            + "☼☼     ☼☼☼☼☼║   ║            ☼"
            + "☼☼     ☼    ╚══╗║©   ●       ☼"
            + "*ø    ●☼☼☼     ╚╝ ☼☼☼☼#      ☼"
            + "☼☼     ☼          ☼   ☼  ●   ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼       ©        ☼         $☼"
            + "☼☼    ● ○                    ☼"
            + "*ø                           ☼"
            + "☼☼             ●             ☼"
            + "☼☼        ☼☼☼                ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼☼#     ☼☼   *ø    ☼"
            + "☼☼      ☼   ☼ $ ● ☼ ☼ ☼ ☼    ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼   ○☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼     ●          ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼             ●             ☼"
            + "☼#          ○                ☼"
            + "☼☼                           ☼"
            + "☼☼        ○                  ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.RIGHT);
  }

  @Test
  public void investigation2() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼         ○                 ☼"
            + "☼#                           ☼"
            + "☼☼  ○    ●                   ☼"
            + "☼☼      $                    ☼"
            + "☼☼ ○         ●    æ          ☼"
            + "☼☼  ®  ☼☼☼☼☼      └──> ○     ☼"
            + "☼☼     ☼  ◄════╕             ☼"
            + "☼#     ☼☼☼        ☼☼☼☼#      ☼"
            + "☼☼     ☼          ☼   ☼  ●   ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼*ø      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼ ×──>           ☼         $☼"
            + "☼☼    ●  ○                   ☼"
            + "☼#                    ○      ☼"
            + "☼☼                           ☼"
            + "☼☼        ☼☼☼                ☼"
            + "☼☼       ☼  ☼●               ☼"
            + "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼     ●          ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                  ○        ☼"
            + "☼☼ ○    ○      ●         ○   ☼"
            + "☼#            ○    ×>        ☼"
            + "☼☼           ○   ○           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void investigation3() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼         ○                 ☼"
            + "☼#                        ×> ☼"
            + "☼☼  ○    ●         ○       ○ ☼"
            + "☼☼                     $○    ☼"
            + "☼☼ ○         ●    ○          ☼"
            + "☼☼     ☼☼☼☼☼                 ☼"
            + "☼☼     ☼ ○                   ☼"
            + "☼#     ☼☼☼     ○  ☼☼☼☼#      ☼"
            + "☼☼     ☼    ○     ☼   ☼  ●   ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼○               ☼         $☼"
            + "☼☼    ●  ◄═══╗               ☼"
            + "*ø           ╙           ×─> ☼"
            + "☼☼        ©                  ☼"
            + "☼☼        ☼☼☼                ☼"
            + "☼☼   ○   ☼  ☼                ☼"
            + "☼☼      ☼☼☼☼#     ☼☼   *ø    ☼"
            + "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼   ©☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼     ●          ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                  ○        ☼"
            + "☼☼ ○    ○      ●         ○   ☼"
            + "☼#              $            ☼"
            + "☼☼               ○           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void investigation4() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼               ●           ☼"
            + "*ø                           ☼"
            + "☼☼      ○●                   ☼"
            + "☼☼           ●               ☼"
            + "☼☼           ●               ☼"
            + "☼☼     ☼☼☼☼☼     ● ○         ☼"
            + "☼☼  ®  ☼                     ☼"
            + "☼#     ☼☼☼        ☼☼☼☼#      ☼"
            + "☼☼  ©  ☼          ☼   ☼○     ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼®               ☼          ☼"
            + "☼☼    ●    ®                 ☼"
            + "☼#                           ☼"
            + "☼☼                           ☼"
            + "☼☼        ☼☼☼   ●            ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼     ☼●☼ ☼ ☼    ☼"
            + "☼#      ☼   ☼     ☼  ☼ ○☼    ☼"
            + "☼☼                ☼    ●☼    ☼"
            + "☼☼   ˄ ●          ☼   ˄ ☼    ☼"
            + "☼☼   ¤              ×─┘▲     ☼"
            + "☼☼                ╘════╝     ☼"
            + "☼☼                           ☼"
            + "☼#                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.UP);
  }

  @Test
  public void investigation5() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼┌>                         ☼"
            + "*ø│         ®                ☼"
            + "☼☼└──ö   ●         ○         ☼"
            + "☼☼                      ○    ☼"
            + "☼☼           ●    ○          ☼"
            + "☼☼     ☼☼☼☼☼                 ☼"
            + "☼☼     ☼                     ☼"
            + "☼#     ☼☼☼     ○  ☼☼☼☼#      ☼"
            + "☼☼     ☼          ☼   ☼  ●   ☼"
            + "☼☼  ○  ☼☼☼*ø    ○ ☼☼☼☼# ○    ☼"
            + "☼☼                ☼ ○        ☼"
            + "☼☼                ☼        ®$☼"
            + "☼☼    ●  $                   ☼"
            + "*ø             ○ ○    ○      ☼"
            + "☼☼    ○                    ® ☼"
            + "☼☼        ☼☼☼    $           ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼   ● ☼●☼ ☼ ☼ ○  ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼    ☼"
            + "☼☼   ○          © ☼     ☼    ☼"
            + "☼☼     ●          ☼   ╓ ☼    ☼"
            + "☼☼                    ║      ☼"
            + "☼☼                    ║      ☼"
            + "☼☼             ●      ╚══╗   ☼"
            + "☼#                       ║   ☼"
            + "☼☼   ©                ◄╗ ║   ☼"
            + "☼☼                     ╚═╝   ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.UP);
  }

  @Test
  public void avoidCollisionEqualLengthNonFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼#╘►  ○☼"
            + "☼☼ ×>  ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.UP);
  }

  @Test
  public void avoidCollisionEqualLengthFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼#╘♥  ○☼"
            + "☼☼ ×♣  ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.UP);
  }

  @Test
  public void avoidCollisionSmallerLengthNonFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼×─>  ☼"
            + "☼#╘►  ○☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void avoidCollisionBiggerLengthEnemyFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼  ×♣ ☼"
            + "☼#╘═► ○☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void avoidCollisionBiggerBy1LengthNonFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼  ×> ☼"
            + "☼#╘═► ○☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void takeCollisionBiggerBy2LengthNonFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼   ×>☼"
            + "☼#╘══►○☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.RIGHT);
  }

  @Test
  public void takeCollisionSmallerButFury() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼# ╘♥ ○☼"
            + "☼☼ ×─> ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.RIGHT);
  }

  @Test
  public void avoidDeadEnds1() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼               ●           ☼"
            + "*ø                           ☼"
            + "☼☼      ○●                   ☼"
            + "☼☼           ●               ☼"
            + "☼☼           ●               ☼"
            + "☼☼     ☼☼☼☼☼     ● ○         ☼"
            + "☼☼  ®  ☼                     ☼"
            + "☼#     ☼☼☼        ☼☼☼☼#      ☼"
            + "☼☼  ©  ☼          ☼   ☼○     ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼®               ☼          ☼"
            + "☼☼    ●    ®                 ☼"
            + "☼#                           ☼"
            + "☼☼                           ☼"
            + "☼☼        ☼☼☼   ●            ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼     ☼ ☼ ☼●☼    ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼     ●          ☼     ☼    ☼"
            + "☼☼                     ▲     ☼"
            + "☼☼                ╘════╝     ☼"
            + "☼☼                           ☼"
            + "☼#                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void avoidDeadEnds2() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼               ●           ☼"
            + "*ø                           ☼"
            + "☼☼      ○●                   ☼"
            + "☼☼           ●               ☼"
            + "☼☼                           ☼"
            + "☼☼     ☼☼☼☼☼     ● ○         ☼"
            + "☼☼  ®  ☼ ○  ◄═╕              ☼"
            + "☼#     ☼☼☼        ☼☼☼☼#      ☼"
            + "☼☼  ©  ☼          ☼   ☼○     ☼"
            + "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼"
            + "☼☼                ☼          ☼"
            + "☼☼®               ☼          ☼"
            + "☼☼    ●    ®                 ☼"
            + "☼#                           ☼"
            + "☼☼                           ☼"
            + "☼☼        ☼☼☼   ●            ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼     ☼ ☼ ☼●☼    ☼"
            + "☼#      ☼   ☼     ☼  ☼  ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼     ●          ☼     ☼    ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼#                           ☼"
            + "☼☼                           ☼"
            + "☼☼                           ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.UP);
  }

  @Test
  public void dontBeAfraidOfASleepingHead() {
    assertAI(
        "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼"
            + "☼☼                    ×──>   ☼"
            + "☼#                           ☼"
            + "☼☼               ®           ☼"
            + "☼☼               ○         ○ ☼"
            + "☼☼                    ○ ●    ☼"
            + "☼☼     ☼☼☼☼☼           ●     ☼"
            + "☼☼     ☼©              ●     ☼"
            + "*ø   ● ☼☼☼  ╔╕●   ☼☼☼☼#      ☼"
            + "☼☼     ☼   ○▼     ☼○● ☼      ☼"
            + "☼☼●    ☼☼☼*ø      ☼☼☼*ø      ☼"
            + "☼☼              ○ ☼©        ○☼"
            + "☼☼             ●○ ☼          ☼"
            + "☼☼     ©●®     ●          ●  ☼"
            + "☼#                  ○        ☼"
            + "☼☼                       ○   ☼"
            + "☼☼        ☼☼☼  ● ●           ☼"
            + "☼☼       ☼  ☼                ☼"
            + "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼"
            + "☼☼      ☼   ☼     ☼ ☼ ☼$☼    ☼"
            + "☼#○     ☼  ®☼     ☼  ☼  ☼    ☼"
            + "☼☼                ☼     ☼    ☼"
            + "☼☼                ☼○    ☼    ☼"
            + "☼☼             ●             ☼"
            + "☼☼                    $  ○   ☼"
            + "☼☼           ○ ●             ☼"
            + "☼# $         ○   ●    ○      ☼"
            + "☼☼   ○          ○    ○       ☼"
            + "☼☼               ○ ○         ☼"
            + "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void avoidDynamicDeadend1() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼   ˄○☼"
            + "☼☼   │ ☼"
            + "☼☼  ×┘ ☼"
            + "☼☼    ▲☼"
            + "☼☼  ╘═╝☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void avoidDynamicDeadend2() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼☼   ˄ ☼"
            + "☼☼   │ ☼"
            + "☼☼  ×┘○☼"
            + "☼☼    ▲☼"
            + "☼☼  ╘═╝☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.LEFT);
  }

  @Test
  public void goBehind() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼   ☼ ☼"
            + "☼#  ○╘►☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void lucrativityTest1() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼#╘►   ☼"
            + "☼☼     ☼"
            + "☼☼ ® ○ ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.DOWN);
  }

  @Test
  public void lucrativityTest2() {
    assertAI(
        "☼☼☼☼☼☼☼☼"
            + "☼☼     ☼"
            + "☼#╘► ® ☼"
            + "☼☼     ☼"
            + "☼☼   ○ ☼"
            + "☼☼     ☼"
            + "☼☼     ☼"
            + "☼☼☼☼☼☼☼☼",
        Direction.RIGHT);
  }

  private ClosestBestBoard board(String board) {
    return (ClosestBestBoard) new ClosestBestBoard().forString(board);
  }

  private void assertAI(String board, Direction expected) {
    String actual = ai.get(board(board));
    assertEquals(expected.toString(), actual);
  }
}
