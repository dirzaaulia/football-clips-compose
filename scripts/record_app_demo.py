import os
import sys
import time
import argparse
import subprocess

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

DEVICE_VIDEO_PATH = "/sdcard/football_clips_demo.mp4"
OUTPUT_DIR = "videos"
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "football_clips_demo.mp4")
PACKAGE_NAME = "com.dirzaaulia.footballclips"
ACTIVITY_NAME = f"{PACKAGE_NAME}/.MainActivity"

def run_adb(cmd, check=True):
    full_cmd = f"adb {cmd}"
    result = subprocess.run(full_cmd, shell=True, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"ADB Error: {result.stderr}")
    return result

def ensure_device_connected():
    res = run_adb("devices")
    lines = [line.strip() for line in res.stdout.splitlines() if line.strip() and not line.startswith("List")]
    if not lines:
        print("❌ Error: Tidak ada perangkat Android yang terhubung via ADB.")
        sys.exit(1)
    print(f"📱 Perangkat terhubung: {lines[0]}")

def perform_automated_walkthrough():
    print("▶️ Menjalankan skenario otomatis...")
    # 1. Buka aplikasi
    run_adb(f"shell am start -n {ACTIVITY_NAME}")
    time.sleep(3.5) # Tunggu splash & data load
    
    # 2. Scroll pelan ke bawah di tab Highlights
    print("   • Scroll daftar cuplikan video (Highlights)...")
    run_adb("shell input swipe 540 1800 540 800 600")
    time.sleep(2)
    run_adb("shell input swipe 540 1800 540 800 600")
    time.sleep(2.5)
    
    # 3. Ganti tema (Top bar action: Droplet theme animation)
    print("   • Menekan tombol ganti tema (Circular droplet transition)...")
    # Tombol tema di top-app-bar kanan (koordinat mendekati ~1180, 200 di layar 1272x2772)
    run_adb("shell input tap 1150 220")
    time.sleep(2.5)

    # 4. Pindah ke tab Jadwal & Skor Langsung (Fixtures)
    print("   • Navigasi ke tab Jadwal & Skor Langsung...")
    # Tab kedua di bottom navigation (~636, 2600)
    run_adb("shell input tap 636 2600")
    time.sleep(3)
    
    # 5. Buka Bottom Sheet Filter Liga
    print("   • Membuka Filter Liga...")
    # Tombol filter di pojok kanan atas layar skor (~1150, 220)
    run_adb("shell input tap 1150 220")
    time.sleep(2.5)
    # Tutup kembali bottom sheet
    run_adb("shell input keyevent 4") # Back key
    time.sleep(1.5)

    # 6. Kembali ke tab awal
    print("   • Kembali ke tab Beranda/Highlights...")
    run_adb("shell input tap 300 2600")
    time.sleep(2)

def main():
    parser = argparse.ArgumentParser(description="Perekam Layar Video Demo Aplikasi Football Clips via ADB")
    parser.add_argument("--duration", type=int, default=20, help="Durasi perekaman dalam detik (default: 20)")
    parser.add_argument("--auto", action="store_true", help="Jalankan navigasi walkthrough otomatis")
    parser.add_argument("--output", default=OUTPUT_FILE, help="Path output file MP4")
    args = parser.parse_args()

    ensure_device_connected()
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    print("\n==================================================")
    print("  Football Clips - Screen Video Recorder")
    print(f"  Durasi: {args.duration} detik")
    print(f"  Mode: {'Walkthrough Otomatis' if args.auto else 'Manual (Anda dapat mengoperasikan HP)'}")
    print(f"  Output: {args.output}")
    print("==================================================\n")

    # Bersihkan file video lama di device jika ada
    run_adb(f"shell rm -f {DEVICE_VIDEO_PATH}")

    # Jalankan screenrecord di latar belakang
    record_proc = subprocess.Popen(
        f"adb shell screenrecord --time-limit {args.duration} --bit-rate 16M {DEVICE_VIDEO_PATH}",
        shell=True
    )

    print(f"🎥 Perekaman dimulai ({args.duration} detik)...")

    if args.auto:
        perform_automated_walkthrough()
        # Tunggu sampai proses screenrecord selesai jika durasi masih ada
        record_proc.wait()
    else:
        print("👉 Silakan operasikan aplikasi di layar HP Anda sekarang!")
        for sec in range(args.duration, 0, -1):
            sys.stdout.write(f"\r⏱️  Sisa waktu perekaman: {sec} detik... ")
            sys.stdout.flush()
            time.sleep(1)
        print("\r⏱️  Perekaman selesai!                         ")
        record_proc.wait()

    # Beri jeda 1 detik agar file flush sempurna di Android
    time.sleep(1)

    print("\n📥 Mengambil (pulling) video dari perangkat...")
    pull_res = run_adb(f"pull {DEVICE_VIDEO_PATH} \"{args.output}\"")
    
    if os.path.exists(args.output):
        size_mb = os.path.getsize(args.output) / (1024 * 1024)
        print(f"✅ Video berhasil disimpan!")
        print(f"   📁 Lokasi: {os.path.abspath(args.output)}")
        print(f"   📊 Ukuran File: {size_mb:.2f} MB")
    else:
        print("❌ Gagal mengunduh file video.")

if __name__ == "__main__":
    main()
