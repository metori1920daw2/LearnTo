package com.game.learnto.CustomViews;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import org.opencv.core.Mat;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class Classifier {
    public enum Device {
        CPU,
        NNAPI,
        GPU
    }
    public static final String TAG = "ClassifierWithTaskApi";
    /** Number of results to show in the UI. */
    private static final int MAX_RESULTS = 3;

    /** Image size along the x axis. */
    private  int imageSizeX;

    /** Image size along the y axis. */
    private  int imageSizeY;
    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    private  ImageClassifier imageClassifier;

    //allocate buffer and create interface
    public  Classifier(Activity activity, Device device, int numThreads) throws IOException {
        if (device != Device.CPU) {
            throw new IllegalArgumentException( "Manipulating the hardware accelerators is not allowed in the Task"+ " library currently. Only CPU is allowed.");
        }
        // Create the ImageClassifier instance.
        ImageClassifier.ImageClassifierOptions options =
                ImageClassifier.ImageClassifierOptions.builder()
                        .setMaxResults(MAX_RESULTS)
                        .setNumThreads(numThreads)
                        .build();
        imageClassifier = ImageClassifier.createFromFileAndOptions(activity, getModelPath(), options);
        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");

        // Get the input image size information of the underlying tflite model.
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(activity, getModelPath());
        MetadataExtractor metadataExtractor = new MetadataExtractor(tfliteModel);
        // Image shape is in the format of {1, height, width, 3}.
        int[] imageShape = metadataExtractor.getInputTensorShape(/*inputIndex=*/ 0);
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
    }
    public static class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /** Display name for the recognition. */
        private final String title;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /** Optional location within the source image for the location of the recognized object. */
        private RectF location;

        public Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }
    public List<Recognition> recognizeImage(final Bitmap bitmap, int sensorOrientation) {
        // Logs this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        TensorImage inputImage = TensorImage.fromBitmap(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int cropSize = min(width, height);
        // TODO(b/169379396): investigate the impact of the resize algorithm on accuracy.
        // Task Library resize the images using bilinear interpolation, which is slightly different from
        // the nearest neighbor sampling algorithm used in lib_support. See
        // https://github.com/tensorflow/examples/blob/0ef3d93e2af95d325c70ef3bcbbd6844d0631e07/lite/examples/image_classification/android/lib_support/src/main/java/org/tensorflow/lite/examples/classification/tflite/Classifier.java#L310.
        ImageProcessingOptions imageOptions =
                ImageProcessingOptions.builder()
                        .setOrientation(getOrientation(sensorOrientation))
                        // Set the ROI to the center of the image.
                        .setRoi(
                                new Rect(
                                        /*left=*/ (width - cropSize) / 2,
                                        /*top=*/ (height - cropSize) / 2,
                                        /*right=*/ (width + cropSize) / 2,
                                        /*bottom=*/ (height + cropSize) / 2))
                        .build();

        // Runs the inference call.
        Trace.beginSection("runInference");
        long startTimeForReference = SystemClock.uptimeMillis();
        List<Classifications> results = imageClassifier.classify(inputImage, imageOptions);
        long endTimeForReference = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.v(TAG, "Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

        Trace.endSection();

        return getRecognitions(results);
    }


    private static List<Recognition> getRecognitions(List<Classifications> classifications) {

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        // All the demo models are single head models. Get the first Classifications in the results.
        for (Category category : classifications.get(0).getCategories()) {
            recognitions.add(
                    new Recognition("" + category.getLabel(), category.getLabel(), category.getScore(), null));
        }
        return recognitions;
    }
    private static ImageProcessingOptions.Orientation getOrientation(int cameraOrientation) {
        switch (cameraOrientation / 90) {
            case 3:
                return ImageProcessingOptions.Orientation.BOTTOM_LEFT;
            case 2:
                return ImageProcessingOptions.Orientation.BOTTOM_RIGHT;
            case 1:
                return ImageProcessingOptions.Orientation.TOP_RIGHT;
            default:
                return ImageProcessingOptions.Orientation.TOP_LEFT;
        }
    }
    private String getModelPath() {
        // you can download this file from
        // see build.gradle for where to obtain this file. It should be auto
        // downloaded into assets.
        return "chars.tflite";
    }

}
