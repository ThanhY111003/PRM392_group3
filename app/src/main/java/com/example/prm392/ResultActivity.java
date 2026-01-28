package com.example.prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivFirstPlace, ivSecondPlace, ivThirdPlace;
    private TextView tvFirstPlace, tvSecondPlace, tvThirdPlace;
    private LinearLayout layoutBettingStats;
    private TextView tvPreviousBalance, tvTotalBet, tvTotalWinnings, tvNewBalance;
    private TextView tvResultMessage;
    private Button btnRaceAgain, btnBackToLobby;

    // Data from Intent
    private int[] rankings; // [1st, 2nd, 3rd, 4th, 5th] - indices of racers
    private long[] betAmounts; // Bet amounts for each racer
    private long previousBalance; // Balance before race started
    private String currentUsername;

    // Calculated data
    private long totalBet;
    private long totalWinnings;
    private long newBalance;

    // Duck image resources
    private int[] racerImageIds = {
            R.drawable.vit1,
            R.drawable.vit2,
            R.drawable.vit3,
            R.drawable.vit4,
            R.drawable.vit5
    };

    private SharedPreferences sharedPreferences;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Khoi tao SoundManager
        soundManager = SoundManager.getInstance(this);

        initViews();
        receiveIntentData();
        calculateResults();
        displayPodium();
        displayStatistics();
        displayBalanceSummary();
        showMessage();
        setupButtons();
        
        // Phat am thanh thang/thua
        if (totalWinnings > 0) {
            soundManager.playWinSound();
        } else {
            soundManager.playLoseSound();
        }
        
        // Tat nhac result vi file ngan
        // soundManager.playResultMusic();
    }

    private void initViews() {
        // Podium
        ivFirstPlace = findViewById(R.id.ivFirstPlace);
        ivSecondPlace = findViewById(R.id.ivSecondPlace);
        ivThirdPlace = findViewById(R.id.ivThirdPlace);
        tvFirstPlace = findViewById(R.id.tvFirstPlace);
        tvSecondPlace = findViewById(R.id.tvSecondPlace);
        tvThirdPlace = findViewById(R.id.tvThirdPlace);

        // Statistics
        layoutBettingStats = findViewById(R.id.layoutBettingStats);

        // Balance
        tvPreviousBalance = findViewById(R.id.tvPreviousBalance);
        tvTotalBet = findViewById(R.id.tvTotalBet);
        tvTotalWinnings = findViewById(R.id.tvTotalWinnings);
        tvNewBalance = findViewById(R.id.tvNewBalance);

        // Message
        tvResultMessage = findViewById(R.id.tvResultMessage);

        // Buttons
        btnRaceAgain = findViewById(R.id.btnRaceAgain);
        btnBackToLobby = findViewById(R.id.btnBackToLobby);

        // SharedPreferences
        sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        rankings = intent.getIntArrayExtra("rankings");
        betAmounts = intent.getLongArrayExtra("betAmounts");
        previousBalance = intent.getLongExtra("previousBalance", 0);
        currentUsername = intent.getStringExtra("username");

        // Fallback values
        if (rankings == null) rankings = new int[]{0, 1, 2, 3, 4};
        if (betAmounts == null) betAmounts = new long[5];
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }
    }

    private void calculateResults() {
        totalBet = 0;
        totalWinnings = 0;

        // Calculate total bet
        for (long bet : betAmounts) {
            totalBet += bet;
        }

        // Calculate winnings (only for the winner)
        int winnerIndex = rankings[0];
        if (betAmounts[winnerIndex] > 0) {
            // Win = 2x the bet amount (includes original bet back)
            totalWinnings = betAmounts[winnerIndex] * 2;
        }

        //newBalance = currentBalance (after bets) + winnings
        newBalance = previousBalance + totalWinnings;

        // Update balance in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(currentUsername + "_balance", newBalance);
        editor.apply();
    }

    private void displayPodium() {
        // 1st Place
        int firstIndex = rankings[0];
        ivFirstPlace.setImageResource(racerImageIds[firstIndex]);
        tvFirstPlace.setText("Vịt #" + (firstIndex + 1));

        // 2nd Place
        if (rankings.length > 1) {
            int secondIndex = rankings[1];
            ivSecondPlace.setImageResource(racerImageIds[secondIndex]);
            tvSecondPlace.setText("Vịt #" + (secondIndex + 1));
        }

        // 3rd Place
        if (rankings.length > 2) {
            int thirdIndex = rankings[2];
            ivThirdPlace.setImageResource(racerImageIds[thirdIndex]);
            tvThirdPlace.setText("Vịt #" + (thirdIndex + 1));
        }
    }

    private void displayStatistics() {
        layoutBettingStats.removeAllViews();

        // Only show bets that were placed
        for (int i = 0; i < betAmounts.length; i++) {
            if (betAmounts[i] > 0) {
                addBetRow(i, betAmounts[i]);
            }
        }

        // If no bets were placed (shouldn't happen, but handle gracefully)
        if (layoutBettingStats.getChildCount() == 0) {
            TextView noBetsText = new TextView(this);
            noBetsText.setText("No bets were placed");
            noBetsText.setTextSize(14);
            noBetsText.setGravity(Gravity.CENTER);
            noBetsText.setPadding(0, 16, 0, 16);
            layoutBettingStats.addView(noBetsText);
        }
    }

    private void addBetRow(int racerIndex, long betAmount) {
        // Create a row for each bet
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(rowParams);

        // Duck name
        TextView tvDuckName = new TextView(this);
        tvDuckName.setText("Vịt #" + (racerIndex + 1));
        tvDuckName.setTextSize(16);
        tvDuckName.setTextColor(getResources().getColor(android.R.color.black));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.5f
        );
        tvDuckName.setLayoutParams(nameParams);
        row.addView(tvDuckName);

        // Bet amount
        TextView tvBetAmount = new TextView(this);
        tvBetAmount.setText(betAmount + "$");
        tvBetAmount.setTextSize(16);
        tvBetAmount.setTextColor(getResources().getColor(android.R.color.black));
        LinearLayout.LayoutParams betParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        tvBetAmount.setLayoutParams(betParams);
        tvBetAmount.setGravity(Gravity.CENTER);
        row.addView(tvBetAmount);

        // Result (Win/Loss)
        boolean isWinner = racerIndex == rankings[0];
        TextView tvResult = new TextView(this);
        if (isWinner) {
            tvResult.setText("✅ WIN");
            tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvResult.setText("❌ LOSS");
            tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        tvResult.setTextSize(16);
        tvResult.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        tvResult.setLayoutParams(resultParams);
        tvResult.setGravity(Gravity.CENTER);
        row.addView(tvResult);

        // Payout
        TextView tvPayout = new TextView(this);
        if (isWinner) {
            long payout = betAmount * 2;
            tvPayout.setText("+" + payout + "$");
            tvPayout.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvPayout.setText("-" + betAmount + "$");
            tvPayout.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        tvPayout.setTextSize(16);
        tvPayout.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams payoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.5f
        );
        tvPayout.setLayoutParams(payoutParams);
        tvPayout.setGravity(Gravity.END);
        row.addView(tvPayout);

        layoutBettingStats.addView(row);

        // Add divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        layoutBettingStats.addView(divider);
    }

    private void displayBalanceSummary() {
        // Note: previousBalance is the balance AFTER bets were deducted
        // So we need to show the balance before bets for clarity
        long balanceBeforeBets = previousBalance + totalBet;

        tvPreviousBalance.setText(balanceBeforeBets + "$");
        tvTotalBet.setText(totalBet + "$");
        tvTotalWinnings.setText(totalWinnings + "$");
        tvNewBalance.setText(newBalance + "$");
    }

    private void showMessage() {
        if (totalWinnings > 0) {
            // Player won
            long profit = totalWinnings - betAmounts[rankings[0]]; // Net profit
            tvResultMessage.setText("🎉 Congratulations! You won " + profit + "$!");
            tvResultMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            // Player lost
            tvResultMessage.setText("😢 Better luck next time! You lost " + totalBet + "$");
            tvResultMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void setupButtons() {
        // Race Again - Go back to RaceActivity
        btnRaceAgain.setOnClickListener(v -> {
            soundManager.playClickSound();
            Intent intent = new Intent(ResultActivity.this, RaceActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
        });

        // Back to Lobby - Go to MainActivity
        btnBackToLobby.setOnClickListener(v -> {
            soundManager.playClickSound();
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.stopBackgroundMusic();
        }
    }
}
