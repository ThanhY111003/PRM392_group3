package com.example.prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvBalance;
    private Button btnTopup, btnStartBet;
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        soundManager = SoundManager.getInstance(this);

        initViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

        currentUsername = getIntent().getStringExtra("username");

        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }

        tvPlayerName.setText(currentUsername);
        updateBalanceDisplay();

        btnTopup.setOnClickListener(v -> {
            soundManager.playClickSound();
            showTopupDialog();
        });

        btnStartBet.setOnClickListener(v -> {
            soundManager.playClickSound();
            Toast.makeText(this, "Chuyen sang man hinh dua xe...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, RaceActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
    }

    private void initViews() {
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvBalance = findViewById(R.id.tvBalance);
        btnTopup = findViewById(R.id.btnTopup);
        btnStartBet = findViewById(R.id.btnStartBet);
    }

    private void updateBalanceDisplay() {
        long currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);
        tvBalance.setText("So du: " + currentBalance + "$");
    }

    private void showTopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_topup, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmTopup);

        btnConfirm.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui long nhap so tien!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long amountToAdd = Long.parseLong(amountStr);

                long currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);

                long newBalance = currentBalance + amountToAdd;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(currentUsername + "_balance", newBalance);
                editor.apply();

                soundManager.playTopupSound();

                updateBalanceDisplay();
                Toast.makeText(this, "Da nap thanh cong: " + amountToAdd + "$", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "So tien khong hop le!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        soundManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.onPause();
        }
    }
}
