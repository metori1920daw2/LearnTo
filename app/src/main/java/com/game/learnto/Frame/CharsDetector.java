package com.game.learnto.Frame;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

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
import java.util.List;

public class CharsDetector {
    private final String TAG = this.getClass().getSimpleName();
    char[] labels = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    private static int digit = -1;
    private static float  prob = 0.0f;
    // The tensorflow lite file
    private Interpreter tflite;

    // Input byte buffer
    private ByteBuffer inputBuffer = null;

    // Output array [batch_size, 10]
    private float[][] mnistOutput = null;

    // Name of the file in the assets folder
    //private static final String MODEL_PATH = "mnist.tflite";

    private static final String MODEL_PATH = "chars.tflite";
    // Specify the output size
    private static final int NUMBER_LENGTH = 36;

    // Specify the input size
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 32;
    private static final int DIM_IMG_SIZE_Y = 32;
    private static final int DIM_PIXEL_SIZE = 1;

    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float
    private static final int BYTE_SIZE_OF_FLOAT = 4;
    private Context ctx = null;
    public CharsDetector(Activity activity) {
        try {
            tflite = new Interpreter(loadModelFile(activity));
            inputBuffer = ByteBuffer.allocateDirect( BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
            inputBuffer.order(ByteOrder.nativeOrder());
            mnistOutput = new float[DIM_BATCH_SIZE][NUMBER_LENGTH];
            Log.d(TAG, "Created a Tensorflow Lite MNIST Classifier.");
        } catch (IOException e) {
            Log.e(TAG, "IOException loading the tflite file");
        }
    }

    /**
     * Run the TFLite model
     */
    protected void runInference() {
        tflite.run(inputBuffer, mnistOutput);
    }

    /**
     * Classifies the number with the mnist model.
     *
     * @param bitmap
     * @return the identified number
     */
    public String classify(Bitmap bitmap) {
        String Resultlabel = null;
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
        }
        preprocess(bitmap);
        runInference();
        //return postprocess();
       int result = maxProbIndex(mnistOutput[0]);
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
                maxProb  = Float.parseFloat(df.format(probs[i]));
                if (maxProb > 0.95){
                    System.out.println("maxProb: "+maxProb);
                    maxIndex = i;
                }

            }
        }
        return maxIndex;
    }

    /**
     * Load the model file from the assets folder
     */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Converts it into the Byte Buffer to feed into the model
     *
     * @param bitmap
     */
    private void preprocess(Bitmap bitmap) {
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
        Log.d(TAG, "Time cost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }


}
