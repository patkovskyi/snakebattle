package com.codenjoy.dojo.snakebattle.client;

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
