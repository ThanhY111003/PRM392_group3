package com.example.prm392;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
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

    private SeekBar[] sbRacers = new SeekBar[SO_LUONG_LAN];
    private ImageView[] ivRacers = new ImageView[SO_LUONG_LAN];
    private Button btnStart, btnReset;
    private TextView tvTitle;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private boolean isRacing = false;
    private Runnable raceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        initViews();

        btnStart.setOnClickListener(v -> batDauDua());
        // Nút Reset giờ có thể bấm bất cứ lúc nào
        btnReset.setOnClickListener(v -> resetGame());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);

        int[] sbIds = {R.id.sb1, R.id.sb2, R.id.sb3, R.id.sb4, R.id.sb5};
        int[] ivIds = {R.id.iv1, R.id.iv2, R.id.iv3, R.id.iv4, R.id.iv5};

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
        }
    }

    private void batDauDua() {
        // Reset trạng thái trước khi đua (đề phòng trường hợp đua tiếp)
        isRacing = true;
        btnStart.setEnabled(false);

        // --- THAY ĐỔI 2: Luôn cho phép bấm Reset ---
        btnReset.setEnabled(true);

        tvTitle.setText("🔥 CUỘC ĐUA BẮT ĐẦU 🔥");

        raceRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRacing) return;

                for (int i = 0; i < SO_LUONG_LAN; i++) {
                    int speed = random.nextInt(51) + 10;
                    int currentProgress = sbRacers[i].getProgress();
                    int newProgress = currentProgress + speed;

                    if (newProgress >= MAX_PROGRESS) {
                        newProgress = MAX_PROGRESS;
                        updatePosition(i, newProgress);
                        xuLyNguoiThang(i);
                        return;
                    }

                    sbRacers[i].setProgress(newProgress);
                    updatePosition(i, newProgress);
                }

                handler.postDelayed(this, DELAY_MS);
            }
        };
        handler.post(raceRunnable);
    }

    private void updatePosition(int index, int progress) {
        SeekBar sb = sbRacers[index];
        ImageView iv = ivRacers[index];
        int trackWidth = sb.getWidth() - 60;
        int iconWidth = iv.getWidth();
        float translationX = (float) progress / MAX_PROGRESS * (trackWidth - iconWidth);
        iv.setTranslationX(translationX);
    }

    private void xuLyNguoiThang(int index) {
        isRacing = false;
        handler.removeCallbacks(raceRunnable);

        String winnerName = "Vịt số " + (index + 1);
        tvTitle.setText("👑 " + winnerName + " CHIẾN THẮNG! 👑");
        Toast.makeText(RaceActivity.this, winnerName + " về nhất!", Toast.LENGTH_LONG).show();

        // Khi thắng xong, nút Start hiện lại
        btnStart.setEnabled(true);
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
            ivRacers[i].setTranslationX(0);
        }

        Toast.makeText(this, "Đã làm mới đường đua!", Toast.LENGTH_SHORT).show();
    }
}
