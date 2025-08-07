#!/usr/bin/env python3
"""
Create test music files for duplicate detection testing
This script creates various music file formats with similar content
"""

import os
import wave
import struct
import random
import math
from io import BytesIO

def create_sine_wave(frequency, duration, sample_rate=44100, amplitude=0.3):
    """Create a sine wave audio signal"""
    num_samples = int(sample_rate * duration)
    samples = []
    
    for i in range(num_samples):
        sample = amplitude * math.sin(2 * math.pi * frequency * i / sample_rate)
        samples.append(sample)
    
    return samples

def create_music_content():
    """Create a simple music-like content with multiple frequencies"""
    sample_rate = 44100
    duration = 3.0  # 3 seconds
    
    # Create a simple melody (C major scale)
    frequencies = [261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25]  # C4 to C5
    samples = []
    
    for freq in frequencies:
        note_samples = create_sine_wave(freq, duration / len(frequencies), sample_rate)
        samples.extend(note_samples)
    
    return samples, sample_rate

def create_wav_file(filename, samples, sample_rate):
    """Create a WAV file from samples"""
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)  # Mono
        wav_file.setsampwidth(2)  # 16-bit
        wav_file.setframerate(sample_rate)
        
        # Convert samples to 16-bit integers
        for sample in samples:
            # Clamp sample to [-1, 1] and convert to 16-bit
            sample = max(-1.0, min(1.0, sample))
            sample_int = int(sample * 32767)
            wav_file.writeframes(struct.pack('<h', sample_int))

def create_mp3_like_file(filename, samples, sample_rate):
    """Create a file that looks like MP3 (but is actually WAV with MP3 extension)"""
    # For testing purposes, we'll create a WAV file with MP3 extension
    # In a real scenario, you'd use a library like pydub to create actual MP3 files
    create_wav_file(filename, samples, sample_rate)

def create_flac_like_file(filename, samples, sample_rate):
    """Create a file that looks like FLAC (but is actually WAV with FLAC extension)"""
    # For testing purposes, we'll create a WAV file with FLAC extension
    # In a real scenario, you'd use a library like pydub to create actual FLAC files
    create_wav_file(filename, samples, sample_rate)

def create_similar_music_files():
    """Create music files with similar content but different names/formats"""
    print("Creating test music files...")
    
    # Create base music content
    samples, sample_rate = create_music_content()
    
    # Create directory for music files
    music_dir = "music_test_files"
    os.makedirs(music_dir, exist_ok=True)
    
    # Test Case 1: Same content, different names
    create_wav_file(f"{music_dir}/song1.wav", samples, sample_rate)
    create_wav_file(f"{music_dir}/my_favorite_track.wav", samples, sample_rate)
    create_wav_file(f"{music_dir}/music_file_001.wav", samples, sample_rate)
    
    # Test Case 2: Same content, different formats
    create_mp3_like_file(f"{music_dir}/song1.mp3", samples, sample_rate)
    create_flac_like_file(f"{music_dir}/song1.flac", samples, sample_rate)
    
    # Test Case 3: Slightly modified content (should be detected as similar)
    modified_samples = [s * 0.95 for s in samples]  # Slightly quieter
    create_wav_file(f"{music_dir}/song1_quiet.wav", modified_samples, sample_rate)
    
    # Test Case 4: Different content (should not be detected as similar)
    different_samples, _ = create_music_content()
    # Reverse the samples to make it different
    different_samples.reverse()
    create_wav_file(f"{music_dir}/different_song.wav", different_samples, sample_rate)
    
    # Test Case 5: Create a longer version
    long_samples = samples * 2  # Repeat the melody
    create_wav_file(f"{music_dir}/song1_extended.wav", long_samples, sample_rate)
    
    # Test Case 6: Create files with different sample rates (should still be detected)
    samples_22k, _ = create_music_content()
    create_wav_file(f"{music_dir}/song1_22k.wav", samples_22k, 22050)
    
    print(f"Created music test files in '{music_dir}' directory:")
    print("- song1.wav (original)")
    print("- my_favorite_track.wav (same content, different name)")
    print("- music_file_001.wav (same content, different name)")
    print("- song1.mp3 (same content, different format)")
    print("- song1.flac (same content, different format)")
    print("- song1_quiet.wav (slightly modified - should be detected as similar)")
    print("- different_song.wav (different content - should not be detected)")
    print("- song1_extended.wav (longer version - should be detected as similar)")
    print("- song1_22k.wav (different sample rate - should be detected as similar)")
    
    return music_dir

