package com.codenjoy.dojo.snakebattle.client;

import com.codenjoy.dojo.services.EventListener;

public class EmulatingEventListener implements EventListener {

  private final int heroIndex;

  public EmulatingEventListener(int heroIndex) {
    this.heroIndex = heroIndex;
  }

  @Override
  public void event(Object event) {
    // System.out.printf("Hero %d event: %s\n", heroIndex, event);
  }
}
