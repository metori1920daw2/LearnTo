package com.game.learnto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.game.learnto.CustomViews.Classifier;
import com.game.learnto.CustomViews.PaintView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.opencv.core.Mat;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    TextView name, mail,textRecunegut;
    Button logout, reset,Validar;
    PaintView paintView;
    private LinearLayout mBottomSheetLayout;
    private BottomSheetBehavior sheetBehavior;
    private ImageView header_Arrow_Image;
    private Toolbar toolbarHome;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    Model model;
    Classifier  classifier= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firebaseAuth = FirebaseAuth.getInstance();
        paintView =  findViewById(R.id.paintView);
        Spinner spinner =  findViewById(R.id.modelPaint);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.models_disponibles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        try {
            classifier = new Classifier(this,Classifier.Device.CPU,-1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // model.LoadModel();
        DisplayMetrics matrics  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(matrics);
        try {
            paintView.Init(matrics);
        } catch (IOException e) {
            e.printStackTrace();
        }
        toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);
        mBottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        sheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        header_Arrow_Image = findViewById(R.id.bottom_sheet_arrow);
        textRecunegut = findViewById(R.id.textRecunegut);

        //reset = findViewById(R.id.reset);
       // Validar = findViewById(R.id.Validar);


      currentUser=firebaseAuth.getCurrentUser();
      if(currentUser!= null)
          getSupportActionBar().setTitle(currentUser.getDisplayName());
        textRecunegut.setOnClickListener(v -> {
            paintView.stopCoounter();
        });
        header_Arrow_Image.setOnClickListener(v -> {
            if(sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
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
                startActivity( new Intent(HomeActivity.this,CameraActivity.class));
                break;
            case R.id.exitBtn:
                displayToast("exitBtn");
                FirebaseAuth.getInstance().signOut();
                startActivity( new Intent(HomeActivity.this,LoginActivity.class));
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

}