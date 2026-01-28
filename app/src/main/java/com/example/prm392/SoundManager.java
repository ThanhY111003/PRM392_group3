package com.example.prm392;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

/**
 * SoundManager - Quản lý âm thanh cho game Duck Race
 * Sử dụng MediaPlayer cho nhạc nền và SoundPool cho hiệu ứng ngắn
 * 
 * HƯỚNG DẪN SỬ DỤNG:
 * 1. Thêm các file âm thanh vào thư mục res/raw/
 * 2. Gọi SoundManager.getInstance(context) để lấy instance
 * 3. Gọi các hàm phát âm thanh: playClickSound(), playWinSound(), v.v.
 * 
 * DANH SÁCH FILE ÂM THANH CẦN CÓ:
 * - res/raw/click.mp3          -> Tiếng click nút
 * - res/raw/success.mp3        -> Tiếng đăng ký/đăng nhập thành công
 * - res/raw/win.mp3            -> Tiếng pháo hoa khi thắng
 * - res/raw/lose.mp3           -> Tiếng thua cuộc
 * - res/raw/bet.mp3            -> Tiếng đặt cược
 * - res/raw/cheer.mp3          -> Tiếng hò reo khi đua
 * - res/raw/topup.mp3          -> Tiếng "Kaching" khi nạp tiền
 * - res/raw/start.mp3          -> Tiếng bắt đầu đua
 * - res/raw/bgm_lobby.mp3      -> Nhạc nền lobby
 * - res/raw/bgm_race.mp3       -> Nhạc nền khi đua
 * - res/raw/bgm_result.mp3     -> Nhạc nền màn hình kết quả
 */
public class SoundManager {

    private static SoundManager instance;
    private Context context;

    // SoundPool for SFX
    private SoundPool soundPool;
    private boolean soundPoolLoaded = false;

    // Sound IDs (loaded from SoundPool)
    private int soundClick = -1;
    private int soundSuccess = -1;
    private int soundWin = -1;
    private int soundLose = -1;
    private int soundBet = -1;
    private int soundCheer = -1;
    private int soundTopup = -1;
    private int soundStart = -1;

    // MediaPlayer for background music
    private MediaPlayer bgmPlayer;
    private boolean isBgmPlaying = false;
    private int currentBgmResId = 0;

    // Volume settings
    private float sfxVolume = 1.0f;
    private float bgmVolume = 0.6f;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(6)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(6, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                soundPoolLoaded = true;
            }
        });

        loadSounds();
    }

    /**
     * Load sound effects from raw resources
     */
    private void loadSounds() {
        // Load each sound file - SoundPool.load() returns stream ID
        soundClick = loadSound("click");
        soundSuccess = loadSound("success");
        soundWin = loadSound("win");
        soundLose = loadSound("lose");
        soundBet = loadSound("bet");
        soundCheer = loadSound("cheer");
        soundTopup = loadSound("topup");
        soundStart = loadSound("start");
    }

    /**
     * Helper method to load a single sound
     */
    private int loadSound(String soundName) {
        try {
            int resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
            if (resId != 0) {
                return soundPool.load(context, resId, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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
     * Play win sound - Tiếng pháo hoa khi thắng
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
     * Play cheer sound - Tiếng hò reo khi đang đua
     */
    public void playCheerSound() {
        playSfx(soundCheer);
    }

    /**
     * Play topup/coin sound - Tiếng "Kaching" khi nhận tiền
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
     * Generic method to play a sound effect
     */
    private void playSfx(int soundId) {
        if (soundPool == null || soundId <= 0) {
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
        // Neu dang phat cung bai nhac thi khong lam gi
        if (currentBgmResId == musicResId && bgmPlayer != null && bgmPlayer.isPlaying()) {
            return;
        }
        
        stopBackgroundMusic();

        try {
            bgmPlayer = MediaPlayer.create(context, musicResId);
            if (bgmPlayer != null) {
                bgmPlayer.setVolume(bgmVolume, bgmVolume);
                bgmPlayer.setLooping(false); // Khong lap lai vi file ngan
                bgmPlayer.start();
                isBgmPlaying = true;
                currentBgmResId = musicResId;
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
            // No music file
        }
    }

    /**
     * Play race music - Tieng dam dong ho reo khi dua
     * Su dung looping vi file da du dai
     */
    public void playRaceMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("bgm_race", "raw", context.getPackageName());
            if (musicResId != 0) {
                // Neu dang phat cung bai nhac thi khong lam gi
                if (currentBgmResId == musicResId && bgmPlayer != null && bgmPlayer.isPlaying()) {
                    return;
                }
                
                stopBackgroundMusic();
                
                bgmPlayer = MediaPlayer.create(context, musicResId);
                if (bgmPlayer != null) {
                    bgmPlayer.setVolume(bgmVolume, bgmVolume);
                    bgmPlayer.setLooping(true); // Loop cho nhac dua
                    bgmPlayer.start();
                    isBgmPlaying = true;
                    currentBgmResId = musicResId;
                }
            }
        } catch (Exception e) {
            // No music file
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
            // No music file
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
        currentBgmResId = 0;
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
            try {
                bgmPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    // ==================== LIFECYCLE METHODS ====================

    /**
     * Release all resources
     */
    public void release() {
        stopBackgroundMusic();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        soundPoolLoaded = false;
        instance = null;
    }

    /**
     * Pause when app goes to background
     */
    public void onPause() {
        pauseBackgroundMusic();
    }

    /**
     * Resume when app comes to foreground
     */
    public void onResume() {
        resumeBackgroundMusic();
    }
}
