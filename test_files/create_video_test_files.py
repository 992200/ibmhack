#!/usr/bin/env python3
"""
Create test video files for duplicate detection testing
This script creates various video file formats with similar content
"""

import os
import struct
import random

def create_simple_video_header(width=320, height=240, duration=3):
    """Create a simple video-like file header"""
    # This creates a file that looks like a video but is actually just a header
    # In a real scenario, you'd use a library like OpenCV or FFmpeg to create actual videos
    
    header = bytearray()
    
    # Add some video-like header bytes
    header.extend(b'VIDEO')  # Magic bytes
    header.extend(struct.pack('<I', width))   # Width
    header.extend(struct.pack('<I', height))  # Height
    header.extend(struct.pack('<I', duration)) # Duration in seconds
    
    # Add some random video-like data
    for _ in range(1000):
        header.append(random.randint(0, 255))
    
    return header

def create_video_file(filename, width=320, height=240, duration=3, quality=1.0):
    """Create a video-like file"""
    header = create_simple_video_header(width, height, duration)
    
    # Adjust file size based on quality
    target_size = int(len(header) * quality)
    
    with open(filename, 'wb') as f:
        f.write(header[:target_size])
        
        # Add some additional data to reach target size
        while f.tell() < target_size:
            f.write(bytes([random.randint(0, 255)]))

def create_similar_video_files():
    """Create video files with similar content but different names/formats"""
    print("Creating test video files...")
    
    # Create directory for video files
    video_dir = "video_test_files"
    os.makedirs(video_dir, exist_ok=True)
    
    # Test Case 1: Same content, different names
    create_video_file(f"{video_dir}/video1.mp4", 320, 240, 3, 1.0)
    create_video_file(f"{video_dir}/my_movie.mp4", 320, 240, 3, 1.0)
    create_video_file(f"{video_dir}/movie_001.mp4", 320, 240, 3, 1.0)
    
    # Test Case 2: Same content, different formats
    create_video_file(f"{video_dir}/video1.avi", 320, 240, 3, 1.0)
    create_video_file(f"{video_dir}/video1.mkv", 320, 240, 3, 1.0)
    create_video_file(f"{video_dir}/video1.mov", 320, 240, 3, 1.0)
    
    # Test Case 3: Same content, different quality/compression
    create_video_file(f"{video_dir}/video1_hd.mp4", 320, 240, 3, 1.2)  # Higher quality
    create_video_file(f"{video_dir}/video1_low.mp4", 320, 240, 3, 0.8)  # Lower quality
    
    # Test Case 4: Different content (should not be detected as similar)
    create_video_file(f"{video_dir}/different_video.mp4", 640, 480, 5, 1.0)  # Different dimensions and duration
    
    # Test Case 5: Same content, different resolution
    create_video_file(f"{video_dir}/video1_720p.mp4", 1280, 720, 3, 1.0)
    create_video_file(f"{video_dir}/video1_480p.mp4", 854, 480, 3, 1.0)
    
    # Test Case 6: Same content, different duration
    create_video_file(f"{video_dir}/video1_extended.mp4", 320, 240, 6, 1.0)  # Longer duration
    
    # Test Case 7: Same content, different bitrate (simulated by file size)
    create_video_file(f"{video_dir}/video1_high_bitrate.mp4", 320, 240, 3, 1.5)
    create_video_file(f"{video_dir}/video1_low_bitrate.mp4", 320, 240, 3, 0.6)
    
    print(f"Created video test files in '{video_dir}' directory:")
    print("- video1.mp4 (original)")
    print("- my_movie.mp4 (same content, different name)")
    print("- movie_001.mp4 (same content, different name)")
    print("- video1.avi (same content, different format)")
    print("- video1.mkv (same content, different format)")
    print("- video1.mov (same content, different format)")
    print("- video1_hd.mp4 (higher quality - should be detected as similar)")
    print("- video1_low.mp4 (lower quality - should be detected as similar)")
    print("- different_video.mp4 (different content - should NOT be detected)")
    print("- video1_720p.mp4 (different resolution - should be detected as similar)")
    print("- video1_480p.mp4 (different resolution - should be detected as similar)")
    print("- video1_extended.mp4 (longer duration - should be detected as similar)")
    print("- video1_high_bitrate.mp4 (higher bitrate - should be detected as similar)")
    print("- video1_low_bitrate.mp4 (lower bitrate - should be detected as similar)")
    
    return video_dir

def create_video_metadata_files():
    """Create text files with video metadata for testing"""
    print("\nCreating video metadata test files...")
    
    metadata_dir = "video_metadata_test"
    os.makedirs(metadata_dir, exist_ok=True)
    
    # Test Case 1: Same video, different metadata
    metadata1 = """Title: My Video
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1000 kbps
FPS: 30"""
    
    metadata2 = """Title: Awesome Movie
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1000 kbps
FPS: 30"""
    
    # Test Case 2: Different videos
    metadata3 = """Title: Different Video
Duration: 00:05:00
Resolution: 640x480
Format: AVI
Codec: XVID
Bitrate: 2000 kbps
FPS: 25"""
    
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
    """Create video playlist files for testing"""
    print("\nCreating video playlist test files...")
    
    playlist_dir = "video_playlist_test"
    os.makedirs(playlist_dir, exist_ok=True)
    
    # M3U playlist for videos
    m3u_content = """#EXTM3U
#EXTINF:180,Test Video - My Video
../video_test_files/video1.mp4
#EXTINF:180,Test Video - Awesome Movie
../video_test_files/my_movie.mp4
#EXTINF:300,Different Video
../video_test_files/different_video.mp4"""
    
    # M3U8 playlist (UTF-8)
    m3u8_content = """#EXTM3U
#EXTINF:180,Test Video - My Video
../video_test_files/video1.mp4
#EXTINF:180,Test Video - Awesome Movie
../video_test_files/my_movie.mp4"""
    
    # PLS playlist
    pls_content = """[playlist]
NumberOfEntries=3
File1=../video_test_files/video1.mp4
Title1=My Video
Length1=180
File2=../video_test_files/my_movie.mp4
Title2=Awesome Movie
Length2=180
File3=../video_test_files/different_video.mp4
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

def main():
    """Main function to create all video test files"""
    print("=== Video File Similarity Detection Test Files Generator ===\n")
    
    try:
        # Create video files
        video_dir = create_similar_video_files()
        
        # Create metadata files
        create_video_metadata_files()
        
        # Create playlist files
        create_video_playlist_files()
        
        print("\n=== Video Test Files Created Successfully! ===")
        print("\nExpected Results:")
        print("✅ video1.mp4, my_movie.mp4, movie_001.mp4 should be detected as duplicates")
        print("✅ video1.mp4, video1.avi, video1.mkv, video1.mov should be detected as duplicates")
        print("✅ video1.mp4, video1_hd.mp4, video1_low.mp4 should be detected as similar (different quality)")
        print("✅ video1.mp4, video1_720p.mp4, video1_480p.mp4 should be detected as similar (different resolution)")
        print("✅ video1.mp4, video1_extended.mp4 should be detected as similar (different duration)")
        print("✅ video1.mp4, video1_high_bitrate.mp4, video1_low_bitrate.mp4 should be detected as similar")
        print("❌ video1.mp4, different_video.mp4 should NOT be detected as duplicates")
        print("\nNote: These are test files with simple video-like content for demonstration purposes.")
        print("Real video files would have more complex video fingerprints and metadata.")
        
    except Exception as e:
        print(f"Error creating test files: {e}")

if __name__ == "__main__":
    main() 