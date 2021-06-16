package com.game.learnto.classes;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public abstract class Text {
    private final String TAG = this.getClass().getSimpleName();
    protected char[] labels = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    protected Interpreter tflite;
    protected ByteBuffer inputBuffer = null;

    protected float[][] Output = null;
    protected static final String MODEL_PATH = "chars.tflite";
    protected static final int NUMBER_LENGTH = 36;


    protected static final int DIM_BATCH_SIZE = 1;
    protected static final int DIM_IMG_SIZE_X = 32;
    protected static final int DIM_IMG_SIZE_Y = 32;
    protected static final int DIM_PIXEL_SIZE = 1;


    protected static final int BYTE_SIZE_OF_FLOAT = 4;
    public Text(Activity activity){
        try {
            tflite = new Interpreter(loadModel(activity));
            inputBuffer = ByteBuffer.allocateDirect( BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
            inputBuffer.order(ByteOrder.nativeOrder());
            Output = new float[DIM_BATCH_SIZE][NUMBER_LENGTH];
            Log.d(TAG, "Carregant model");
        } catch (IOException e) {
            Log.e(TAG, "IOException, ERROR en la carrega del model  tflite");
        }
    }
    private MappedByteBuffer loadModel(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    protected void runInference() {
        tflite.run(inputBuffer, Output);
    }
    public String classify(Bitmap bitmap) {
      int  h= bitmap.getHeight();
        int  w=  bitmap.getWidth();
        System.out.println("getHeight: "+h);
        System.out.println("getWidth: "+w);
        String Resultlabel = null;
        if (tflite == null) {
            Log.e(TAG, "ERROR; model no inicialitzada");
        }
        preprocess(bitmap);
        runInference();
        int result = maxProbIndex(Output[0]);
        if(result !=-1){
            Resultlabel= String.valueOf(Array.getChar(labels,result));
        }
        return Resultlabel;
    }

    private  int maxProbIndex(float[] probs) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                System.out.println(probs[i]);
                //maxProb  = Float.parseFloat(df.format(probs[i]));
                maxProb =  probs[i];
                if (maxProb > 0.70){
                    System.out.println("maxProb: "+maxProb);
                    maxIndex = i;
                }

            }
        }
        return maxIndex;
    }



    protected void preprocess(Bitmap bitmap) {
        if (bitmap == null || inputBuffer == null) {
            return;
        }
        inputBuffer.rewind();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        long startTime = SystemClock.uptimeMillis();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; ++i) {
            int pixel = pixels[i];
            int channel = pixel & 0xff;
            inputBuffer.putFloat(((0xff - channel)/255.0f));
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Temps de processament del model: " + Long.toString(endTime - startTime));
    }
}
