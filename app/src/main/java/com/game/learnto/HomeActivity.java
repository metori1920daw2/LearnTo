package com.game.learnto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.game.learnto.Frame.Classifier;
import com.game.learnto.CustomViews.PaintView;
import com.game.learnto.Frame.ClassifierManager;
import com.game.learnto.Frame.ObserverPaint;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ObserverPaint {

    TextView txt_prediccio1, txt_prediccio2, txt_prediccio3;
    private PaintView paintView;
    private LinearLayout mBottomSheetLayout;
    private BottomSheetBehavior sheetBehavior;
    private ImageView header_Arrow_Image;
    private Toolbar toolbarHome;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    Model model;
    ClassifierManager classifierManager = null;
    LinearLayout probabilyPredictions;
    TextView txt;
    private EditText textRecunegut;
    Button loadImage, clearnCanvas;
    String CadenaString = null;
    TextInputLayout layoutTextRecunegut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firebaseAuth = FirebaseAuth.getInstance();
        paintView = findViewById(R.id.paintView);
        Spinner spinner = findViewById(R.id.modelPaint);
        txt = findViewById(R.id.modelCarregat);
        loadImage= findViewById(R.id.loadImage);
        clearnCanvas = findViewById(R.id.CleanCanvas);
        textRecunegut = findViewById(R.id.textRecunegut);
        probabilyPredictions = findViewById(R.id.probabilitatProdiccio);
        txt_prediccio1 = findViewById(R.id.txt_prediccio1);
        txt_prediccio2 = findViewById(R.id.txt_prediccio2);
        txt_prediccio3 = findViewById(R.id.txt_prediccio3);
        layoutTextRecunegut = findViewById(R.id.Layout_textRecunegut);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.models_disponibles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        classifierManager = ClassifierManager.getInstance(this);
        classifierManager.registerObserverPaint(this);
        // model.LoadModel();
        DisplayMetrics matrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(matrics);

        paintView.Init(matrics);
        toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);
        mBottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        sheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        header_Arrow_Image = findViewById(R.id.bottom_sheet_arrow);
        textRecunegut = findViewById(R.id.textRecunegut);

        //reset = findViewById(R.id.reset);
        // Validar = findViewById(R.id.Validar);
        clearnCanvas.setOnClickListener(v -> {
            textRecunegut.setText("");
        });

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null)
            getSupportActionBar().setTitle(currentUser.getDisplayName());
        textRecunegut.setOnClickListener(v -> {
            paintView.stopCoounter();
        });
        header_Arrow_Image.setOnClickListener(v -> {
            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        });
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                header_Arrow_Image.setRotation(slideOffset * 180);
            }
        });

        loadImage.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(gallery, 100);
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        Uri imageUri;
        if (resultCode == RESULT_OK && reqCode == 100){
            //imageUri = data.getData();
            imageUri= data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                paintView.LoadImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showResultsInBottomSheet(List<Classifier.Recognition> results) {
        if (results != null && results.size() >= 3) {
            Classifier.Recognition recognition = results.get(0);
            if (recognition != null) {
                if (recognition.getConfidence() != null && recognition.getTitle() != null) {
                    txt_prediccio1.setText(recognition.getTitle() + "-->" + String.format("%.1f%n", (100 * recognition.getConfidence())) + "%");
                }

            }

            Classifier.Recognition recognition1 = results.get(1);
            if (recognition1 != null) {
                if (recognition1.getConfidence() != null) {
                    txt_prediccio2.setText(recognition1.getTitle() + "-->" + String.format("%.1f%n", (100 * recognition1.getConfidence())) + "%");
                }
            }

            Classifier.Recognition recognition2 = results.get(2);
            if (recognition2 != null) {
                if (recognition2.getConfidence() != null && recognition2.getTitle() != null) {
                    txt_prediccio3.setText(recognition2.getTitle() + "-->" + String.format("%.1f%n", (100 * recognition2.getConfidence())) + "%");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera_page:
                startActivity(new Intent(HomeActivity.this, CameraActivity.class));
                break;
            case R.id.exitBtn:
                displayToast("exitBtn");
                classifierManager.removeObserverPaint(this);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                break;
            case R.id.Paint_page:
                break;
            default:
                displayToast("default");
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void displayToast(String s) {
        Toast.makeText(HomeActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private int getScreenOrientation() {
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
    public void UpdateRecognition(String predic, boolean Ok) {
        CadenaString = textRecunegut.getText().toString();
        if(Ok){
            layoutTextRecunegut.setErrorEnabled(false);
            CadenaString  =CadenaString+""+predic;
            textRecunegut.setText(CadenaString);
        }else{
            layoutTextRecunegut.setErrorEnabled(true);
            layoutTextRecunegut.setError(predic);
            hideKeyboardFrom(textRecunegut);
        }

    }

    private   void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}