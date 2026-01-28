# HƯỚNG DẪN THÊM ÂM THANH CHO GAME DUCK RACE

## Bước 1: Tải file âm thanh
Tải các file âm thanh miễn phí từ các trang sau:
- https://mixkit.co/free-sound-effects/game/
- https://www.zapsplat.com/sound-effect-categories/game-sounds/
- https://freesound.org/

## Bước 2: Các file cần có
Đặt các file vào thư mục: `app/src/main/res/raw/`

### Hiệu ứng âm thanh (SFX):
| Tên file          | Mô tả                               |
|-------------------|-------------------------------------|
| click.mp3         | Tiếng click nút                     |
| success.mp3       | Tiếng thành công (đăng nhập/ký)     |
| win.mp3           | Tiếng pháo hoa khi thắng            |
| lose.mp3          | Tiếng thua cuộc                     |
| bet.mp3           | Tiếng đặt cược                      |
| cheer.mp3         | Tiếng hò reo khi đang đua           |
| topup.mp3         | Tiếng "Kaching" khi nạp tiền        |
| start.mp3         | Tiếng bắt đầu đua                   |

### Nhạc nền (BGM):
| Tên file          | Màn hình                            |
|-------------------|-------------------------------------|
| bgm_lobby.mp3     | Màn hình chính (MainActivity)       |
| bgm_race.mp3      | Màn hình đua (RaceActivity)         |
| bgm_result.mp3    | Màn hình kết quả (ResultActivity)   |

## Bước 3: Cách sử dụng SoundManager

### Khởi tạo:
```java
SoundManager soundManager = SoundManager.getInstance(this);
```

### Phát hiệu ứng:
```java
soundManager.playClickSound();    // Click nút
soundManager.playWinSound();      // Thắng (pháo hoa)
soundManager.playLoseSound();     // Thua
soundManager.playCheerSound();    // Hò reo khi đua
soundManager.playTopupSound();    // Kaching khi nạp tiền
soundManager.playBetSound();      // Đặt cược
soundManager.playStartSound();    // Bắt đầu đua
soundManager.playSuccessSound();  // Thành công
```

### Phát nhạc nền:
```java
soundManager.playLobbyMusic();    // Nhạc lobby
soundManager.playRaceMusic();     // Nhạc đua
soundManager.playResultMusic();   // Nhạc kết quả
soundManager.stopBackgroundMusic(); // Dừng nhạc
```

### Lifecycle (thêm vào các Activity):
```java
@Override
protected void onPause() {
    super.onPause();
    soundManager.onPause();
}

@Override
protected void onResume() {
    super.onResume();
    soundManager.onResume();
}
```

## Lưu ý quan trọng:
1. File phải có tên chính xác như trên (viết thường, không dấu)
2. Định dạng: .mp3 hoặc .ogg
3. Sau khi thêm file, chạy lại Build > Clean Project rồi Build lại
