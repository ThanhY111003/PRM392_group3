package com.example.prm392;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class SoundManager {

    private static SoundManager instance;
    private Context context;

    private SoundPool soundPool;
    private boolean soundPoolLoaded = false;

    private int soundClick = -1;
    private int soundSuccess = -1;
    private int soundWin = -1;
    private int soundLose = -1;
    private int soundBet = -1;
    private int soundCheer = -1;
    private int soundTopup = -1;
    private int soundStart = -1;

    private MediaPlayer bgmPlayer;
    private boolean isBgmPlaying = false;
    private int currentBgmResId = 0;

    private float sfxVolume = 1.0f;
    private float bgmVolume = 1.0f;

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

    private void loadSounds() {
        soundClick = loadSound("click");
        soundSuccess = loadSound("success");
        soundWin = loadSound("win");
        soundLose = loadSound("lose");
        soundBet = loadSound("bet");
        soundCheer = loadSound("cheer");
        soundTopup = loadSound("topup");
        soundStart = loadSound("start");
    }

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

    public void playClickSound() {
        playSfx(soundClick);
    }

    public void playSuccessSound() {
        playSfx(soundSuccess);
    }

    public void playWinSound() {
        playSfx(soundWin);
    }

    public void playLoseSound() {
        playSfx(soundLose);
    }

    public void playBetSound() {
        playSfx(soundBet);
    }

    public void playCheerSound() {
        playSfx(soundCheer);
    }

    public void playTopupSound() {
        playSfx(soundTopup);
    }

    public void playStartSound() {
        playSfx(soundStart);
    }

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

    public void playBackgroundMusic(int musicResId) {
        if (currentBgmResId == musicResId && bgmPlayer != null && bgmPlayer.isPlaying()) {
            return;
        }
        
        stopBackgroundMusic();

        try {
            bgmPlayer = MediaPlayer.create(context, musicResId);
            if (bgmPlayer != null) {
                bgmPlayer.setVolume(bgmVolume, bgmVolume);
                bgmPlayer.setLooping(false);
                bgmPlayer.start();
                isBgmPlaying = true;
                currentBgmResId = musicResId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playLobbyMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("bgm_lobby", "raw", context.getPackageName());
            if (musicResId != 0) {
                playBackgroundMusic(musicResId);
            }
        } catch (Exception e) {
        }
    }

    public void playRaceMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("race_crowd_v2", "raw", context.getPackageName());
            if (musicResId != 0) {
                if (currentBgmResId == musicResId && bgmPlayer != null && bgmPlayer.isPlaying()) {
                    return;
                }
                
                stopBackgroundMusic();
                
                bgmPlayer = MediaPlayer.create(context, musicResId);
                if (bgmPlayer != null) {
                    bgmPlayer.setVolume(bgmVolume, bgmVolume);
                    bgmPlayer.setLooping(true);
                    bgmPlayer.start();
                    isBgmPlaying = true;
                    currentBgmResId = musicResId;
                }
            }
        } catch (Exception e) {
        }
    }

    public void playResultMusic() {
        try {
            int musicResId = context.getResources().getIdentifier("bgm_result", "raw", context.getPackageName());
            if (musicResId != 0) {
                playBackgroundMusic(musicResId);
            }
        } catch (Exception e) {
        }
    }

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

    public void pauseBackgroundMusic() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }

    public void resumeBackgroundMusic() {
        if (bgmPlayer != null && !bgmPlayer.isPlaying()) {
            try {
                bgmPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isBgmPlaying() {
        return isBgmPlaying;
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }

    public void setBgmVolume(float volume) {
        this.bgmVolume = Math.max(0f, Math.min(1f, volume));
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(bgmVolume, bgmVolume);
        }
    }

    public void release() {
        stopBackgroundMusic();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        soundPoolLoaded = false;
        instance = null;
    }

    public void onPause() {
        pauseBackgroundMusic();
    }

    public void onResume() {
        resumeBackgroundMusic();
    }
}
