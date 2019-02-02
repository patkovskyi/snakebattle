package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.client.WebSocketRunner;
import java.net.URI;

public class Main {
  public static void main(String[] args) {
    // WebSocketRunner.runClient("https://192.168.1.1:8080/codenjoy-contest/board/player/patkovskyi@gmail.com?code=6001978481505125210",
    // WebSocketRunner.runClient("https://game2.epam-bot-challenge.com.ua/codenjoy-contest/board/player/patkovskyi@gmail.com?code=6001978481505125210",
    //                new ClosestBestSolver(new RandomDice()),
    //                new Board());

    WebSocketRunner.run(
        URI.create(
            "wss://game2.epam-bot-challenge.com.ua/codenjoy-contest/ws?user=patkovskyi@gmail.com&code=6001978481505125210"),
        new ClosestBestSolver(),
        new Board(),
        1000);
  }
}
