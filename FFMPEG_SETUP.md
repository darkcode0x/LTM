# FFmpeg Installation Guide for Video Converter

## 📥 Download FFmpeg

### Option 1: Official Build (Recommended)
1. Visit: https://www.gyan.dev/ffmpeg/builds/
2. Download: **ffmpeg-release-essentials.zip** (Latest version)
3. File size: ~80 MB

### Option 2: Full Build
1. Visit: https://www.ffmpeg.org/download.html
2. Download Windows build from Gyan.dev or BtbN

## 🔧 Installation Steps

### Windows Installation

1. **Extract the Archive**
   ```
   Extract ffmpeg-x.x.x-essentials_build.zip to:
   C:\ffmpeg
   ```

2. **Verify Structure**
   ```
   C:\ffmpeg\
   ├── bin\
   │   ├── ffmpeg.exe
   │   ├── ffprobe.exe
   │   └── ffplay.exe
   ├── doc\
   └── presets\
   ```

3. **Add to System PATH**
   
   **Method 1: Using GUI**
   - Right-click **This PC** → **Properties**
   - Click **Advanced system settings**
   - Click **Environment Variables**
   - Under **System variables**, find **Path**
   - Click **Edit** → **New**
   - Add: `C:\ffmpeg\bin`
   - Click **OK** on all dialogs
   
   **Method 2: Using Command Prompt (Admin)**
   ```cmd
   setx /M PATH "%PATH%;C:\ffmpeg\bin"
   ```

4. **Verify Installation**
   Open **NEW** Command Prompt (not the old one):
   ```cmd
   ffmpeg -version
   ffprobe -version
   ```
   
   Expected output:
   ```
   ffmpeg version x.x.x Copyright (c) 2000-2024 the FFmpeg developers
   built with gcc x.x.x (GCC)
   configuration: --enable-gpl --enable-version3...
   ```

## 🔍 Test FFmpeg

### Test Video Conversion
```cmd
cd C:\ffmpeg\bin

# Test basic conversion
ffmpeg -i input.mp4 -c:v libx264 -preset fast output.mp4

# Test with resolution change
ffmpeg -i input.mp4 -vf scale=1280:720 output_720p.mp4
```

### Test Video Info Extraction
```cmd
ffprobe -v quiet -print_format json -show_format -show_streams input.mp4
```

## ⚙️ Configure Video Converter Application

### Update FFmpegWrapper.java (if needed)

If FFmpeg is installed in a custom location, update these lines:

```java
// Default paths (if not in PATH)
private static final String FFMPEG_PATH = "C:\\ffmpeg\\bin\\ffmpeg.exe";
private static final String FFPROBE_PATH = "C:\\ffmpeg\\bin\\ffprobe.exe";
```

Or use environment variable:
```java
private static final String FFMPEG_PATH = 
    System.getenv("FFMPEG_PATH") != null ? 
    System.getenv("FFMPEG_PATH") : "ffmpeg";
```

## 🐧 Linux Installation

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install ffmpeg
```

### Verify
```bash
ffmpeg -version
ffprobe -version
```

## 🍎 macOS Installation

### Using Homebrew
```bash
brew install ffmpeg
```

### Verify
```bash
ffmpeg -version
ffprobe -version
```

## 🧪 Quick Test Script

Create `test_ffmpeg.bat`:
```batch
@echo off
echo Testing FFmpeg Installation...
echo.

echo [1] Checking ffmpeg...
ffmpeg -version
if %errorlevel% neq 0 (
    echo ERROR: ffmpeg not found!
    pause
    exit /b 1
)

echo.
echo [2] Checking ffprobe...
ffprobe -version
if %errorlevel% neq 0 (
    echo ERROR: ffprobe not found!
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS! FFmpeg is properly installed
echo ========================================
pause
```

Run: `test_ffmpeg.bat`

## 🔧 Troubleshooting

### Error: "ffmpeg is not recognized as an internal or external command"

**Solution:**
1. Restart Command Prompt (PATH changes require restart)
2. Verify PATH: `echo %PATH%` (should contain `C:\ffmpeg\bin`)
3. Try full path: `C:\ffmpeg\bin\ffmpeg.exe -version`

### Error: "missing VCRUNTIME140.dll"

**Solution:**
Download and install:
- Microsoft Visual C++ Redistributable (x64)
- Link: https://aka.ms/vs/17/release/vc_redist.x64.exe

### Error: Permission Denied

**Solution:**
- Run Command Prompt as Administrator
- Check antivirus isn't blocking ffmpeg.exe

## 📝 Sample Video Files for Testing

Download sample videos:
- Big Buck Bunny: https://sample-videos.com/
- Test Files: https://file-examples.com/

Or use online converter to create test videos.

## ✅ Final Verification Checklist

- [ ] FFmpeg extracted to `C:\ffmpeg`
- [ ] `C:\ffmpeg\bin` added to System PATH
- [ ] Command Prompt restarted
- [ ] `ffmpeg -version` works
- [ ] `ffprobe -version` works
- [ ] Test conversion completed successfully
- [ ] Video info extraction works

## 🚀 Ready to Use

Once all checkmarks are complete, your Video Converter application is ready to use!

Start your Tomcat server and access:
```
http://localhost:8080/VideoConverter
```
