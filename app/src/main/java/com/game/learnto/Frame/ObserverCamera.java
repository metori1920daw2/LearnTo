package com.game.learnto.Frame;

import android.graphics.Bitmap;

import java.util.List;

public interface ObserverCamera {
    void UpdateRecognition(String predic, boolean Ok);
    void UpdateRecognition(boolean IsProcessing);
}
