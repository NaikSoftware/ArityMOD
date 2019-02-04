// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.feature.graph;

class FPS {

    private int drawCnt;
    private long lastTime;
    private int fps;

    boolean incFrame() {
        if (--drawCnt > 0) {
            return false;
        }
        drawCnt = 100;
        long now = System.currentTimeMillis();
        fps = Math.round(100000f / (now - lastTime));
        lastTime = now;
        return true;
    }

    int getValue() {
        return fps;
    }    
}
