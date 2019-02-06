package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
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

import com.codenjoy.dojo.snakebattle.model.Elements;
import java.util.Set;

public class BoardStringComparator {

  private static final Set<Elements> objects =
      Set.of(
          Elements.GOLD, Elements.APPLE, Elements.STONE, Elements.FURY_PILL, Elements.FLYING_PILL);

  public static boolean movesEqual(String board1, String board2) {
    if (board1.length() != board2.length()) return false;

    for (int i = 0; i < board1.length(); i++) {
      char c1 = board1.charAt(i);
      char c2 = board2.charAt(i);
      if (c1 == c2) continue;

      if (objects.contains(Elements.valueOf(c1))) c1 = ' ';
      if (objects.contains(Elements.valueOf(c2))) c2 = ' ';
      if ((c1 == Elements.HEAD_DEAD.ch() || c1 == Elements.ENEMY_HEAD_DEAD.ch()) && c2 != ' ')
        continue;

      if ((c2 == Elements.HEAD_DEAD.ch() || c2 == Elements.ENEMY_HEAD_DEAD.ch()) && c1 != ' ')
        continue;

      if (c1 != c2) return false;
    }

    return true;
  }
}