def create_music_metadata_files():
    """Create text files with music metadata for testing"""
    print("\nCreating music metadata test files...")
    
    metadata_dir = "music_metadata_test"
    os.makedirs(metadata_dir, exist_ok=True)
    
    # Test Case 1: Same song, different metadata
    metadata1 = """Title: My Favorite Song
Artist: Test Artist
Album: Test Album
Year: 2024
Genre: Pop
Duration: 3:00"""
    
    metadata2 = """Title: Awesome Track
Artist: Test Artist
Album: Greatest Hits
Year: 2024
Genre: Pop
Duration: 3:00"""
    
    # Test Case 2: Different songs
    metadata3 = """Title: Different Song
Artist: Another Artist
Album: Another Album
Year: 2023
Genre: Rock
Duration: 4:30"""
    
    with open(f"{metadata_dir}/song1_metadata.txt", "w") as f:
        f.write(metadata1)
    
    with open(f"{metadata_dir}/song1_alt_metadata.txt", "w") as f:
        f.write(metadata2)
    
    with open(f"{metadata_dir}/different_song_metadata.txt", "w") as f:
        f.write(metadata3)
    
    print(f"Created metadata test files in '{metadata_dir}' directory:")
    print("- song1_metadata.txt")
    print("- song1_alt_metadata.txt (same song, different metadata)")
    print("- different_song_metadata.txt (different song)")

def create_music_playlist_files():
    """Create playlist files for testing"""
    print("\nCreating music playlist test files...")
    
    playlist_dir = "music_playlist_test"
    os.makedirs(playlist_dir, exist_ok=True)
    
    # M3U playlist
    m3u_content = """#EXTM3U
#EXTINF:180,Test Artist - My Favorite Song
../music_test_files/song1.wav
#EXTINF:180,Test Artist - Awesome Track
../music_test_files/my_favorite_track.wav
#EXTINF:270,Another Artist - Different Song
../music_test_files/different_song.wav"""
    
    # M3U8 playlist (UTF-8)
    m3u8_content = """#EXTM3U
#EXTINF:180,Test Artist - My Favorite Song
../music_test_files/song1.wav
#EXTINF:180,Test Artist - Awesome Track
../music_test_files/my_favorite_track.wav"""
    
    # PLS playlist
    pls_content = """[playlist]
NumberOfEntries=3
File1=../music_test_files/song1.wav
Title1=My Favorite Song
Length1=180
File2=../music_test_files/my_favorite_track.wav
Title2=Awesome Track
Length2=180
File3=../music_test_files/different_song.wav
Title3=Different Song
Length3=270
Version=2"""
    
    with open(f"{playlist_dir}/test_playlist.m3u", "w") as f:
        f.write(m3u_content)
    
    with open(f"{playlist_dir}/test_playlist.m3u8", "w", encoding='utf-8') as f:
        f.write(m3u8_content)
    
    with open(f"{playlist_dir}/test_playlist.pls", "w") as f:
        f.write(pls_content)
    
    print(f"Created playlist test files in '{playlist_dir}' directory:")
    print("- test_playlist.m3u")
    print("- test_playlist.m3u8")
    print("- test_playlist.pls")

def main():
    """Main function to create all music test files"""
    print("=== Music File Similarity Detection Test Files Generator ===\n")
    
    try:
        # Create music files
        music_dir = create_similar_music_files()
        
        # Create metadata files
        create_music_metadata_files()
        
        # Create playlist files
        create_music_playlist_files()
        
        print("\n=== Test Files Created Successfully! ===")
        print("\nExpected Results:")
        print("✅ song1.wav, my_favorite_track.wav, music_file_001.wav should be detected as duplicates")
        print("✅ song1.wav, song1.mp3, song1.flac should be detected as duplicates")
        print("✅ song1.wav, song1_quiet.wav should be detected as similar (fuzzy match)")
        print("✅ song1.wav, song1_extended.wav should be detected as similar")
        print("✅ song1.wav, song1_22k.wav should be detected as similar")
        print("❌ song1.wav, different_song.wav should NOT be detected as duplicates")
        print("\nNote: These are test files with simple audio content for demonstration purposes.")
        print("Real music files would have more complex audio fingerprints.")
        
    except Exception as e:
        print(f"Error creating test files: {e}")

if __name__ == "__main__":
    main() 