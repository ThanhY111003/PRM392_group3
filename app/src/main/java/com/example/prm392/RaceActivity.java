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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class RaceActivity extends AppCompatActivity {

    private static final int SO_LUONG_LAN = 5;

    // --- Cau hinh ---
    private static final int MAX_PROGRESS = 10000;
    private static final int DELAY_MS = 30;
    private static final int START_OFFSET_DP = 40;
    private static final int CHEER_INTERVAL_MS = 2000;

    private SeekBar[] sbRacers = new SeekBar[SO_LUONG_LAN];
    private ImageView[] ivRacers = new ImageView[SO_LUONG_LAN];
    private Button btnStart, btnReset, btnExit;
    private TextView tvTitle, tvBalanceInRace;

    // View dat cuoc
    private CheckBox[] cbBets = new CheckBox[SO_LUONG_LAN];
    private EditText[] edtBets = new EditText[SO_LUONG_LAN];

    // Du lieu nguoi choi & cuoc
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private long currentBalance;
    private long balanceBeforeRace;
    private long[] betAmounts = new long[SO_LUONG_LAN];
    private int startOffsetPx;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private boolean isRacing = false;
    private Runnable raceRunnable;
    private Runnable cheerRunnable;
    
    // Track all finishers for result screen
    private boolean[] hasFinished = new boolean[SO_LUONG_LAN];
    private int[] finishOrder = new int[SO_LUONG_LAN];
    private int finishCount = 0;

    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_race);

        soundManager = SoundManager.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.race_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }
        currentBalance = sharedPreferences.getLong(currentUsername + "_balance", 0);
        startOffsetPx = dpToPx(START_OFFSET_DP);

        initViews();

        btnStart.setOnClickListener(v -> {
            soundManager.playClickSound();
            batDauDua();
        });
        
        btnReset.setOnClickListener(v -> {
            soundManager.playClickSound();
            resetGame();
        });
        
        btnExit.setOnClickListener(v -> {
            soundManager.playClickSound();
            // Dung cuoc dua neu dang chay
            if (isRacing) {
                isRacing = false;
                handler.removeCallbacks(raceRunnable);
                stopCheerLoop();
            }
            // Quay ve MainActivity
            Intent intent = new Intent(RaceActivity.this, MainActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvBalanceInRace = findViewById(R.id.tvBalanceInRace);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);
        btnExit = findViewById(R.id.btnExit);

        if (tvBalanceInRace != null) {
            tvBalanceInRace.setText("So du: " + currentBalance + "$");
        }

        int[] sbIds = {R.id.sb1, R.id.sb2, R.id.sb3, R.id.sb4, R.id.sb5};
        int[] ivIds = {R.id.iv1, R.id.iv2, R.id.iv3, R.id.iv4, R.id.iv5};
        int[] cbIds = {R.id.cbBet1, R.id.cbBet2, R.id.cbBet3, R.id.cbBet4, R.id.cbBet5};
        int[] edtIds = {R.id.edtBet1, R.id.edtBet2, R.id.edtBet3, R.id.edtBet4, R.id.edtBet5};

        int[] racerImageIds = {
                R.drawable.vit6,
                R.drawable.vit6,
                R.drawable.vit6,
                R.drawable.vit6,
                R.drawable.vit6
        };

        for (int i = 0; i < SO_LUONG_LAN; i++) {
            sbRacers[i] = findViewById(sbIds[i]);
            ivRacers[i] = findViewById(ivIds[i]);
            cbBets[i] = findViewById(cbIds[i]);
            edtBets[i] = findViewById(edtIds[i]);

            sbRacers[i].setEnabled(false);
            sbRacers[i].setMax(MAX_PROGRESS);

            try {
                ivRacers[i].setImageResource(racerImageIds[i]);
            } catch (Exception e) {
                ivRacers[i].setImageResource(R.mipmap.ic_launcher);
            }

            ivRacers[i].setTranslationX(startOffsetPx);
        }
    }

    private void batDauDua() {
        long tongTienCuoc = 0;
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            betAmounts[i] = 0;
        }

        boolean coDatCuoc = false;
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            if (cbBets[i] != null && cbBets[i].isChecked()) {
                coDatCuoc = true;

                String amountStr = edtBets[i] != null ? edtBets[i].getText().toString().trim() : "";
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Vui long nhap so tien cho Vit " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                long amount;
                try {
                    amount = Long.parseLong(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "So tien khong hop le cho Vit " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (amount <= 0) {
                    Toast.makeText(this, "So tien cuoc phai > 0 cho Vit " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                betAmounts[i] = amount;
                tongTienCuoc += amount;
            }
        }

        if (!coDatCuoc) {
            Toast.makeText(this, "Ban chua chon con nao de dat cuoc!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tongTienCuoc > currentBalance) {
            Toast.makeText(this, "Tong tien cuoc vuot qua so du!", Toast.LENGTH_SHORT).show();
            return;
        }

        balanceBeforeRace = currentBalance;
        
        balanceBeforeRace = currentBalance;
        
        soundManager.playBetSound();

        currentBalance -= tongTienCuoc;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(currentUsername + "_balance", currentBalance);
        editor.apply();

        if (tvBalanceInRace != null) {
            tvBalanceInRace.setText("So du: " + currentBalance + "$");
        }

        isRacing = true;
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            hasFinished[i] = false;
            finishOrder[i] = -1;
        }
        finishCount = 0;
        btnStart.setEnabled(false);
        btnReset.setEnabled(true);

        soundManager.playStartSound();
        soundManager.playRaceMusic();

        tvTitle.setText("CUOC DU DA BAT DAU!");

        raceRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRacing) return;

                for (int i = 0; i < SO_LUONG_LAN; i++) {
                    if (hasFinished[i]) continue;
                    
                    int speed = random.nextInt(71) + 10;
                    int currentProgress = sbRacers[i].getProgress();
                    int newProgress = currentProgress + speed;

                    if (newProgress >= MAX_PROGRESS) {
                        newProgress = MAX_PROGRESS;
                        sbRacers[i].setProgress(newProgress);
                        updatePosition(i, newProgress);
                        
                        hasFinished[i] = true;
                        finishOrder[finishCount++] = i;
                        
                        if (finishCount >= SO_LUONG_LAN) {
                            stopCheerLoop();
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
    
    private void startCheerLoop() {
        cheerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRacing) {
                    soundManager.playCheerSound();
                    handler.postDelayed(this, CHEER_INTERVAL_MS);
                }
            }
        };
        handler.postDelayed(cheerRunnable, CHEER_INTERVAL_MS);
    }
    
    private void stopCheerLoop() {
        if (cheerRunnable != null) {
            handler.removeCallbacks(cheerRunnable);
        }
    }

    private void updatePosition(int index, int progress) {
        SeekBar sb = sbRacers[index];
        ImageView iv = ivRacers[index];

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
        
        soundManager.stopBackgroundMusic();

        int winnerIndex = finishOrder[0];
        String winnerName = "Vit so " + (winnerIndex + 1);
        tvTitle.setText(winnerName + " CHIEN THANG!");
        
        handler.postDelayed(() -> launchResultActivity(), 2000);
    }
    
    private void launchResultActivity() {
        Intent intent = new Intent(RaceActivity.this, ResultActivity.class);
        intent.putExtra("rankings", finishOrder);
        intent.putExtra("betAmounts", betAmounts);
        intent.putExtra("previousBalance", currentBalance);
        intent.putExtra("username", currentUsername);
        startActivity(intent);
        finish();
    }

    private void resetGame() {
        isRacing = false;
        handler.removeCallbacks(raceRunnable);
        stopCheerLoop();
        
        tvTitle.setText("DUCK RACE");
        btnStart.setEnabled(true);
        btnReset.setEnabled(false);
        
        for (int i = 0; i < SO_LUONG_LAN; i++) {
            sbRacers[i].setProgress(0);
            ivRacers[i].setTranslationX(startOffsetPx);
            betAmounts[i] = 0;
            if (cbBets[i] != null) {
                cbBets[i].setChecked(false);
            }
            if (edtBets[i] != null) {
                edtBets[i].setText("");
            }
        }
        
        if (tvBalanceInRace != null) {
            tvBalanceInRace.setText("So du: " + currentBalance + "$");
        }
        
        Toast.makeText(this, "Da lam moi duong dua!", Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
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
        if (isRacing) {
            stopCheerLoop();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.stopBackgroundMusic();
        }
    }
}
