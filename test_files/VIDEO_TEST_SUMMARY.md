# Video Duplicate Detection Test Summary

## 🎬 Overview
This document summarizes the video duplicate detection testing setup with diverse video files containing different content types.

## 📁 Test Files Created

### Diverse Video Test Files Directory: `diverse_video_test_files/`

**Total Files:** 14 video files with different content types

### 🎨 Content Types

#### Group 1: Red to Blue Gradient (Should be detected as duplicates)
- `video1.mp4` (3.3KB) - Red to Blue Gradient
- `my_movie.mp4` (3.3KB) - Red to Blue Gradient  
- `movie_001.mp4` (3.3KB) - Red to Blue Gradient
- `video1.avi` (8.8KB) - Red to Blue Gradient
- `video1.mkv` (3.1KB) - Red to Blue Gradient
- `video1.mov` (3.3KB) - Red to Blue Gradient

#### Group 2: Different Content (Should NOT be detected as duplicates)
- `different_video.mp4` (4.2KB) - Moving Stripes Pattern
- `another_video.mp4` (11KB) - Expanding Circles Pattern
- `pattern_video.mp4` (21KB) - Checkerboard Pattern
- `text_video.mp4` (3.2KB) - Text Animation
- `noise_video.mp4` (1.3MB) - Random Noise
- `wave_video.mp4` (14KB) - Wave Noise
- `green_to_purple.mp4` (3.4KB) - Green to Purple Gradient
- `yellow_to_cyan.mp4` (3.2KB) - Yellow to Cyan Gradient

## 🔍 Expected Results

### ✅ Should be Detected as Duplicates
- All 6 files in Group 1 (Red to Blue Gradient) should be grouped together
- Similarity scores should be high (90%+) for these files
- Different formats (MP4, AVI, MKV, MOV) should be recognized as same content

### ❌ Should NOT be Detected as Duplicates
- All 8 files in Group 2 should be treated as separate, unique content
- Each file has completely different visual patterns and animations
- Similarity scores should be low (< 60%) between different content types

## 🚀 How to Test

### 1. Start the Application
```bash
cd IBM_Hackerthon
mvn spring-boot:run
```

### 2. Access the Web Interface
- Open browser: `http://localhost:8080`
- You'll see the FileGuard interface with video detection features

### 3. Run the Scan
- Enter directory path: `D:\IBM_Hackerthonn\IBM_Hackerthon\test_files\diverse_video_test_files`
- Click "Launch Scan"
- Wait for the scan to complete

### 4. Verify Results
- **Duplicate Groups**: Should show 1 group with 6 Red to Blue Gradient videos
- **Individual Files**: 8 separate files with different content
- **Similarity Scores**: High scores (90%+) for duplicates, low scores for different content

## 🎯 Video Detection Features

### Supported Video Formats
- MP4, AVI, MOV, MKV, WMV, FLV, WebM, M4V, 3GP, OGV, TS, MTS

### Detection Methods
1. **ssdeep Fuzzy Hashing**: Primary method for video content comparison
2. **Size & Entropy Analysis**: Fallback method when ssdeep fails
3. **Multi-level Thresholds**: Different similarity levels based on content

### Similarity Thresholds
- **Video Files**: 60% threshold (lenient due to compression differences)
- **High Similarity**: 90%+ (same content, different quality/format)
- **Medium Similarity**: 75%+ (same content, different compression)
- **Low Similarity**: 60%+ (same content, different format/quality)

## 📊 Test Validation

### Success Criteria
- ✅ All 6 Red to Blue Gradient videos grouped as duplicates
- ✅ All 8 different content videos treated as unique
- ✅ Similarity percentages displayed for each file
- ✅ No false positives (different content marked as duplicates)
- ✅ No false negatives (same content not detected as duplicates)

### Content Diversity
- **Gradients**: Color transitions (Red→Blue, Green→Purple, Yellow→Cyan)
- **Patterns**: Moving stripes, expanding circles, checkerboard
- **Animations**: Text movement, wave effects
- **Noise**: Random and structured noise patterns

## 🛠️ Technical Details

### File Sizes
- Smallest: 3.1KB (gradient videos)
- Largest: 1.3MB (random noise video)
- Average: ~15KB per file

### Video Properties
- Resolution: 320x240 pixels
- Duration: 3 seconds each
- Frame Rate: 10 FPS
- Format: MP4, AVI, MKV, MOV

### Creation Method
- Generated using Python with imageio library
- FFmpeg backend for proper video encoding
- Diverse algorithms for different content types

## 📝 Notes

- All video files are playable and valid
- Content is visually distinct for accurate testing
- File sizes vary based on content complexity
- Perfect for testing video duplicate detection accuracy
- Can be used to validate the system's ability to distinguish between similar and different video content

## 🔧 Troubleshooting

If you encounter issues:
1. Ensure Spring Boot application is running
2. Check that video files exist in the specified directory
3. Verify ssdeep is installed for fuzzy hashing
4. Check browser console for any JavaScript errors
5. Ensure sufficient disk space for temporary files

---

**Created by:** FileGuard Duplicate Detection System  
**Date:** 2024  
**Purpose:** Video duplicate detection testing and validation 