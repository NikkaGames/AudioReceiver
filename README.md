# Android Low-Latency Opus Audio Receiver

This app receives real-time audio over Wi-Fi using Opus (2.5ms frames) and plays it on the Android device with extremely low latency (~25–35ms).

The sender (your PC) streams system audio using FFmpeg, and the Android app decodes it using a foreground service with a low-latency AudioTrack.

---

## How it Works
1. Your PC captures system audio (via virtual cable or WASAPI input).
2. FFmpeg encodes the audio into Opus 2.5ms frames @ 128kbps.
3. FFmpeg streams it via UDP to your Android device.
4. The Android app receives packets, decodes Opus frames, and plays them through a low-latency AudioTrack.

---

## Requirements

### PC
- Windows 10/11
- FFmpeg installed
- A loopback audio device such as:
  - VB-Audio Virtual Cable
  - Stereo Mix

### Android
- Android 8.0+
- Foreground service permission
- Local WiFi network connection

---

## Streaming System Audio from PC (FFmpeg Command)

Run this command on your Windows PC to start sending audio:

```
ffmpeg -f dshow -audio_buffer_size 10 -i audio="CABLE Output (VB-Audio Virtual Cable)" -reorder_queue_size 0 -fflags +discardcorrupt+fastseek+nobuffer -flags +low_delay -avioflags direct -use_wallclock_as_timestamps 1 -vsync 0 -af aresample=async=1:first_pts=0 -c:a libopus -frame_duration 2.5 -application lowdelay -b:a 128k -flush_packets 1 -map 0:a -f data udp://<ANDROID_IP>:8888
```

Replace `<ANDROID_IP>` with your phone’s WiFi IP address (example: `192.168.100.6`).

### Finding Android IP:
Settings → Wi-Fi → Connected Network → IP address

---

## Using the Android App

### 1. Launch the App
You will see:
- Status text (Running / Stopped)
- Start Service / Stop Service buttons

### 2. Start the Receiver
Press **Start Service** to begin listening for UDP audio.

### 3. Stop the Receiver
Press **Stop Service** to shut down the foreground service.

### 4. Notification Controls
While running, a persistent notification appears:
- Shows: “Audio Receiver – Receiving audio stream…”
- Tapping it returns you to the app

---

## Network Notes
- PC and Android must be on the same WiFi network.
- UDP streaming is extremely fast but sensitive to WiFi quality.
- For best results: 5GHz WiFi or wired → WiFi hotspot.

---

## Known Limitations
- Audio quality may degrade with unstable WiFi
- Battery optimizations should be disabled on Android
- App relies on foreground service for consistent playback

---

## License
MIT License
```
Copyright (c) 2025 Nikka

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
---
