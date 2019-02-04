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


import static org.junit.Assert.assertEquals;

public class TestUtils {

  public static void assertBoardsEqual(String expected, String actual) {
    assertEquals(injectN(expected), injectN(actual));
  }

  public static String injectN(String expected) {
    int size = (int) Math.sqrt(expected.length());
    if (size * size != expected.length()) {
      throw new IllegalArgumentException("Board is not square.");
    }

    return inject(expected, size, "\n");
  }

  public static String inject(String string, int position, String substring) {
    StringBuilder result = new StringBuilder();
    for (int index = 1; index < string.length() / position + 1; index++) {
      result.append(string, (index - 1) * position, index * position).append(substring);
    }
    result.append(string.substring((string.length() / position) * position));
    return result.toString();
  }
}
