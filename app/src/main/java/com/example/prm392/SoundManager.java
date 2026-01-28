package com.example.prm392;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

/**
 * SoundManager - Quản lý âm thanh cho game Duck Race
 * Sử dụng MediaPlayer cho nhạc nền và SoundPool cho hiệu ứng ngắn
 */
public class SoundManager {

    private static SoundManager instance;
    private Context context;

    // SoundPool for SFX
    private SoundPool soundPool;
    private boolean soundPoolLoaded = false;

    // Sound IDs
    private int soundClick;
    private int soundSuccess;
    private int soundWin;
    private int soundLose;
    private int soundBet;
    private int soundCheer;
    private int soundTopup;
    private int soundStart;

    // MediaPlayer for background music
    private MediaPlayer bgmPlayer;
    private boolean isBgmPlaying = false;

    // Volume settings
    private float sfxVolume = 0.8f;
    private float bgmVolume = 0.5f;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initSoundPool();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * Initialize SoundPool for sound effects
     */
    private void initSoundPool() {
        // Build SoundPool based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundPoolLoaded = true;
        });

        // Load sound effects (using placeholder resources)
        // Note: Replace R.raw.sound_name with actual audio files
        loadSounds();
    }

    /**
     * Load sound effects from raw resources
     * Add your audio files to res/raw/ folder
     */
    private void loadSounds() {
        try {
            // Try to load sounds - these will fail if files don't exist
            // but the app will still work
            soundClick = context.getResources().getIdentifier("click", "raw", context.getPackageName());
            if (soundClick == 0) soundClick = -1;

            soundSuccess = context.getResources().getIdentifier("success", "raw", context.getPackageName());
            if (soundSuccess == 0) soundSuccess = -1;

            soundWin = context.getResources().getIdentifier("win", "raw", context.getPackageName());
            if (soundWin == 0) soundWin = -1;

            soundLose = context.getResources().getIdentifier("lose", "raw", context.getPackageName());
            if (soundLose == 0) soundLose = -1;

            soundBet = context.getResources().getIdentifier("bet", "raw", context.getPackageName());
            if (soundBet == 0) soundBet = -1;

            soundCheer = context.getResources().getIdentifier("cheer", "raw", context.getPackageName());
            if (soundCheer == 0) soundCheer = -1;

            soundTopup = context.getResources().getIdentifier("topup", "raw", context.getPackageName());
            if (soundTopup == 0) soundTopup = -1;

            soundStart = context.getResources().getIdentifier("start", "raw", context.getPackageName());
            if (soundStart == 0) soundStart = -1;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== PLAY SOUND METHODS ====================

    /**
     * Play click sound effect
     */
    public void playClickSound() {
        playSfx(soundClick);
    }

    /**
     * Play success sound (login, register successful)
     */
    public void playSuccessSound() {
        playSfx(soundSuccess);
    }

    /**
     * Play win sound (won the race)
     */
    public void playWinSound() {
        playSfx(soundWin);
    }

    /**
     * Play lose sound (lost the race)
     */
    public void playLoseSound() {
        playSfx(soundLose);
    }

    /**
     * Play bet placed sound
     */
    public void playBetSound() {
        playSfx(soundBet);
    }

    /**
     * Play cheer sound (during race)
     */
    public void playCheerSound() {
        playSfx(soundCheer);
    }

    /**
     * Play topup/coin sound
     */
    public void playTopupSound() {
        playSfx(soundTopup);
    }

    /**
     * Play race start sound
     */
    public void playStartSound() {
        playSfx(soundStart);
    }

    /**
     * Generic method to play a sound
     */
    private void playSfx(int soundId) {
        if (soundPool == null || !soundPoolLoaded || soundId == -1) {
            return;
        }

        try {
            soundPool.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== BACKGROUND MUSIC METHODS ====================

    /**
     * Play background music for a specific screen
     * @param musicResId Resource ID of the music file in raw folder
     */
    public void playBackgroundMusic(int musicResId) {
        stopBackgroundMusic();

        try {
            bgmPlayer = MediaPlayer.create(context, musicResId);
            if (bgmPlayer != null) {
                bgmPlayer.setVolume(bgmVolume, bgmVolume);
                bgmPlayer.setLooping(true);
                bgmPlayer.start();
                isBgmPlaying = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Play default lobby music
     */
    public void playLobbyMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("bgm_lobby", "raw", context.getPackageName());
            if (musicResId != 0) {
                playBackgroundMusic(musicResId);
            }
        } catch (Exception e) {
            // No music file, that's okay
        }
    }

    /**
     * Play race music
     */
    public void playRaceMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("bgm_race", "raw", context.getPackageName());
            if (musicResId != 0) {
                playBackgroundMusic(musicResId);
            }
        } catch (Exception e) {
            // No music file, that's okay
        }
    }

    /**
     * Play result music
     */
    public void playResultMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("bgm_result", "raw", context.getPackageName());
            if (musicResId != 0) {
                playBackgroundMusic(musicResId);
            }
        } catch (Exception e) {
            // No music file, that's okay
        }
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (bgmPlayer != null) {
            try {
                if (bgmPlayer.isPlaying()) {
                    bgmPlayer.stop();
                }
                bgmPlayer.release();
                bgmPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isBgmPlaying = false;
    }

    /**
     * Pause background music
     */
    public void pauseBackgroundMusic() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }

    /**
     * Resume background music
     */
    public void resumeBackgroundMusic() {
        if (bgmPlayer != null && !bgmPlayer.isPlaying()) {
            bgmPlayer.start();
        }
    }

    /**
     * Check if background music is playing
     */
    public boolean isBgmPlaying() {
        return isBgmPlaying;
    }

    // ==================== VOLUME CONTROL ====================

    /**
     * Set SFX volume (0.0 to 1.0)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }

    /**
     * Set BGM volume (0.0 to 1.0)
     */
    public void setBgmVolume(float volume) {
        this.bgmVolume = Math.max(0f, Math.min(1f, volume));
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(bgmVolume, bgmVolume);
        }
    }

    // ==================== CLEANUP ====================

    /**
     * Release all resources
     * Call this in Application.onTerminate() or when app closes
     */
    public void release() {
        stopBackgroundMusic();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        soundPoolLoaded = false;
    }

    /**
     * Pause all sounds when app goes to background
     */
    public void onPause() {
        pauseBackgroundMusic();
    }

    /**
     * Resume sounds when app comes to foreground
     */
    public void onResume() {
        resumeBackgroundMusic();
    }
}

