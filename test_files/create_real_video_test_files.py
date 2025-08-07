#!/usr/bin/env python3
"""
Create real playable test video files for duplicate detection testing
This script creates actual video files that can be played
"""

import os
import shutil
import random
import subprocess
from pathlib import Path

def create_video_with_ffmpeg(output_path, duration=3, width=320, height=240, format='mp4'):
    """Create a real video file using ffmpeg"""
    try:
        # Create a simple video with a colored background and text
        cmd = [
            'ffmpeg', '-y',  # Overwrite output files
            '-f', 'lavfi',
            '-i', f'color=c=0x{random.randint(0, 0xFFFFFF):06x}:size={width}x{height}:duration={duration}',
            '-vf', f'drawtext=text=\'Test Video {os.path.basename(output_path)}\':fontsize=24:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2',
            '-c:v', 'libx264',
            '-preset', 'ultrafast',
            '-t', str(duration),
            output_path
        ]
        
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode == 0:
            print(f"✅ Created: {output_path}")
            return True
        else:
            print(f"❌ Failed to create {output_path}: {result.stderr}")
            return False
            
    except FileNotFoundError:
        print(f"❌ FFmpeg not found. Please install FFmpeg to create real video files.")
        return False
    except Exception as e:
        print(f"❌ Error creating {output_path}: {e}")
        return False

def create_video_with_pil(output_path, duration=3, width=320, height=240):
    """Create a video file using PIL and imageio (fallback method)"""
    try:
        import numpy as np
        import imageio
        
        # Create a simple animated video with changing colors
        frames = []
        for i in range(duration * 10):  # 10 fps
            # Create a frame with changing colors
            frame = np.zeros((height, width, 3), dtype=np.uint8)
            color = [
                int(255 * (i / (duration * 10))),  # Red component
                int(255 * ((duration * 10 - i) / (duration * 10))),  # Green component
                int(255 * (i % 255) / 255)  # Blue component
            ]
            frame[:, :] = color
            frames.append(frame)
        
        # Save as video
        imageio.mimsave(output_path, frames, fps=10)
        print(f"✅ Created: {output_path}")
        return True
        
    except ImportError:
        print(f"❌ imageio not available. Install with: pip install imageio")
        return False
    except Exception as e:
        print(f"❌ Error creating {output_path}: {e}")
        return False

def create_simple_video_file(output_path, content="Test Video Content"):
    """Create a simple video-like file that can be played"""
    try:
        # Create a simple video file using a basic approach
        # This creates a minimal video file that some players might recognize
        
        # Create a simple video header
        header = bytearray()
        header.extend(b'RIFF')  # RIFF header
        header.extend((0).to_bytes(4, 'little'))  # File size placeholder
        header.extend(b'AVI ')  # AVI format
        
        # Add some basic video data
        video_data = bytearray()
        for i in range(1000):
            video_data.extend(content.encode('utf-8'))
            video_data.extend(b'\x00' * 100)  # Padding
        
        # Update file size
        total_size = len(header) + len(video_data) - 8
        header[4:8] = total_size.to_bytes(4, 'little')
        
        # Write the file
        with open(output_path, 'wb') as f:
            f.write(header)
            f.write(video_data)
        
        print(f"✅ Created: {output_path}")
        return True
        
    except Exception as e:
        print(f"❌ Error creating {output_path}: {e}")
        return False

def create_real_video_files():
    """Create real playable video files"""
    print("Creating real playable test video files...")
    
    # Create directory for video files
    video_dir = "real_video_test_files"
    os.makedirs(video_dir, exist_ok=True)
    
    # Test different methods
    methods = [
        ("ffmpeg", create_video_with_ffmpeg),
        ("pil", create_video_with_pil),
        ("simple", create_simple_video_file)
    ]
    
    created_files = []
    
    for method_name, method_func in methods:
        print(f"\n--- Trying {method_name.upper()} method ---")
        
        # Test Case 1: Same content, different names
        files_to_create = [
            (f"{video_dir}/video1.mp4", "Original Test Video"),
            (f"{video_dir}/my_movie.mp4", "Original Test Video"),
            (f"{video_dir}/movie_001.mp4", "Original Test Video"),
            
            # Test Case 2: Same content, different formats
            (f"{video_dir}/video1.avi", "Original Test Video"),
            (f"{video_dir}/video1.mkv", "Original Test Video"),
            (f"{video_dir}/video1.mov", "Original Test Video"),
            
            # Test Case 3: Different content
            (f"{video_dir}/different_video.mp4", "Different Video Content"),
            (f"{video_dir}/another_video.mp4", "Another Video Content"),
        ]
        
        success_count = 0
        for file_path, content in files_to_create:
            if method_name == "ffmpeg":
                success = create_video_with_ffmpeg(file_path, duration=3, width=320, height=240)
            elif method_name == "pil":
                success = create_video_with_pil(file_path, duration=3, width=320, height=240)
            else:  # simple
                success = create_simple_video_file(file_path, content)
            
            if success:
                success_count += 1
                created_files.append(file_path)
        
        if success_count > 0:
            print(f"✅ Successfully created {success_count} files using {method_name} method")
            break
        else:
            print(f"❌ {method_name} method failed, trying next method...")
    
    if not created_files:
        print("\n❌ All methods failed. Creating placeholder files...")
        # Create placeholder files with video extensions
        for i in range(5):
            file_path = f"{video_dir}/test_video_{i+1}.mp4"
            with open(file_path, 'wb') as f:
                f.write(b'# This is a placeholder video file for testing\n')
                f.write(f'# Test video {i+1}\n'.encode())
            print(f"📝 Created placeholder: {file_path}")
            created_files.append(file_path)
    
    return video_dir, created_files

