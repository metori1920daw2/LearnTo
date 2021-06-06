package com.game.learnto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Model {
    private static final String TAG = "Model";
    private static Interpreter tflite = null;
    private static Context ctx;
    private static  TensorBuffer  probabilityBuffer = null;
    private static ByteBuffer imgData = null;
    private static final int PIXEL_WIDTH = 32;
    private static final int DIM_BATCH_SIZE = 1;
    private static final int  DIM_IMG_SIZE_X = 32;
    private static final int DIM_IMG_SIZE_Y = 32;
    private static final int DIM_PIXEL_SIZE = 1;
    public Model(Context ctx) {
        this.ctx = ctx;
        probabilityBuffer =TensorBuffer.createFixedSize(new int[]{1,DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE}, DataType.FLOAT32);
        imgData = ByteBuffer.allocateDirect(1 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());


    }
    public static TensorImage getImageTensor(Bitmap bitmap){
        ImageProcessor imageProcessor =new ImageProcessor.Builder().add(new ResizeOp(32, 32, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0,  1/255))
                .build();
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);
        return imageProcessor.process(tensorImage);
    }
    public  void LoadModel(){
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder().requireWifi().build();
        FirebaseModelDownloader.getInstance()
                .getModel("charsV2", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(model -> {
                    File modelFile = model.getFile();
                    if (modelFile != null) {
                        tflite = new Interpreter(modelFile);
                        System.out.println("MODEL CARREGAT: "+ tflite.getOutputTensorCount());
                        Log.i(TAG, "MODEL CARREGAT: "+ tflite.getOutputTensorCount());
                    }else{
                        System.out.println("ERROR EN LA CARREGA DEL MODEL");
                        Log.i(TAG, "ERROR EN LA CARREGA DEL MODEL");
                    }
                });

    }

    public static  void doInference(TensorImage tensor) {
        TensorProcessor probabilityProcessor =new TensorProcessor.Builder().add(new NormalizeOp(0, 1.0f/255.0f)).build();
        System.out.println("llllllllllllllllllllllllllllllllllllllllllllllllllllllllllll: "+ tensor.getTensorBuffer().getFloatArray().length);
        tflite.run(tensor.getBuffer(), probabilityBuffer.getBuffer().rewind());
        //System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm:"+probabilityProcessor.process(probabilityBuffer).getFloatArray());



    }
    public static void GetResultInference() {
        imgData.rewind();
        FloatBuffer probabilities = imgData.asFloatBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ctx.getAssets().open("labels.txt")));
            for (int i = 0; i < probabilities.capacity(); i++) {
                String label = reader.readLine();
                float probability = probabilities.get(i);
                Log.i(TAG, String.format("%s : %1.4f", label, probability));
            }
        } catch (IOException e) {
            System.out.println("ppppppppppppppppppppppppppppppppppppppppppppppp: "+e);
            Log.i(TAG,e.getMessage().toString());
        }
    }
    public static void ReadInput(ByteBuffer buffer) {

        FloatBuffer probabilities = buffer.asFloatBuffer();
            for (int i = 0; i < probabilities.capacity(); i++) {
                float probability = probabilities.get(i);
                System.out.println("probability: "+probability);

            }
    }
    public static void doInference(ByteBuffer buffer) {
        //System.out.println(buffer.asFloatBuffer());
        tflite.run(buffer,probabilityBuffer.getBuffer().rewind());
        System.out.println("input.asFloatBuffer().capacity(): "+probabilityBuffer.getBuffer().capacity());
    }
    private static void read(ByteBuffer buffer){
        buffer.rewind();

        while (buffer.hasRemaining())
            System.out.println(buffer.get());
    }
    public static ByteBuffer getByteBufferDos(Bitmap bitmap){

        //Bitmap bitmap = Bitmap.createScaledBitmap(bp, 32, 32, true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer input = ByteBuffer.allocateDirect(width * height * 1 * 4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                int px = bitmap.getPixel(x, y);
                // Get channel values from the pixel value.
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);


                // Normalize channel values to [-1.0, 1.0]. This requirement depends
                // on the model. For example, some models might require values to be
                // normalized to the range [0.0, 1.0] instead.
                float rf = r/ 255.0f;
                float gf = g / 255.0f;
                float bf = b  / 255.0f;
                input.putFloat(rf);
            }
        }

        //System.out.println("input.asFloatBuffer().capacity(): "+input.capacity());
    return input;
    }
   static boolean isGrayScalePixel(int pixel){
        int alpha = (pixel & 0xFF000000) >> 24;
        int red   = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue  = (pixel & 0x000000FF);

        if( 0 == alpha && red == green && green == blue ) return true;
        else return false;

    }
    public static ByteBuffer getByteBuffer(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer mImgData = ByteBuffer.allocateDirect(4 * width * height);
        mImgData.order(ByteOrder.nativeOrder());
        int[] pixels = new int[width*height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int pixel : pixels) {
            float value = (float) Color.red(pixel)/255.0f;
            mImgData.putFloat(value);
        }
        return mImgData;
    }
    public static  void doInference(float[][][][] img) {
        //probabilityBuffer.getBuffer().clear();
        TensorProcessor probabilityProcessor =new TensorProcessor.Builder().add(new NormalizeOp(0, 1.0f/255.0f)).build();
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa: "+img);
        tflite.run(img, probabilityBuffer.getBuffer().rewind());
        System.out.println("llllllllllllllllllllllllllllllllllllllllllllllllllllllllllll: "+ Arrays.toString( (float[]) probabilityProcessor.process(probabilityBuffer).getFloatArray()).length() );
        //System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm:"+probabilityProcessor.process(probabilityBuffer).getFloatArray());



    }
    public  static void GetLabels() {
        final String ASSOCIATED_AXIS_LABELS = "labels.txt";
        List<String> associatedAxisLabels = null;
        Map<String, Float> floatMap = null;
        try {
            associatedAxisLabels = FileUtil.loadLabels(ctx, ASSOCIATED_AXIS_LABELS);
            TensorProcessor probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0, 1/255)).build();
            if (null != associatedAxisLabels  && probabilityBuffer != null) {

                System.out.println(" probabilityBuffer.getBuffer().capacity(): "+ probabilityBuffer.getBuffer().capacity());
                TensorLabel labels = new TensorLabel(associatedAxisLabels,probabilityProcessor.process(probabilityBuffer));
                // Create a map to access the result based on label
                floatMap= labels.getMapWithFloatValue();
            }
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }

    }
    public static float[][][][] bitmapToInputArray(Bitmap img) {
        Bitmap bitmap= Bitmap.createScaledBitmap(img, PIXEL_WIDTH, PIXEL_WIDTH, true);
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();

        int batchNum = 0;
        float[][][][] input = new float[1][PIXEL_WIDTH][PIXEL_WIDTH][1];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bitmap.getPixel(x, y);
                // to the range [0.0, 1.0] instead.
                //input[batchNum][x][y][0] = Color.red(pixel) / 255.0f;
                System.out.println("Color.red(pixel) / 255.0f: "+pixel);

            }
        }

        return input;
    }
    public static void runTextRecognition(Bitmap img) {
        InputImage image = InputImage.fromBitmap(img, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image).addOnSuccessListener(texts -> processTextRecognitionResult(texts))
                .addOnFailureListener(e -> { e.printStackTrace(); });
    }
    private static void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            System.out.println("NO es troba cap text en la image");
            return;
        }
        String  textos="";
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    textos=  textos+" "+elements.get(k).getText();;
                    //textResult.setText(textos);
                    System.out.println( "text trobat: "+textos);

                }
            }
        }
    }
    public   static  Bitmap getOutputImage(ByteBuffer output){
        output.rewind();
        int outputWidth = 32;
        int outputHeight = 32;
        Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        int [] pixels = new int[outputWidth * outputHeight];
        for (int i = 0; i < outputWidth * outputHeight; i++) {
            int a = 0xFF;
            float r = output.getFloat() * 255.0f;
            float g = output.getFloat() * 255.0f;
            float b = output.getFloat() * 255.0f;
            pixels[i] = a << 24 | ((int) r << 16) | ((int) g << 8) | (int) b;
        }
        bitmap.setPixels(pixels, 0, outputWidth, 0, 0, outputWidth, outputHeight);
        return bitmap;
    }
}
