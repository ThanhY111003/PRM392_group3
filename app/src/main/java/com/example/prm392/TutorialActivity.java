package com.example.prm392;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        Button btnBack = findViewById(R.id.btnTutorial);
        btnBack.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClickSound();
            finish();
        });
    }
}