package com.game.learnto.Frame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.game.learnto.ImageUtils;
import com.game.learnto.classes.Fotografia;
import com.game.learnto.classes.Manual;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;

public class ClassifierManager  implements Observable {
    private ArrayList<ObserverCamera> observersCamera;
    private ArrayList<ObserverPaint> observersPaint;
    private static ClassifierManager instance;
    private Context ctx;
    private Manual manual = null;
    private Fotografia fotografia = null;


    public ClassifierManager(Context context) {
        this.ctx = context;
        observersCamera = new ArrayList<>();
        observersPaint = new ArrayList<>();
        manual = new Manual((Activity) context);
        fotografia = new Fotografia((Activity) context);
    }

    public static ClassifierManager getInstance(Context context) {
        if (instance == null) {
            instance = new ClassifierManager(context);
        }
        return instance;
    }
    @Override
    public void registerObserverPaint(ObserverPaint observer) {
       observersPaint.add(observer);

    }

    @Override
    public void removeObserverPaint(ObserverPaint observer) {
        observersPaint.remove(observersPaint.indexOf(observer));
    }

    @Override
    public void registerObserverCamera(ObserverCamera observer) {
        observersCamera.add(observer);
    }

    @Override
    public void removeObserverCamera(ObserverCamera observer) {
        observersPaint.remove(observersPaint.indexOf(observer));
    }



    @Override
    public void notifyObserversCamera(String predic, boolean ok) {
        for (ObserverCamera observer :observersCamera) {
            observer.UpdateRecognition(predic,ok);
        }
    }

    @Override
    public void notifyObserversCamera(boolean IsProcessing) {
        for (ObserverCamera observer :observersCamera) {
            observer.UpdateRecognition(IsProcessing);
        }
    }


    @Override
    public void notifyObserversPaint(String predic, boolean ok) {
        for (ObserverPaint observer :observersPaint) {
            observer.UpdateRecognition(predic,ok);
        }
    }
    public   void predicImagePaint(Bitmap bitmap){
        String predictionResult =  manual.classify(Bitmap.createScaledBitmap(ImageUtils.cropCenter(bitmap), 32, 32, false));
        if(predictionResult != null){
            System.out.println( "custom model: "+predictionResult);
            notifyObserversPaint(predictionResult,true);

        }
    }


    public void predicImagePaint(String predict, boolean ok){
        notifyObserversPaint(predict, ok);
    }
    public void predicImageCamera(Bitmap bitmap) {
        String predictionResult =  fotografia.classify(Bitmap.createScaledBitmap(ImageUtils.cropCenter(bitmap), 32, 32, false));
        System.out.println( "custom model: "+predictionResult);
        if(predictionResult != null){
            System.out.println( "predicImageCamera custom model: "+predictionResult);
            notifyObserversCamera(predictionResult, true);
        }else{
            System.out.println( "final de processament resultat null : "+predictionResult);
            notifyObserversCamera(false);
        }

    }
    private   void runTextRecognition(Bitmap img) {
        InputImage image = InputImage.fromBitmap(img, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image).addOnSuccessListener(texts -> processTextRecognitionResult(texts)).addOnFailureListener(e -> { e.printStackTrace(); });
    }
    private  void processTextRecognitionResult(Text texts) {
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
                    predicImagePaint(textos,true);
                    System.out.println( "text trobat: "+textos);

                }
            }
        }
    }
}
