import com.codenjoy.dojo.snakebattle.model.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TestInverter {

  public static void main(String[] args) throws IOException {
    Map<Elements, Elements> m = new HashMap<>();
    m.put(Elements.HEAD_UP, Elements.ENEMY_HEAD_UP);
    m.put(Elements.HEAD_DOWN, Elements.ENEMY_HEAD_DOWN);
    m.put(Elements.HEAD_LEFT, Elements.ENEMY_HEAD_LEFT);
    m.put(Elements.HEAD_RIGHT, Elements.ENEMY_HEAD_RIGHT);
    m.put(Elements.HEAD_EVIL, Elements.ENEMY_HEAD_EVIL);
    m.put(Elements.HEAD_FLY, Elements.ENEMY_HEAD_FLY);
    m.put(Elements.HEAD_SLEEP, Elements.ENEMY_HEAD_SLEEP);
    m.put(Elements.HEAD_DEAD, Elements.ENEMY_HEAD_DEAD);

    m.put(Elements.BODY_HORIZONTAL, Elements.ENEMY_BODY_HORIZONTAL);
    m.put(Elements.BODY_VERTICAL, Elements.ENEMY_BODY_VERTICAL);
    m.put(Elements.BODY_LEFT_DOWN, Elements.ENEMY_BODY_LEFT_DOWN);
    m.put(Elements.BODY_LEFT_UP, Elements.ENEMY_BODY_LEFT_UP);
    m.put(Elements.BODY_RIGHT_DOWN, Elements.ENEMY_BODY_RIGHT_DOWN);
    m.put(Elements.BODY_RIGHT_UP, Elements.ENEMY_BODY_RIGHT_UP);

    m.put(Elements.TAIL_END_UP, Elements.ENEMY_TAIL_END_UP);
    m.put(Elements.TAIL_END_DOWN, Elements.ENEMY_TAIL_END_DOWN);
    m.put(Elements.TAIL_END_LEFT, Elements.ENEMY_TAIL_END_LEFT);
    m.put(Elements.TAIL_END_RIGHT, Elements.ENEMY_TAIL_END_RIGHT);
    m.put(Elements.TAIL_INACTIVE, Elements.ENEMY_TAIL_INACTIVE);

    Map<Character, Character> straight = m.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().ch(), e -> e.getValue().ch()));

    Map<Character, Character> inverted = m.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getValue().ch(), e -> e.getKey().ch()));

    boolean printedFirstLine = false;
    long lastRead;
    Scanner scanner = new Scanner(System.in);
    do {
      lastRead = System.currentTimeMillis();
      String line = scanner.nextLine();
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < line.length(); i++) {
        char input = line.charAt(i);
        char output = straight.getOrDefault(input, input);
        output = inverted.getOrDefault(input, output);
        sb.append(output);
      }

      if (!printedFirstLine) {
        System.out.println();
        System.out.println();
        System.out.println();
        printedFirstLine = true;
        lastRead = System.currentTimeMillis();
      }

      System.out.println(sb.toString());
    } while (System.currentTimeMillis() - lastRead < 1000);
  }
}
