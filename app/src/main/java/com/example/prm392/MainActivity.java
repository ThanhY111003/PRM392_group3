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

    // Khai bao cac view
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

        // Khoi tao SoundManager
        soundManager = SoundManager.getInstance(this);

        // Anh xa View tu XML
        initViews();

        // Xu ly Padding cho he thong (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khoi tao SharedPreferences
        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

        // Lay username tu Intent gui sang tu man hinh Login
        currentUsername = getIntent().getStringExtra("username");

        // Neu khong lay duoc username (vi du mo truc tiep), dat mac dinh la Guest
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }

        // Hien thi thong tin len man hinh
        tvPlayerName.setText(currentUsername);
        updateBalanceDisplay();

        // Xu ly nut Nap tien
        btnTopup.setOnClickListener(v -> {
            soundManager.playClickSound();
            showTopupDialog();
        });

        // Xu ly nut Bat dau cuoc (Luong 3 se dung)
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
        // Doc so du tu SharedPreferences, mac dinh la 0 neu chua co
        long currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);
        tvBalance.setText("So du: " + currentBalance + "$");
    }

    private void showTopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_topup, null);
        builder.setView(dialogView);

        // Tao Dialog tu builder
        AlertDialog dialog = builder.create();

        // Anh xa cac view trong dialog_topup.xml
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmTopup);

        // Xu ly nut Xac nhan
        btnConfirm.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui long nhap so tien!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long amountToAdd = Long.parseLong(amountStr);

                // 1. Lay so du hien tai tu SharedPreferences
                long currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);

                // 2. Cong don tien
                long newBalance = currentBalance + amountToAdd;

                // 3. Luu lai vao bo nho
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(currentUsername + "_balance", newBalance);
                editor.apply();

                // 4. Cap nhat UI va thong bao
                // Phat am thanh nap tien thanh cong
                soundManager.playTopupSound();

                updateBalanceDisplay();
                Toast.makeText(this, "Da nap thanh cong: " + amountToAdd + "$", Toast.LENGTH_SHORT).show();

                // 5. Dong Dialog sau khi nap thanh cong
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
        soundManager.playLobbyMusic();
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
