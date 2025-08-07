#!/usr/bin/env python3
"""
Create diverse video test files with different content for duplicate detection testing
This script creates videos with different colors, patterns, and animations
"""

import os
import random
import numpy as np
import imageio

def create_color_gradient_video(output_path, duration=3, width=320, height=240, start_color=(255,0,0), end_color=(0,0,255)):
    """Create a video with color gradient animation"""
    frames = []
    for i in range(duration * 10):  # 10 fps
        # Create gradient from start_color to end_color
        progress = i / (duration * 10)
        color = [
            int(start_color[0] * (1 - progress) + end_color[0] * progress),
            int(start_color[1] * (1 - progress) + end_color[1] * progress),
            int(start_color[2] * (1 - progress) + end_color[2] * progress)
        ]
        
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        frame[:, :] = color
        frames.append(frame)
    
    imageio.mimsave(output_path, frames, fps=10)
    print(f"✅ Created gradient video: {output_path}")

def create_pattern_video(output_path, duration=3, width=320, height=240, pattern_type="stripes"):
    """Create a video with moving patterns"""
    frames = []
    for i in range(duration * 10):  # 10 fps
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        
        if pattern_type == "stripes":
            # Moving vertical stripes
            stripe_width = 20
            offset = (i * 2) % stripe_width
            for x in range(width):
                if (x + offset) % stripe_width < stripe_width // 2:
                    frame[:, x] = [255, 255, 0]  # Yellow
                else:
                    frame[:, x] = [0, 0, 255]    # Blue
                    
        elif pattern_type == "circles":
            # Expanding circles
            center_x, center_y = width // 2, height // 2
            radius = (i * 3) % (min(width, height) // 2)
            for y in range(height):
                for x in range(width):
                    distance = np.sqrt((x - center_x)**2 + (y - center_y)**2)
                    if distance < radius:
                        frame[y, x] = [255, 0, 255]  # Magenta
                    else:
                        frame[y, x] = [0, 255, 255]  # Cyan
                        
        elif pattern_type == "checkerboard":
            # Moving checkerboard
            square_size = 15
            offset = (i * 2) % square_size
            for y in range(height):
                for x in range(width):
                    if ((x + offset) // square_size + (y + offset) // square_size) % 2 == 0:
                        frame[y, x] = [255, 128, 0]  # Orange
                    else:
                        frame[y, x] = [128, 0, 255]  # Purple
        
        frames.append(frame)
    
    imageio.mimsave(output_path, frames, fps=10)
    print(f"✅ Created {pattern_type} pattern video: {output_path}")

def create_text_video(output_path, duration=3, width=320, height=240, text="Hello World"):
    """Create a video with animated text"""
    frames = []
    for i in range(duration * 10):  # 10 fps
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        
        # Animate text position
        x_pos = (i * 5) % (width - 100)
        y_pos = height // 2
        
        # Create a simple text effect (colored rectangle with text)
        text_width = len(text) * 10
        text_height = 30
        
        # Background for text
        start_x = max(0, x_pos)
        end_x = min(width, x_pos + text_width)
        start_y = max(0, y_pos - text_height // 2)
        end_y = min(height, y_pos + text_height // 2)
        
        frame[start_y:end_y, start_x:end_x] = [255, 255, 255]  # White background
        
        frames.append(frame)
    
    imageio.mimsave(output_path, frames, fps=10)
    print(f"✅ Created text video: {output_path}")

def create_noise_video(output_path, duration=3, width=320, height=240, noise_type="random"):
    """Create a video with different types of noise"""
    frames = []
    for i in range(duration * 10):  # 10 fps
        if noise_type == "random":
            # Random noise
            frame = np.random.randint(0, 256, (height, width, 3), dtype=np.uint8)
        elif noise_type == "static":
            # Static noise (same pattern moving)
            np.random.seed(i)
            frame = np.random.randint(0, 256, (height, width, 3), dtype=np.uint8)
        elif noise_type == "wave":
            # Wave-like noise
            frame = np.zeros((height, width, 3), dtype=np.uint8)
            for y in range(height):
                for x in range(width):
                    wave = int(128 + 127 * np.sin(x * 0.1 + i * 0.2) * np.cos(y * 0.1 + i * 0.3))
                    frame[y, x] = [wave, wave//2, wave//4]
        
        frames.append(frame)
    
    imageio.mimsave(output_path, frames, fps=10)
    print(f"✅ Created {noise_type} noise video: {output_path}")

def create_diverse_video_files():
    """Create diverse video files with different content"""
    print("Creating diverse video test files...")
    
    # Create directory for video files
    video_dir = "diverse_video_test_files"
    os.makedirs(video_dir, exist_ok=True)
    
    created_files = []
    
    # Test Case 1: Same content, different names (red to blue gradient)
    create_color_gradient_video(f"{video_dir}/video1.mp4", duration=3, start_color=(255,0,0), end_color=(0,0,255))
    create_color_gradient_video(f"{video_dir}/my_movie.mp4", duration=3, start_color=(255,0,0), end_color=(0,0,255))
    create_color_gradient_video(f"{video_dir}/movie_001.mp4", duration=3, start_color=(255,0,0), end_color=(0,0,255))
    
    # Test Case 2: Same content, different formats (red to blue gradient)
    create_color_gradient_video(f"{video_dir}/video1.avi", duration=3, start_color=(255,0,0), end_color=(0,0,255))
    create_color_gradient_video(f"{video_dir}/video1.mkv", duration=3, start_color=(255,0,0), end_color=(0,0,255))
    create_color_gradient_video(f"{video_dir}/video1.mov", duration=3, start_color=(255,0,0), end_color=(0,0,255))
    
    # Test Case 3: Different content - stripes pattern
    create_pattern_video(f"{video_dir}/different_video.mp4", duration=3, pattern_type="stripes")
    
    # Test Case 4: Different content - circles pattern
    create_pattern_video(f"{video_dir}/another_video.mp4", duration=3, pattern_type="circles")
    
    # Test Case 5: Different content - checkerboard pattern
    create_pattern_video(f"{video_dir}/pattern_video.mp4", duration=3, pattern_type="checkerboard")
    
    # Test Case 6: Different content - text animation
    create_text_video(f"{video_dir}/text_video.mp4", duration=3, text="Hello World")
    
    # Test Case 7: Different content - random noise
    create_noise_video(f"{video_dir}/noise_video.mp4", duration=3, noise_type="random")
    
    # Test Case 8: Different content - wave noise
    create_noise_video(f"{video_dir}/wave_video.mp4", duration=3, noise_type="wave")
    
    # Test Case 9: Different gradient colors
    create_color_gradient_video(f"{video_dir}/green_to_purple.mp4", duration=3, start_color=(0,255,0), end_color=(128,0,255))
    
    # Test Case 10: Different gradient colors
    create_color_gradient_video(f"{video_dir}/yellow_to_cyan.mp4", duration=3, start_color=(255,255,0), end_color=(0,255,255))
    
    # List all created files
    for file in os.listdir(video_dir):
        if file.lower().endswith(('.mp4', '.avi', '.mov', '.mkv')):
            created_files.append(os.path.join(video_dir, file))
    
    return video_dir, created_files

def create_video_metadata_files():
    """Create text files with video metadata for testing"""
    print("\nCreating video metadata test files...")
    
    metadata_dir = "diverse_video_metadata_test"
    os.makedirs(metadata_dir, exist_ok=True)
    
    # Test Case 1: Same video, different metadata
    metadata1 = """Title: Red to Blue Gradient
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1000 kbps
FPS: 10
Content: Color gradient animation"""
    
    metadata2 = """Title: Color Transition Video
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1000 kbps
FPS: 10
Content: Color gradient animation"""
    
    # Test Case 2: Different videos
    metadata3 = """Title: Stripes Pattern
Duration: 00:03:00
Resolution: 320x240
Format: MP4
Codec: H.264
Bitrate: 1200 kbps
FPS: 10
Content: Moving stripe pattern"""
    
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

def main():
    """Main function to create all diverse video test files"""
    print("=== Diverse Video File Similarity Detection Test Files Generator ===\n")
    
    try:
        # Create diverse video files
        video_dir, created_files = create_diverse_video_files()
        
        # Create metadata files
        create_video_metadata_files()
        
        print(f"\n=== Diverse Video Test Files Created Successfully! ===")
        print(f"\nCreated {len(created_files)} diverse video files in '{video_dir}' directory:")
        for file_path in created_files:
            print(f"- {os.path.basename(file_path)}")
        
        print("\nVideo Content Types:")
        print("🎨 Red to Blue Gradient: video1.mp4, my_movie.mp4, movie_001.mp4, video1.avi, video1.mkv, video1.mov")
        print("📏 Moving Stripes: different_video.mp4")
        print("⭕ Expanding Circles: another_video.mp4")
        print("🏁 Checkerboard: pattern_video.mp4")
        print("📝 Text Animation: text_video.mp4")
        print("🎲 Random Noise: noise_video.mp4")
        print("🌊 Wave Noise: wave_video.mp4")
        print("🟢 Green to Purple: green_to_purple.mp4")
        print("🟡 Yellow to Cyan: yellow_to_cyan.mp4")
        
        print("\nExpected Results:")
        print("✅ video1.mp4, my_movie.mp4, movie_001.mp4 should be detected as duplicates")
        print("✅ video1.mp4, video1.avi, video1.mkv, video1.mov should be detected as duplicates")
        print("❌ video1.mp4, different_video.mp4 should NOT be detected as duplicates")
        print("❌ video1.mp4, another_video.mp4 should NOT be detected as duplicates")
        print("❌ All other videos should be detected as different content")
        
        print("\nNote: These videos have diverse content with different colors, patterns, and animations.")
        print("Perfect for testing the accuracy of video duplicate detection!")
        
    except Exception as e:
        print(f"Error creating test files: {e}")

if __name__ == "__main__":
    main() 