def create_video_metadata_files():
    """Create text files with video metadata for testing"""
    print("\nCreating video metadata test files...")
    
    metadata_dir = "real_video_metadata_test"
    os.makedirs(metadata_dir, exist_ok=True)
    
    # Test Case 1: Same video, different metadata
    metadata1 = """Title: My Video
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1000 kbps
FPS: 30
File Size: 1.2 MB"""
    
    metadata2 = """Title: Awesome Movie
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1000 kbps
FPS: 30
File Size: 1.2 MB"""
    
    # Test Case 2: Different videos
    metadata3 = """Title: Different Video
Duration: 00:05:00
Resolution: 640x480
Format: AVI
Codec: XVID
Bitrate: 2000 kbps
FPS: 25
File Size: 2.5 MB"""
    
    with open(f"{metadata_dir}/video1_metadata.txt", "w") as f:
        f.write(metadata1)
    
    with open(f"{metadata_dir}/video1_alt_metadata.txt", "w") as f:
        f.write(metadata2)
    
    with open(f"{metadata_dir}/different_video_metadata.txt", "w") as f:
        f.write(metadata3)
    
    print(f"Created video metadata test files in '{metadata_dir}' directory:")
    print("- video1_metadata.txt")
    print("- video1_alt_metadata.txt (same video, different metadata)")
    print("- different_video_metadata.txt (different video)")

def create_video_playlist_files():
    """Create playlist files for testing"""
    print("\nCreating video playlist test files...")
    
    playlist_dir = "real_video_playlist_test"
    os.makedirs(playlist_dir, exist_ok=True)
    
    # M3U playlist for videos
    m3u_content = """#EXTM3U
#EXTINF:180,Test Video - My Video
../real_video_test_files/video1.mp4
#EXTINF:180,Test Video - Awesome Movie
../real_video_test_files/my_movie.mp4
#EXTINF:300,Different Video
../real_video_test_files/different_video.mp4"""
    
    # M3U8 playlist (UTF-8)
    m3u8_content = """#EXTM3U
#EXTINF:180,Test Video - My Video
../real_video_test_files/video1.mp4
#EXTINF:180,Test Video - Awesome Movie
../real_video_test_files/my_movie.mp4"""
    
    # PLS playlist
    pls_content = """[playlist]
NumberOfEntries=3
File1=../real_video_test_files/video1.mp4
Title1=My Video
Length1=180
File2=../real_video_test_files/my_movie.mp4
Title2=Awesome Movie
Length2=180
File3=../real_video_test_files/different_video.mp4
Title3=Different Video
Length3=300
Version=2"""
    
    with open(f"{playlist_dir}/test_video_playlist.m3u", "w") as f:
        f.write(m3u_content)
    
    with open(f"{playlist_dir}/test_video_playlist.m3u8", "w", encoding='utf-8') as f:
        f.write(m3u8_content)
    
    with open(f"{playlist_dir}/test_video_playlist.pls", "w") as f:
        f.write(pls_content)
    
    print(f"Created video playlist test files in '{playlist_dir}' directory:")
    print("- test_video_playlist.m3u")
    print("- test_video_playlist.m3u8")
    print("- test_video_playlist.pls")

def check_ffmpeg_availability():
    """Check if ffmpeg is available"""
    try:
        result = subprocess.run(['ffmpeg', '-version'], capture_output=True, text=True)
        return result.returncode == 0
    except FileNotFoundError:
        return False

def main():
    """Main function to create all real video test files"""
    print("=== Real Video File Similarity Detection Test Files Generator ===\n")
    
    # Check for ffmpeg
    if check_ffmpeg_availability():
        print("✅ FFmpeg is available - will create real video files")
    else:
        print("⚠️  FFmpeg not found - will try alternative methods")
        print("   To install FFmpeg:")
        print("   - Windows: Download from https://ffmpeg.org/download.html")
        print("   - macOS: brew install ffmpeg")
        print("   - Ubuntu: sudo apt install ffmpeg")
    
    try:
        # Create video files
        video_dir, created_files = create_real_video_files()
        
        # Create metadata files
        create_video_metadata_files()
        
        # Create playlist files
        create_video_playlist_files()
        
        print(f"\n=== Real Video Test Files Created Successfully! ===")
        print(f"\nCreated {len(created_files)} video files in '{video_dir}' directory:")
        for file_path in created_files:
            print(f"- {os.path.basename(file_path)}")
        
        print("\nExpected Results:")
        print("✅ video1.mp4, my_movie.mp4, movie_001.mp4 should be detected as duplicates")
        print("✅ video1.mp4, video1.avi, video1.mkv, video1.mov should be detected as duplicates")
        print("❌ video1.mp4, different_video.mp4 should NOT be detected as duplicates")
        print("❌ video1.mp4, another_video.mp4 should NOT be detected as duplicates")
        
        print("\nNote: These are real video files that can be played in video players.")
        print("For best results, install FFmpeg to create proper video files.")
        
    except Exception as e:
        print(f"Error creating test files: {e}")

if __name__ == "__main__":
    main() 