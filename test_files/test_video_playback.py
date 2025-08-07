#!/usr/bin/env python3
"""
Test video file playback and properties for duplicate detection testing
"""

import os
import subprocess
import json
from pathlib import Path

def test_video_file(file_path):
    """Test if a video file can be played and get its properties"""
    try:
        # Try to get video information using ffprobe if available
        result = subprocess.run([
            'ffprobe', '-v', 'quiet', '-print_format', 'json', 
            '-show_format', '-show_streams', file_path
        ], capture_output=True, text=True)
        
        if result.returncode == 0:
            try:
                data = json.loads(result.stdout)
                # Extract video properties
                format_info = data.get('format', {})
                streams = data.get('streams', [])
                
                duration = format_info.get('duration', 'Unknown')
                size = format_info.get('size', 'Unknown')
                
                # Find video stream
                video_stream = None
                for stream in streams:
                    if stream.get('codec_type') == 'video':
                        video_stream = stream
                        break
                
                if video_stream:
                    width = video_stream.get('width', 'Unknown')
                    height = video_stream.get('height', 'Unknown')
                    codec = video_stream.get('codec_name', 'Unknown')
                    fps = video_stream.get('r_frame_rate', 'Unknown')
                    
                    print(f"✅ {os.path.basename(file_path)} - Valid video file")
                    print(f"   📏 Resolution: {width}x{height}")
                    print(f"   🎬 Codec: {codec}")
                    print(f"   ⏱️  Duration: {duration}s")
                    print(f"   📊 Size: {size} bytes")
                    print(f"   🎯 FPS: {fps}")
                    return True, {
                        'width': width,
                        'height': height,
                        'codec': codec,
                        'duration': duration,
                        'size': size,
                        'fps': fps
                    }
                else:
                    print(f"❌ {os.path.basename(file_path)} - No video stream found")
                    return False, {}
                    
            except json.JSONDecodeError:
                print(f"✅ {os.path.basename(file_path)} - Valid video file (JSON parsing failed)")
                return True, {}
        else:
            print(f"❌ {os.path.basename(file_path)} - Not a valid video file")
            return False, {}
            
    except FileNotFoundError:
        # Fallback: check file size and extension
        if os.path.exists(file_path):
            size = os.path.getsize(file_path)
            print(f"📁 {os.path.basename(file_path)} - Size: {size} bytes (ffprobe not available)")
            return size > 1000, {'size': size}  # Assume valid if > 1KB
        else:
            print(f"❌ {os.path.basename(file_path)} - File not found")
            return False, {}

def analyze_video_content(file_path):
    """Analyze video content for duplicate detection testing"""
    filename = os.path.basename(file_path)
    
    # Define expected content based on filename
    content_map = {
        'video1.mp4': 'Red to Blue Gradient',
        'my_movie.mp4': 'Red to Blue Gradient',
        'movie_001.mp4': 'Red to Blue Gradient',
        'video1.avi': 'Red to Blue Gradient',
        'video1.mkv': 'Red to Blue Gradient',
        'video1.mov': 'Red to Blue Gradient',
        'different_video.mp4': 'Moving Stripes Pattern',
        'another_video.mp4': 'Expanding Circles Pattern',
        'pattern_video.mp4': 'Checkerboard Pattern',
        'text_video.mp4': 'Text Animation',
        'noise_video.mp4': 'Random Noise',
        'wave_video.mp4': 'Wave Noise',
        'green_to_purple.mp4': 'Green to Purple Gradient',
        'yellow_to_cyan.mp4': 'Yellow to Cyan Gradient'
    }
    
    return content_map.get(filename, 'Unknown Content')

def get_duplicate_groups():
    """Define expected duplicate groups for testing"""
    return {
        'Group 1 - Red to Blue Gradient': [
            'video1.mp4', 'my_movie.mp4', 'movie_001.mp4',
            'video1.avi', 'video1.mkv', 'video1.mov'
        ],
        'Group 2 - Different Content': [
            'different_video.mp4', 'another_video.mp4', 'pattern_video.mp4',
            'text_video.mp4', 'noise_video.mp4', 'wave_video.mp4',
            'green_to_purple.mp4', 'yellow_to_cyan.mp4'
        ]
    }

def main():
    """Test all video files in the diverse_video_test_files directory"""
    print("=== Video File Playback Test for Duplicate Detection ===\n")
    
    video_dir = "diverse_video_test_files"
    if not os.path.exists(video_dir):
        print(f"❌ Directory '{video_dir}' not found")
        print("Please run 'python create_diverse_video_test_files.py' first")
        return
    
    video_files = []
    for file in os.listdir(video_dir):
        if file.lower().endswith(('.mp4', '.avi', '.mov', '.mkv', '.webm')):
            video_files.append(os.path.join(video_dir, file))
    
    if not video_files:
        print("❌ No video files found")
        return
    
    print(f"Found {len(video_files)} video files to test:\n")
    
    valid_count = 0
    video_properties = {}
    
    for video_file in sorted(video_files):
        is_valid, properties = test_video_file(video_file)
        if is_valid:
            valid_count += 1
            video_properties[os.path.basename(video_file)] = properties
        print()  # Add spacing between files
    
    print("=" * 60)
    print("=== ANALYSIS RESULTS ===")
    print("=" * 60)
    
    print(f"\n📊 Overall Results:")
    print(f"Total files: {len(video_files)}")
    print(f"Valid files: {valid_count}")
    print(f"Success rate: {valid_count/len(video_files)*100:.1f}%")
    
    if valid_count > 0:
        print(f"\n✅ Video files are ready for duplicate detection testing!")
        
        # Show content analysis
        print(f"\n🎬 Content Analysis:")
        print("-" * 40)
        for video_file in sorted(video_files):
            filename = os.path.basename(video_file)
            content = analyze_video_content(video_file)
            print(f"📹 {filename}: {content}")
        
        # Show expected duplicate groups
        print(f"\n🔍 Expected Duplicate Groups:")
        print("-" * 40)
        duplicate_groups = get_duplicate_groups()
        for group_name, files in duplicate_groups.items():
            print(f"\n{group_name}:")
            for file in files:
                print(f"  • {file}")
        
        # Show testing instructions
        print(f"\n🚀 Testing Instructions:")
        print("-" * 40)
        print("1. Start your Spring Boot application")
        print("2. Open browser and go to http://localhost:8080")
        print("3. Enter directory path: D:\\IBM_Hackerthonn\\IBM_Hackerthon\\test_files\\diverse_video_test_files")
        print("4. Click 'Launch Scan' to test video duplicate detection")
        print("5. Verify that:")
        print("   ✅ Red to Blue Gradient videos are detected as duplicates")
        print("   ✅ Different content videos are NOT detected as duplicates")
        print("   ✅ Similarity percentages are shown for each file")
        
        # Show file properties summary
        print(f"\n📋 File Properties Summary:")
        print("-" * 40)
        for filename, props in video_properties.items():
            if props:
                size = props.get('size', 'Unknown')
                duration = props.get('duration', 'Unknown')
                resolution = f"{props.get('width', '?')}x{props.get('height', '?')}"
                print(f"📹 {filename}: {size} bytes, {duration}s, {resolution}")
        
    else:
        print("\n❌ No valid video files found. Please check the video creation process.")
        print("Try running: python create_diverse_video_test_files.py")

if __name__ == "__main__":
    main() 