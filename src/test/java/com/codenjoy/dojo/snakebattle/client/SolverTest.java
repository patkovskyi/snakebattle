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


import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author K.ilya
 *         Это пример для реализации unit-тестов твоего бота
 *         Необходимо раскомментировать существующие тесты, добиться их выполнения ботом.
 *         Затем создавай свои тесты, улучшай бота и проверяй что не испортил предыдущие достижения.
 */

public class SolverTest {

    private Dice dice;
    private Solver ai;

    @Rule
    public Timeout globalTimeout = Timeout.millis(700);

    @Before
    public void setup() {
        dice = mock(Dice.class);
        ai = new YourSolver(dice);
    }

    @Test
    public void trivialCases() {
        assertAI("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "╘►     ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼☼☼☼☼☼☼", Direction.RIGHT);

        assertAI("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼#╘►  ○☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼☼☼☼☼☼☼", Direction.RIGHT);

        assertAI("☼☼☼☼☼☼☼☼" +
                "☼☼     ☼" +
                "☼☼ ╘►  ☼" +
                "☼☼     ☼" +
                "☼☼$    ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼☼☼☼☼☼☼", Direction.DOWN);

        assertAI("☼☼☼☼☼☼☼☼" +
                "☼☼  ○  ☼" +
                "☼☼ ╘►  ☼" +
                "☼☼     ☼" +
                "☼☼$    ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼☼☼☼☼☼☼", Direction.UP);

        assertAI("☼☼☼☼☼☼☼☼" +
                "☼☼    ○☼" +
                "☼☼     ☼" +
                "☼☼  ▲  ☼" +
                "☼☼$ ╙  ☼" +
                "☼☼     ☼" +
                "☼☼     ☼" +
                "☼☼☼☼☼☼☼☼", Direction.LEFT);
    }

    @Test
    public void preferApplesOrGoldWhenShort() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼         ○                 ☼" +
                "☼#                           ☼" +
                "☼☼  ○    ●         ◄══╕      ☼" +
                "☼☼                           ☼" +
                "☼☼ ○         ●    ○          ☼" +
                "☼☼     ☼☼☼☼☼                 ☼" +
                "☼☼     ☼                     ☼" +
                "☼#     ☼☼☼     ○  ☼☼☼*ø      ☼" +
                "☼☼     ☼          ☼   ☼      ☼" +
                "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼" +
                "☼☼                ☼          ☼" +
                "☼☼                ☼         $☼" +
                "☼☼       ○                   ☼" +
                "*ø             ○      ○      ☼" +
                "☼☼           ○               ☼" +
                "☼☼        ☼☼☼  ®             ☼" +
                "☼☼       ☼  ☼                ☼" +
                "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼" +
                "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼" +
                "☼#   æ  ☼   ☼     ☼ $☼  ☼    ☼" +
                "☼☼   └>   ®       ☼     ☼    ☼" +
                "☼☼                ☼     ☼    ☼" +
                "☼☼                           ☼" +
                "☼☼                   ●       ☼" +
                "☼☼             ●         ○   ☼" +
                "☼#  ○                        ☼" +
                "☼☼                           ☼" +
                "☼☼                           ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.DOWN);
    }

