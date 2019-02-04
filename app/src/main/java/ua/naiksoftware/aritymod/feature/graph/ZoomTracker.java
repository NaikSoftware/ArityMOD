// Copyright (C) 2010 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.feature.graph;

class ZoomTracker {

    private float sx1, sy1, sx2, sy2;

    void start(float x1, float y1, float x2, float y2) {
        sx1 = x1;
        sy1 = y1;
        sx2 = x2;
        sy2 = y2;
    }

    boolean update(float x1, float y1, float x2, float y2) {
        final float LIMIT = 1.5f;
        if (Math.abs(x1 - sx1) < LIMIT && Math.abs(y1 - sy1) < LIMIT &&
                Math.abs(x2 - sx2) < LIMIT && Math.abs(y2 - sy2) < LIMIT) {
            return false;
        }
        sx1 = x1;
        sx2 = x2;
        sy1 = y1;
        sy2 = y2;
        return true;
    }
}
