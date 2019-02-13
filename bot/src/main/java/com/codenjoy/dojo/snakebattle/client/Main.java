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

import com.codenjoy.dojo.client.WebSocketRunner;
import java.io.IOException;
import java.net.URI;

public class Main {

  public static void main(String[] args) throws IOException {
    // WebSocketRunner.runClient("https://192.168.1.1:8080/codenjoy-contest/board/player/patkovskyi@gmail.com?code=6001978481505125210",
//    WebSocketRunner.runClient(
//        "https://game3.epam-bot-challenge.com.ua/codenjoy-contest/board/player/pzpz4ael0yhbfcvoghei?code=7636115390628474462",
//        new MySolver(),
//        new MyBoard());

//    String urlString = "https://game3.epam-bot-challenge.com.ua/codenjoy-contest/rest/player/pzpz4ael0yhbfcvoghei/7636115390628474462/reset";
//    // String urlString = "https://snakebattle.tk/codenjoy-contest/rest/player/patkovskyi@unreal.com/16412357891426752377/reset";
//    URL url = new URL(urlString);
//    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
//    System.out.println("Content: " + bufferedReader.lines().collect(Collectors.joining("\n")));
//    bufferedReader.close();

    WebSocketRunner.run(
        URI.create(
            "wss://game3.epam-bot-challenge.com.ua/codenjoy-contest/ws?user=pzpz4ael0yhbfcvoghei&code=7636115390628474462"),
            // "wss://snakebattle.tk/codenjoy-contest/ws?user=patkovskyi@unreal.com&code=16412357891426752377"),
        new MySolver(),
        new MyBoard(),
        1000);
  }
}