    @Test
    public void preferStonesIfLong() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼         ○                 ☼" +
                "☼#                           ☼" +
                "☼☼  ○    ●         ◄═══╕     ☼" +
                "☼☼                           ☼" +
                "☼☼ ○         ○    ○          ☼" +
                "☼☼     ☼☼☼☼☼                 ☼" +
                "☼☼     ☼                     ☼" +
                "☼#     ☼☼☼     ○  ☼☼☼*ø      ☼" +
                "☼☼     ☼          ☼   ☼      ☼" +
                "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼" +
                "☼☼                ☼          ☼" +
                "☼☼                ☼         $☼" +
                "☼☼       ○                   ☼" +
                "*ø             ○      ○      ☼" +
                "☼☼           ○               ☼" +
                "☼☼        ☼☼☼  ®             ☼" +
                "☼☼       ☼  ☼                ☼" +
                "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼" +
                "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼" +
                "☼#   æ  ☼   ☼     ☼ $☼  ☼    ☼" +
                "☼☼   └>   ®       ☼     ☼    ☼" +
                "☼☼                ☼     ☼    ☼" +
                "☼☼                           ☼" +
                "☼☼                   ●       ☼" +
                "☼☼             ●         ○   ☼" +
                "☼#  ○                        ☼" +
                "☼☼                           ☼" +
                "☼☼                           ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.LEFT);
    }

    @Test
    public void exception1() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼®           ®  ® ▲         ☼" +
                "☼#                 ║ ©       ☼" +
                "☼☼×>     ●        ╔╝    ○  ○ ☼" +
                "☼☼       ╘══╗     ║     ○    ☼" +
                "☼☼  ●       ║●  ╔═╝          ☼" +
                "☼☼     ☼☼☼☼☼║   ║            ☼" +
                "☼☼     ☼    ╚══╗║©   ●       ☼" +
                "*ø    ●☼☼☼     ╚╝ ☼☼☼☼#      ☼" +
                "☼☼     ☼          ☼   ☼  ●   ☼" +
                "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼" +
                "☼☼                ☼          ☼" +
                "☼☼       ©        ☼         $☼" +
                "☼☼    ● ○                    ☼" +
                "*ø                           ☼" +
                "☼☼             ●             ☼" +
                "☼☼        ☼☼☼                ☼" +
                "☼☼       ☼  ☼                ☼" +
                "☼☼      ☼☼☼☼#     ☼☼   *ø    ☼" +
                "☼☼      ☼   ☼ $ ● ☼ ☼ ☼ ☼    ☼" +
                "☼#      ☼   ☼     ☼  ☼  ☼   ○☼" +
                "☼☼                ☼     ☼    ☼" +
                "☼☼     ●          ☼     ☼    ☼" +
                "☼☼                           ☼" +
                "☼☼                           ☼" +
                "☼☼             ●             ☼" +
                "☼#          ○                ☼" +
                "☼☼                           ☼" +
                "☼☼        ○                  ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.RIGHT);
    }

    @Test
    public void exception2() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼         ○                 ☼" +
                "☼#                           ☼" +
                "☼☼  ○    ●                   ☼" +
                "☼☼      $                    ☼" +
                "☼☼ ○         ●    æ          ☼" +
                "☼☼  ®  ☼☼☼☼☼      └──> ○     ☼" +
                "☼☼     ☼  ◄════╕             ☼" +
                "☼#     ☼☼☼        ☼☼☼☼#      ☼" +
                "☼☼     ☼          ☼   ☼  ●   ☼" +
                "☼☼     ☼☼☼☼#      ☼☼☼*ø      ☼" +
                "☼☼                ☼          ☼" +
                "☼☼ ×──>           ☼         $☼" +
                "☼☼    ●  ○                   ☼" +
                "☼#                    ○      ☼" +
                "☼☼                           ☼" +
                "☼☼        ☼☼☼                ☼" +
                "☼☼       ☼  ☼●               ☼" +
                "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼" +
                "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼" +
                "☼#      ☼   ☼     ☼  ☼  ☼    ☼" +
                "☼☼                ☼     ☼    ☼" +
                "☼☼     ●          ☼     ☼    ☼" +
                "☼☼                           ☼" +
                "☼☼                  ○        ☼" +
                "☼☼ ○    ○      ●         ○   ☼" +
                "☼#            ○    ×>        ☼" +
                "☼☼           ○   ○           ☼" +
                "☼☼                           ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.DOWN);
    }

    @Test
    public void exception3() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼         ○                 ☼" +
                "☼#                        ×> ☼" +
                "☼☼  ○    ●         ○       ○ ☼" +
                "☼☼                     $○    ☼" +
                "☼☼ ○         ●    ○          ☼" +
                "☼☼     ☼☼☼☼☼                 ☼" +
                "☼☼     ☼ ○                   ☼" +
                "☼#     ☼☼☼     ○  ☼☼☼☼#      ☼" +
                "☼☼     ☼    ○     ☼   ☼  ●   ☼" +
                "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼" +
                "☼☼                ☼          ☼" +
                "☼☼○               ☼         $☼" +
                "☼☼    ●  ◄═══╗               ☼" +
                "*ø           ╙           ×─> ☼" +
                "☼☼        ©                  ☼" +
                "☼☼        ☼☼☼                ☼" +
                "☼☼   ○   ☼  ☼                ☼" +
                "☼☼      ☼☼☼☼#     ☼☼   *ø    ☼" +
                "☼☼      ☼   ☼   ● ☼ ☼ ☼ ☼ ○  ☼" +
                "☼#      ☼   ☼     ☼  ☼  ☼   ©☼" +
                "☼☼                ☼     ☼    ☼" +
                "☼☼     ●          ☼     ☼    ☼" +
                "☼☼                           ☼" +
                "☼☼                  ○        ☼" +
                "☼☼ ○    ○      ●         ○   ☼" +
                "☼#              $            ☼" +
                "☼☼               ○           ☼" +
                "☼☼                           ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.LEFT);
    }

    @Test
    // this needs to be changed actually, going up is bad
    public void exception4() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼               ●           ☼" +
                "*ø                           ☼" +
                "☼☼      ○●                   ☼" +
                "☼☼           ●               ☼" +
                "☼☼           ●               ☼" +
                "☼☼     ☼☼☼☼☼     ● ○         ☼" +
                "☼☼  ®  ☼                     ☼" +
                "☼#     ☼☼☼        ☼☼☼☼#      ☼" +
                "☼☼  ©  ☼          ☼   ☼○     ☼" +
                "☼☼     ☼☼☼☼#      ☼☼☼☼#      ☼" +
                "☼☼                ☼          ☼" +
                "☼☼®               ☼          ☼" +
                "☼☼    ●    ®                 ☼" +
                "☼#                           ☼" +
                "☼☼                           ☼" +
                "☼☼        ☼☼☼   ●            ☼" +
                "☼☼       ☼  ☼                ☼" +
                "☼☼      ☼☼☼*ø     ☼☼   ☼#    ☼" +
                "☼☼      ☼   ☼     ☼●☼ ☼ ☼    ☼" +
                "☼#      ☼   ☼     ☼  ☼ ○☼    ☼" +
                "☼☼                ☼    ●☼    ☼" +
                "☼☼   ˄ ●          ☼   ˄ ☼    ☼" +
                "☼☼   ¤              ┌─┘▲     ☼" +
                "☼☼                ╘═──═╝     ☼" +
                "☼☼                           ☼" +
                "☼#                           ☼" +
                "☼☼                           ☼" +
                "☼☼                           ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.UP);
    }

    @Test
    public void exception5() {
        assertAI("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼" +
                "☼☼┌>                         ☼" +
                "*ø│         ®                ☼" +
                "☼☼└──ö   ●         ○         ☼" +
                "☼☼                      ○    ☼" +
                "☼☼           ●    ○          ☼" +
                "☼☼     ☼☼☼☼☼                 ☼" +
                "☼☼     ☼                     ☼" +
                "☼#     ☼☼☼     ○  ☼☼☼☼#      ☼" +
                "☼☼     ☼          ☼   ☼  ●   ☼" +
                "☼☼  ○  ☼☼☼*ø    ○ ☼☼☼☼# ○    ☼" +
                "☼☼                ☼ ○        ☼" +
                "☼☼                ☼        ®$☼" +
                "☼☼    ●  $                   ☼" +
                "*ø             ○ ○    ○      ☼" +
                "☼☼    ○                    ® ☼" +
                "☼☼        ☼☼☼    $           ☼" +
                "☼☼       ☼  ☼                ☼" +
                "☼☼      ☼☼☼☼#     ☼☼   ☼#    ☼" +
                "☼☼      ☼   ☼   ● ☼●☼ ☼ ☼ ○  ☼" +
                "☼#      ☼   ☼     ☼  ☼  ☼    ☼" +
                "☼☼   ○          © ☼     ☼    ☼" +
                "☼☼     ●          ☼   ╓ ☼    ☼" +
                "☼☼                    ║      ☼" +
                "☼☼                    ║      ☼" +
                "☼☼             ●      ╚══╗   ☼" +
                "☼#                       ║   ☼" +
                "☼☼   ©                ◄╗ ║   ☼" +
                "☼☼                     ╚═╝   ☼" +
                "☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼", Direction.UP);
    }

    private Board board(String board) {
        return (Board) new MyBoard().forString(board);
    }

    private void assertAI(String board, Direction expected) {
        String actual = ai.get(board(board));
        assertEquals(expected.toString(), actual);
    }

    private void dice(Direction direction) {
        when(dice.next(anyInt())).thenReturn(direction.value());
    }
}
