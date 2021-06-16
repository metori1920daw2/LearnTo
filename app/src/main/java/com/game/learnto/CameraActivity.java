package com.game.learnto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.game.learnto.Frame.ClassifierManager;
import com.game.learnto.Frame.ObserverCamera;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements ImageReader.OnImageAvailableListener, ObserverCamera {

    private int sensorOrientation;
    private TextView textResult;
    private final String TAG = "MainActivity";
    Interpreter tflite;
    private Handler handler;
    private HandlerThread handlerThread;
    private LinearLayout mBottomSheetLayout;
    private BottomSheetBehavior sheetBehavior;
    private ImageView header_Arrow_Image;
    private Toolbar toolbarCamera;
    ClassifierManager classifierManager = null;
    Spinner spinner = null;
    CameraConnectionFragment camera2Fragment = null;
    Button openCamera, closeCamera, prova;
    private Bitmap rgbFrameBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        toolbarCamera = findViewById(R.id.toolbarcamera);
        setSupportActionBar(toolbarCamera);
        textResult = findViewById(R.id.txt_prediccio);
        mBottomSheetLayout = findViewById(R.id.bottom_sheet_layout_camera);
        sheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        header_Arrow_Image = findViewById(R.id.bottom_sheet_arrow_camera);


        spinner =  findViewById(R.id.modelCamera);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.models_disponibles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        openCamera = findViewById(R.id.playCamera);
        closeCamera = findViewById(R.id.StopCamera);
        closeCamera.setOnClickListener(v -> {
            if(camera2Fragment !=null)
                camera2Fragment.closeCamera();

        });

        openCamera.setOnClickListener(v -> {
            if(camera2Fragment !=null){
                camera2Fragment.openCamera(640, 480);
                setFragment();
            }


        });

        classifierManager = ClassifierManager.getInstance(this);
        classifierManager.registerObserverCamera(this);


        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else{
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                header_Arrow_Image.setRotation(slideOffset * 180);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 121);
        } else {
            setFragment();
        }



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setFragment();
        } else {
            finish();
        }
    }



    int previewHeight = 0,previewWidth = 0;

    protected void setFragment() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Fragment fragment;
        camera2Fragment = CameraConnectionFragment.newInstance((size, rotation) -> {
                    previewHeight = size.getHeight();
                    previewWidth = size.getWidth();
                    Log.d("tryOrientation","rotation: "+rotation+"   orientation: "+getScreenOrientation()+"  "+previewWidth+"   "+previewHeight);
                    sensorOrientation = rotation;
                },
                this,R.layout.camera_frame,new Size(640, 480));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

    }


    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    @Override
    public void onImageAvailable(ImageReader reader) {
        if (previewWidth == 0 || previewHeight == 0)
            return;

        if (rgbBytes == null)
            rgbBytes = new int[previewWidth * previewHeight];

        try {
            final Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter = () -> {
                ImageUtils.convertYUV420ToARGB8888(
                        yuvBytes[0], yuvBytes[1], yuvBytes[2],
                        previewWidth, previewHeight, yRowStride,
                        uvRowStride, uvPixelStride, rgbBytes);
            };

            postInferenceCallback =() -> {
                image.close();
                isProcessingFrame = false;
            };

            processImage();

        } catch (final Exception e) {

            return;
        }

    }


    private void processImage(){
        imageConverter.run();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        classifierManager.predicImageCamera(ImageUtils.rotateBitmapG(ImageUtils.doGreyscale(rgbFrameBitmap),6));



    }


    private void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.camera_page:
                displayToast("camera_page");
                break;
            case R.id.exitBtn:
                classifierManager.registerObserverCamera(this);
                FirebaseAuth.getInstance().signOut();
                startActivity( new Intent(getApplicationContext(),LoginActivity.class));
                break;
            case R.id.Paint_page:
                classifierManager.registerObserverCamera(this);
                startActivity( new Intent(getApplicationContext(),HomeActivity.class));
                break;
            default:
                displayToast("default");
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    private void displayToast(String s) {
        Toast.makeText(CameraActivity.this, s, Toast.LENGTH_SHORT).show();
    }
    @Override
    public synchronized void onResume() {
        super.onResume();
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }



    @Override
    public void UpdateRecognition(String predic, boolean Ok) {
        if(Ok){
            postInferenceCallback.run();
            textResult.setText(predic);
        }
    }

    @Override
    public void UpdateRecognition(boolean IsProcessing) {
        if(!IsProcessing){
            postInferenceCallback.run();
        }
    }
}