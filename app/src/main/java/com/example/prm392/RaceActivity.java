package com.example.prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class RaceActivity extends AppCompatActivity {

    private static final int SO_LUONG_LAN = 5;

    // --- CẤU HÌNH ---
    private static final int MAX_PROGRESS = 10000;
    private static final int DELAY_MS = 30;
    private static final int START_OFFSET_DP = 40; // chừa khoảng nhỏ ở đầu đường đua

    private SeekBar[] sbRacers = new SeekBar[SO_LUONG_LAN];
    private ImageView[] ivRacers = new ImageView[SO_LUONG_LAN];
    private Button btnStart, btnReset;
    private TextView tvTitle, tvBalanceInRace;

    // View đặt cược
    private CheckBox[] cbBets = new CheckBox[SO_LUONG_LAN];
    private EditText[] edtBets = new EditText[SO_LUONG_LAN];

    // Dữ liệu người chơi & cược
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private long currentBalance;
    private long balanceBeforeRace; // Balance before bets were deducted
    private long[] betAmounts = new long[SO_LUONG_LAN];
    private int startOffsetPx;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private boolean isRacing = false;
    private Runnable raceRunnable;
    
    // Track all finishers for result screen
    private boolean[] hasFinished = new boolean[SO_LUONG_LAN];
    private int[] finishOrder = new int[SO_LUONG_LAN];
    private int finishCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        // Khởi tạo SharedPreferences và lấy thông tin người chơi / số dư
        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }
        currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);
        startOffsetPx = dpToPx(START_OFFSET_DP);

        initViews();

        btnStart.setOnClickListener(v -> batDauDua());
        // Nút Reset giờ có thể bấm bất cứ lúc nào
        btnReset.setOnClickListener(v -> resetGame());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvBalanceInRace = findViewById(R.id.tvBalanceInRace);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);

        // Hiển thị số dư hiện tại trong màn đua
        if (tvBalanceInRace != null) {
            tvBalanceInRace.setText("Số dư: " + currentBalance + "$");
        }

        int[] sbIds = {R.id.sb1, R.id.sb2, R.id.sb3, R.id.sb4, R.id.sb5};
        int[] ivIds = {R.id.iv1, R.id.iv2, R.id.iv3, R.id.iv4, R.id.iv5};
        int[] cbIds = {R.id.cbBet1, R.id.cbBet2, R.id.cbBet3, R.id.cbBet4, R.id.cbBet5};
        int[] edtIds = {R.id.edtBet1, R.id.edtBet2, R.id.edtBet3, R.id.edtBet4, R.id.edtBet5};

        // --- THAY ĐỔI 1: Mảng chứa ID ảnh trong Drawable ---
        // Bạn nhớ đổi tên R.drawable.xxx thành tên file ảnh thực tế của bạn
        int[] racerImageIds = {
                R.drawable.vit1, // Ảnh cho làn 1
                R.drawable.vit2, // Ảnh cho làn 2
                R.drawable.vit3, // Ảnh cho làn 3
                R.drawable.vit4, // Ảnh cho làn 4
                R.drawable.vit5  // Ảnh cho làn 5
        };

        for (int i = 0; i < SO_LUONG_LAN; i++) {
            sbRacers[i] = findViewById(sbIds[i]);
            ivRacers[i] = findViewById(ivIds[i]);
            cbBets[i] = findViewById(cbIds[i]);
            edtBets[i] = findViewById(edtIds[i]);

            sbRacers[i].setEnabled(false);
            sbRacers[i].setMax(MAX_PROGRESS);

            // Set ảnh trực tiếp từ resource (Không cần Glide nữa)
            // Nếu bạn chưa có đủ 5 ảnh, code sẽ báo đỏ.
            // Tạm thời có thể dùng R.mipmap.ic_launcher để test nếu thiếu ảnh.
            try {
                ivRacers[i].setImageResource(racerImageIds[i]);
            } catch (Exception e) {
                // Fallback nếu quên copy ảnh
                ivRacers[i].setImageResource(R.mipmap.ic_launcher);
            }

            // Đặt vị trí xuất phát sau vùng đặt cược
            ivRacers[i].setTranslationX(startOffsetPx);
        }
    }

    private void batDauDua() {
        // --- XỬ LÝ ĐẶT CƯỢC TRƯỚC KHI ĐUA ---
        long tongTienCuoc = 0;
        // reset mảng cược cũ
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            betAmounts[i] = 0;
        }

        boolean coDatCuoc = false;
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            if (cbBets[i] != null && cbBets[i].isChecked()) {
                coDatCuoc = true;

                String amountStr = edtBets[i] != null ? edtBets[i].getText().toString().trim() : "";
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số tiền cho " + "Vịt " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                long amount;
                try {
                    amount = Long.parseLong(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số tiền không hợp lệ cho Vịt " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (amount <= 0) {
                    Toast.makeText(this, "Số tiền cược phải > 0 cho Vịt " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                betAmounts[i] = amount;
                tongTienCuoc += amount;
            }
        }

        if (!coDatCuoc) {
            Toast.makeText(this, "Bạn chưa chọn con nào để đặt cược!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tongTienCuoc > currentBalance) {
            Toast.makeText(this, "Tổng tiền cược vượt quá số dư!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save balance before race for result screen
        balanceBeforeRace = currentBalance;
        
        // Trừ tiền cược ngay khi bắt đầu đua (tiền này sẽ không được hoàn lại khi Reset)
        currentBalance -= tongTienCuoc;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(currentUsername + "_balance", currentBalance);
        editor.apply();

        if (tvBalanceInRace != null) {
            tvBalanceInRace.setText("Số dư: " + currentBalance + "$");
        }

        // Reset trạng thái trước khi đua (đề phòng trường hợp đua tiếp)
        isRacing = true;
        // Reset finish tracking
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            hasFinished[i] = false;
            finishOrder[i] = -1;
        }
        finishCount = 0;
        btnStart.setEnabled(false);

        // --- THAY ĐỔI 2: Luôn cho phép bấm Reset ---
        btnReset.setEnabled(true);

        tvTitle.setText("🔥 CUỘC ĐUA BẮT ĐẦU 🔥");

        raceRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRacing) return;

                for (int i = 0; i < SO_LUONG_LAN; i++) {
                    // Skip racers that have already finished
                    if (hasFinished[i]) continue;
                    
                    int speed = random.nextInt(51) + 10;
                    int currentProgress = sbRacers[i].getProgress();
                    int newProgress = currentProgress + speed;

                    if (newProgress >= MAX_PROGRESS) {
                        newProgress = MAX_PROGRESS;
                        sbRacers[i].setProgress(newProgress);
                        updatePosition(i, newProgress);
                        
                        // Mark as finished and record position
                        hasFinished[i] = true;
                        finishOrder[finishCount++] = i;
                        
                        // Check if all racers have finished
                        if (finishCount >= SO_LUONG_LAN) {
                            xuLyKetThucDua();
                            return;
                        }
                    } else {
                        sbRacers[i].setProgress(newProgress);
                        updatePosition(i, newProgress);
                    }
                }

                handler.postDelayed(this, DELAY_MS);
            }
        };
        handler.post(raceRunnable);
    }

    private void updatePosition(int index, int progress) {
        SeekBar sb = sbRacers[index];
        ImageView iv = ivRacers[index];

        // Để xe đi được xa hơn, chỉ chừa 20px padding cuối đường đua
        int paddingEndPx = dpToPx(20);
        int trackWidth = sb.getWidth() - paddingEndPx - startOffsetPx;
        int iconWidth = iv.getWidth();

        if (trackWidth <= iconWidth) {
            iv.setTranslationX(startOffsetPx);
            return;
        }

        float translationX = startOffsetPx + (float) progress / MAX_PROGRESS * (trackWidth - iconWidth);
        iv.setTranslationX(translationX);
    }

    private void xuLyKetThucDua() {
        isRacing = false;
        handler.removeCallbacks(raceRunnable);

        // Show winner briefly
        int winnerIndex = finishOrder[0];
        String winnerName = "Vịt số " + (winnerIndex + 1);
        tvTitle.setText("👑 " + winnerName + " CHIẾN THẮNG! 👑");
        
        // Launch ResultActivity with all data
        // Use a short delay to let users see the winner before transitioning
        handler.postDelayed(() -> launchResultActivity(), 1500);
    }
    
    private void launchResultActivity() {
        Intent intent = new Intent(RaceActivity.this, ResultActivity.class);
        intent.putExtra("rankings", finishOrder);
        intent.putExtra("betAmounts", betAmounts);
        intent.putExtra("previousBalance", currentBalance); // Balance after bets deducted
        intent.putExtra("username", currentUsername);
        startActivity(intent);
        finish(); // Close RaceActivity
    }

    // --- THAY ĐỔI 3: Logic Reset mạnh mẽ hơn ---
    private void resetGame() {
        // 1. Dừng ngay lập tức mọi hoạt động đua
        isRacing = false;
        handler.removeCallbacks(raceRunnable);

        // 2. Reset giao diện
        tvTitle.setText("🏆 DUCK RACE 🏆");
        btnStart.setEnabled(true);
        // btnReset.setEnabled(false); // Có thể tắt dòng này nếu muốn nút Reset luôn sáng

        // 3. Đưa tất cả về vạch xuất phát
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            sbRacers[i].setProgress(0);
            ivRacers[i].setTranslationX(startOffsetPx);

            // Xoá thông tin cược
            betAmounts[i] = 0;
            if (cbBets[i] != null) {
                cbBets[i].setChecked(false);
            }
            if (edtBets[i] != null) {
                edtBets[i].setText("");
            }
        }

        // Hiển thị lại số dư gốc
        if (tvBalanceInRace != null) {
            tvBalanceInRace.setText("Số dư: " + currentBalance + "$");
        }

        Toast.makeText(this, "Đã làm mới đường đua!", Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
