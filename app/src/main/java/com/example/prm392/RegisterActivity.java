package com.example.prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister, buttonNavigateToLogin;
    private SharedPreferences sharedPreferences;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        soundManager = SoundManager.getInstance(this);

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonNavigateToLogin = findViewById(R.id.buttonNavigateToLogin);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playClickSound();
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(username, password);
                    editor.apply();

                    soundManager.playSuccessSound();
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish(); 
                }
            }
        });

        buttonNavigateToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playClickSound();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); 
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager != null) {
            soundManager.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.onPause();
        }
    }
}
