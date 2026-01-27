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

    // Khai báo các view
    private TextView tvPlayerName, tvBalance;
    private Button btnTopup, btnStartBet;
    private SharedPreferences sharedPreferences;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ View từ XML
        initViews();

        // Xử lý Padding cho hệ thống (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

        // Lấy username từ Intent gửi sang từ màn hình Login
        currentUsername = getIntent().getStringExtra("username");

        // Nếu không lấy được username (ví dụ mở trực tiếp), đặt mặc định là Guest
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }

        // Hiển thị thông tin lên màn hình
        tvPlayerName.setText(currentUsername);
        updateBalanceDisplay();

        // Xử lý nút Nạp tiền
        btnTopup.setOnClickListener(v -> showTopupDialog());

        // Xử lý nút Bắt đầu cược (Luồng 3 sẽ dùng)
        btnStartBet.setOnClickListener(v -> {
             Toast.makeText(this, "Chuyển sang màn hình đua xe...", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(MainActivity.this, RaceActivity.class);
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
        // Đọc số dư từ SharedPreferences, mặc định là 0 nếu chưa có
        long currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);
        tvBalance.setText("Số dư: " + currentBalance + "$");
    }

    private void showTopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_topup, null);
        builder.setView(dialogView);

        // Tạo Dialog từ builder
        AlertDialog dialog = builder.create();

        // Ánh xạ các view trong dialog_topup.xml
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmTopup);

        // Xử lý nút Xác nhận
        btnConfirm.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long amountToAdd = Long.parseLong(amountStr);

                // 1. Lấy số dư hiện tại từ SharedPreferences
                long currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);

                // 2. Cộng dồn tiền
                long newBalance = currentBalance + amountToAdd;

                // 3. Lưu lại vào bộ nhớ
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(currentUsername + "_balance", newBalance);
                editor.apply();

                // 4. Cập nhật UI và thông báo
                updateBalanceDisplay();
                Toast.makeText(this, "Đã nạp thành công: " + amountToAdd + "$", Toast.LENGTH_SHORT).show();

                // 5. Đóng Dialog sau khi nạp thành công
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}