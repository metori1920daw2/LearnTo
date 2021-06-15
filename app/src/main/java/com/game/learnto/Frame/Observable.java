package com.game.learnto.Frame;

import android.graphics.Bitmap;

import java.util.List;

public interface Observable {
     void registerObserverPaint(ObserverPaint observer);
     void removeObserverPaint(ObserverPaint observer);
    void registerObserverCamera(ObserverCamera observer);
    void removeObserverCamera(ObserverCamera observer);
    void notifyObserversCamera(String predic, boolean ok );
    void notifyObserversCamera(boolean IsProcessing);
    void notifyObserversPaint(String predic, boolean ok);
}
