package com.example.appmanager.service;

import com.example.appmanager.model.ApplicationFile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class DuplicateDetectorService {
    public Map<String, List<ApplicationFile>> findDuplicates(List<ApplicationFile> files) {
        if (files == null || files.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            // Debug: Log video files found
            List<ApplicationFile> videoFiles = files.stream()
                    .filter(f -> f != null && f.getFileType() != null && isVideoFile(f.getFileType()))
                    .collect(Collectors.toList());
            System.out.println("Found " + videoFiles.size() + " video files:");
            for (ApplicationFile video : videoFiles) {
                System.out.println("  - " + video.getName() + " (" + video.getSize() + " bytes, entropy: " + video.getEntropy() + ")");
            }
            
            Map<String, List<ApplicationFile>> groupedByHash = files.stream()
                    .filter(file -> file != null && file.getHash() != null)
                    .collect(Collectors.groupingBy(ApplicationFile::getHash));
            Map<String, List<ApplicationFile>> duplicates = groupedByHash.entrySet().stream()
                    .filter(e -> e.getValue().size() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            // Set 100% similarity for exact matches (SHA-256)
            for (List<ApplicationFile> group : duplicates.values()) {
                for (ApplicationFile file : group) {
                    if (file != null) {
                        file.setSimilarityScore(100.0);
                    }
                }
            }

            // Enhanced hybrid detection for files with unique hashes
            List<ApplicationFile> nonDuplicateFiles = files.stream()
                    .filter(f -> f != null && f.getHash() != null && groupedByHash.get(f.getHash()).size() == 1)
                    .collect(Collectors.toList());
            Map<String, List<ApplicationFile>> hybridDuplicates = new HashMap<>();
            boolean[] visited = new boolean[nonDuplicateFiles.size()];
            
            // First, group video files separately for better detection
            List<ApplicationFile> nonDuplicateVideoFiles = nonDuplicateFiles.stream()
                    .filter(f -> f != null && f.getFileType() != null && isVideoFile(f.getFileType()))
                    .collect(Collectors.toList());
            
            System.out.println("Processing " + nonDuplicateVideoFiles.size() + " video files for similarity...");
            
            // Create separate groups for different types of video similarities
            List<List<ApplicationFile>> exactHashGroups = new ArrayList<>();
            List<List<ApplicationFile>> contentBasedGroups = new ArrayList<>();
            
            // First, find exact hash matches (100% similarity)
            Map<String, List<ApplicationFile>> videoHashGroups = nonDuplicateVideoFiles.stream()
                    .filter(f -> f != null && f.getHash() != null)
                    .collect(Collectors.groupingBy(ApplicationFile::getHash));
            
            for (List<ApplicationFile> hashGroup : videoHashGroups.values()) {
                if (hashGroup.size() > 1) {
                    for (ApplicationFile file : hashGroup) {
                        file.setSimilarityScore(100.0);
                    }
                    exactHashGroups.add(hashGroup);
                    
                    // Mark these files as visited
                    for (ApplicationFile file : hashGroup) {
                        int index = nonDuplicateFiles.indexOf(file);
                        if (index >= 0) {
                            visited[index] = true;
                        }
                    }
                }
            }
            
            // Then, find content-based similarities for remaining videos
            List<ApplicationFile> remainingVideos = nonDuplicateVideoFiles.stream()
                    .filter(f -> !visited[nonDuplicateFiles.indexOf(f)])
                    .collect(Collectors.toList());
            
            if (remainingVideos.size() > 1) {
                contentBasedGroups = groupSimilarVideos(remainingVideos);
            }
            
            // Combine all video groups
            List<List<ApplicationFile>> allVideoGroups = new ArrayList<>();
            allVideoGroups.addAll(exactHashGroups);
            allVideoGroups.addAll(contentBasedGroups);
            
            System.out.println("Found " + allVideoGroups.size() + " video groups:");
            for (List<ApplicationFile> group : allVideoGroups) {
                System.out.println("  Group with " + group.size() + " videos:");
                for (ApplicationFile file : group) {
                    System.out.println("    - " + file.getName() + " (similarity: " + file.getSimilarityScore() + "%)");
                }
            }
            
            for (List<ApplicationFile> group : allVideoGroups) {
                if (group.size() > 1) {
                    // Create a synthetic key for the video group
                    String key = "video-group-" + group.get(0).getName() + "-" + group.size();
                    hybridDuplicates.put(key, group);
                    
                    // Mark these files as visited
                    for (ApplicationFile file : group) {
                        int index = nonDuplicateFiles.indexOf(file);
                        if (index >= 0) {
                            visited[index] = true;
                        }
                    }
                }
            }
            
            // Then process remaining files with the original algorithm
            for (int i = 0; i < nonDuplicateFiles.size(); i++) {
                if (visited[i]) continue;
                ApplicationFile fileA = nonDuplicateFiles.get(i);
                if (fileA == null) continue;
                
                List<ApplicationFile> group = new java.util.ArrayList<>();
                group.add(fileA);
                
                for (int j = i + 1; j < nonDuplicateFiles.size(); j++) {
                    if (visited[j]) continue;
                    ApplicationFile fileB = nonDuplicateFiles.get(j);
                    if (fileB == null) continue;
                    
                    double similarity = calculateEnhancedSimilarity(fileA, fileB);
                    double threshold = getEnhancedSimilarityThreshold(fileA.getFileType());
                    
                    // For video files, focus on content similarity, not name
                    if (isVideoFile(fileA.getFileType()) && isVideoFile(fileB.getFileType())) {
                        // Use content-based similarity as primary factor
                        double contentSimilarity = calculateVideoContentBasedSimilarity(fileA, fileB);
                        double nameSimilarity = calculateVideoNameSimilarity(fileA.getName(), fileB.getName());
                        
                        // Weight content much higher than name for video files
                        similarity = (contentSimilarity * 0.8) + (nameSimilarity * 0.2);
                        
                        // Additional content-based checks for video files
                        double sizeDiff = Math.abs(fileA.getSize() - fileB.getSize()) / Math.max(fileA.getSize(), fileB.getSize());
                        double entropyDiff = Math.abs(fileA.getEntropy() - fileB.getEntropy());
                        
                        // If content is very similar, consider them duplicates regardless of name
                        if (contentSimilarity > 60.0 || (sizeDiff < 0.2 && entropyDiff < 0.3)) {
                            similarity = Math.max(similarity, 50.0);
                        }
                    }
                    
                    if (similarity > threshold) {
                        fileB.setSimilarityScore(similarity);
                        group.add(fileB);
                        visited[j] = true;
                    }
                }
                if (group.size() > 1) {
                    // Set similarity for the first file based on the group
                    double avgSimilarity = group.stream()
                        .filter(f -> f != null)
                        .mapToDouble(ApplicationFile::getSimilarityScore)
                        .average()
                        .orElse(0.0);
                    fileA.setSimilarityScore(avgSimilarity);
                    
                    // Use a synthetic key for hybrid groups
                    String key = "hybrid-" + fileA.getName() + "-" + fileA.getSize();
                    hybridDuplicates.put(key, group);
                }
            }
            
            // Second pass: Look for any remaining video files that might be similar
            List<ApplicationFile> remainingVideoFiles = nonDuplicateFiles.stream()
                    .filter(f -> f != null && !visited[nonDuplicateFiles.indexOf(f)] && 
                               f.getFileType() != null && isVideoFile(f.getFileType()))
                    .collect(Collectors.toList());
            
            if (remainingVideoFiles.size() > 1) {
                List<List<ApplicationFile>> additionalVideoGroups = groupSimilarVideos(remainingVideoFiles);
                for (List<ApplicationFile> group : additionalVideoGroups) {
                    if (group.size() > 1) {
                        // Set similarity scores for the group
                        double avgSimilarity = group.stream()
                            .mapToDouble(ApplicationFile::getSimilarityScore)
                            .average()
                            .orElse(0.0);
                        
                        for (ApplicationFile file : group) {
                            file.setSimilarityScore(avgSimilarity);
                        }
                        
                        // Create a synthetic key for the additional video group
                        String key = "video-group-2-" + group.get(0).getName() + "-" + group.size();
                        hybridDuplicates.put(key, group);
                    }
                }
            }
            // Merge SHA and hybrid duplicates
            duplicates.putAll(hybridDuplicates);
            return duplicates;
        } catch (Exception e) {
            System.err.println("Error in findDuplicates: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private double calculateEnhancedSimilarity(ApplicationFile a, ApplicationFile b) {
        if (a == null || b == null) {
            return 0.0;
        }
        
        // Enhanced text file similarity with multiple algorithms
        if (a.getFileType() != null && b.getFileType() != null && 
            a.getFileType().equals("txt") && b.getFileType().equals("txt")) {
            try {
                double jaccard = jaccardSimilarity(a.getPath(), b.getPath());
                double cosine = cosineSimilarity(a.getPath(), b.getPath());
                double levenshtein = levenshteinSimilarity(a.getPath(), b.getPath());
                
                // Weighted combination of multiple similarity measures
                return (jaccard * 0.4 + cosine * 0.4 + levenshtein * 0.2) * 100.0;
            } catch (Exception e) { 
                System.err.println("Error calculating enhanced text similarity: " + e.getMessage());
                return 0.0;
            }
        }
        
        // Enhanced audio file similarity with fingerprinting
        if (a.getFileType() != null && b.getFileType() != null &&
            isAudioFile(a.getFileType()) && isAudioFile(b.getFileType())) {
            // First try ssdeep comparison
            if (a.getSsdeepHash() != null && b.getSsdeepHash() != null && 
                !a.getSsdeepHash().isEmpty() && !b.getSsdeepHash().isEmpty()) {
                try {
                    int score = ssdeepCompare(a.getSsdeepHash(), b.getSsdeepHash());
                    if (score > 0) {
                        return (double) score;
                    }
                } catch (Exception e) { 
                    System.err.println("Error comparing ssdeep hashes: " + e.getMessage());
                }
            }
            
            // Enhanced audio analysis
            double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
            double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
            
            // Audio-specific similarity detection
            if (sizeDiff < 0.05 && entropyDiff < 0.05) {
                return 95.0; // Very high similarity for audio files
            } else if (sizeDiff < 0.1 && entropyDiff < 0.1) {
                return 85.0; // High similarity
            } else if (sizeDiff < 0.15 && entropyDiff < 0.15) {
                return 75.0; // Medium similarity
            } else if (sizeDiff < 0.25 && entropyDiff < 0.2) {
                return 65.0; // Low similarity
            }
        }
        
        // Enhanced video file similarity with advanced analysis
        if (a.getFileType() != null && b.getFileType() != null &&
            isVideoFile(a.getFileType()) && isVideoFile(b.getFileType())) {
            // First try ssdeep comparison for video files (content-based)
            if (a.getSsdeepHash() != null && b.getSsdeepHash() != null && 
                !a.getSsdeepHash().isEmpty() && !b.getSsdeepHash().isEmpty()) {
                try {
                    int score = ssdeepCompare(a.getSsdeepHash(), b.getSsdeepHash());
                    if (score > 0) {
                        return (double) score; // ssdeep provides content-based similarity
                    }
                } catch (Exception e) { 
                    System.err.println("Error comparing ssdeep hashes for video: " + e.getMessage());
                }
            }
            
            // Use enhanced video content similarity (content-focused)
            double contentSimilarity = calculateVideoContentBasedSimilarity(a, b);
            if (contentSimilarity > 0) {
                return contentSimilarity;
            }
            
            // Cross-format video similarity detection
            double crossFormatSimilarity = calculateCrossFormatVideoSimilarity(a, b);
            if (crossFormatSimilarity > 0) {
                return crossFormatSimilarity;
            }
            
            // Enhanced video-specific similarity detection with content-based algorithms
            double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
            double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
            
            // More aggressive content-based similarity thresholds for video files
            if (sizeDiff < 0.05 && entropyDiff < 0.05) {
                return 98.0; // Very high content similarity
            } else if (sizeDiff < 0.1 && entropyDiff < 0.1) {
                return 95.0; // High content similarity
            } else if (sizeDiff < 0.2 && entropyDiff < 0.15) {
                return 90.0; // Medium content similarity
            } else if (sizeDiff < 0.4 && entropyDiff < 0.25) {
                return 85.0; // Low content similarity
            } else if (sizeDiff < 0.6 && entropyDiff < 0.3) {
                return 75.0; // Very low content similarity but still possible duplicate
            } else if (sizeDiff < 0.8 && entropyDiff < 0.4) {
                return 65.0; // Extremely lenient for video content similarity
            }
            
            // Additional content-based checks for video files
            double perceptualSimilarity = calculatePerceptualSimilarity(a, b);
            if (perceptualSimilarity > 70.0) {
                return perceptualSimilarity;
            }
            
            // Structural similarity check
            double structuralSimilarity = analyzeVideoStructure(a, b);
            if (structuralSimilarity > 60.0) {
                return structuralSimilarity;
            }
            
            // Content pattern analysis
            double contentPatternSimilarity = analyzeContentPatterns(a, b);
            if (contentPatternSimilarity > 50.0) {
                return contentPatternSimilarity;
            }
            
            // Only use name similarity as a last resort for content-based detection
            if (a.getName() != null && b.getName() != null) {
                String nameA = a.getName().toLowerCase();
                String nameB = b.getName().toLowerCase();
                
                // Only consider name if content analysis shows some similarity
                if (sizeDiff < 0.5 || entropyDiff < 0.5) {
                    double nameSimilarity = calculateVideoNameSimilarity(nameA, nameB);
                    // Use name as a minor factor only when content is somewhat similar
                    return Math.max(40.0, nameSimilarity * 0.3);
                }
            }
        }
        
        // Enhanced binary file similarity with fuzzy matching
        if (a.getFileType() != null && b.getFileType() != null &&
            !a.getFileType().equals("txt") && !b.getFileType().equals("txt") && 
            !isAudioFile(a.getFileType()) && !isAudioFile(b.getFileType()) &&
            !isVideoFile(a.getFileType()) && !isVideoFile(b.getFileType())) {
            // Fuzzy binary comparison using ssdeep
            if (a.getSsdeepHash() != null && b.getSsdeepHash() != null && 
                !a.getSsdeepHash().isEmpty() && !b.getSsdeepHash().isEmpty()) {
                try {
                    int score = ssdeepCompare(a.getSsdeepHash(), b.getSsdeepHash());
                    return (double) score;
                } catch (Exception e) { 
                    System.err.println("Error comparing ssdeep hashes: " + e.getMessage());
                    return 0.0;
                }
            }
            
            // Enhanced binary similarity based on size, type, and entropy
            if (!a.getFileType().equals(b.getFileType())) return 0.0;
            
            double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
            double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
            
            if (sizeDiff < 0.01 && entropyDiff < 0.01) {
                return 98.0; // Very high similarity
            } else if (sizeDiff < 0.05 && entropyDiff < 0.05) {
                return 90.0; // High similarity
            } else if (sizeDiff < 0.1 && entropyDiff < 0.1) {
                return 80.0; // Medium similarity
            }
        }
        
        // General fallback with enhanced analysis
        if (a.getFileType() == null || b.getFileType() == null || 
            !a.getFileType().equals(b.getFileType())) return 0.0;
        if (a.getSize() != b.getSize()) return 0.0;
        double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
        if (entropyDiff < 0.005) return 98.0; // Very high similarity if entropy matches very closely
        if (entropyDiff < 0.01) return 95.0; // High similarity
        return Math.max(0.0, 100.0 - (entropyDiff * 1000)); // Scale entropy difference
    }

    private double calculateVideoNameSimilarity(String nameA, String nameB) {
        if (nameA == null || nameB == null) return 0.0;
        
        // Remove common video extensions
        nameA = nameA.replaceAll("\\.(mp4|avi|mov|mkv|wmv|flv|webm|m4v|3gp|ogv|ts|mts|m2ts|vob|asf|divx|xvid|h264|h265|hevc|avc|mpg|mpeg|rm|rmvb|swf|f4v)$", "");
        nameB = nameB.replaceAll("\\.(mp4|avi|mov|mkv|wmv|flv|webm|m4v|3gp|ogv|ts|mts|m2ts|vob|asf|divx|xvid|h264|h265|hevc|avc|mpg|mpeg|rm|rmvb|swf|f4v)$", "");
        
        // Remove common separators and special characters
        nameA = nameA.replaceAll("[._\\-\\s]+", " ").trim();
        nameB = nameB.replaceAll("[._\\-\\s]+", " ").trim();
        
        if (nameA.equals(nameB)) {
            return 95.0; // Exact name match
        }
        
        // Check for common video naming patterns with more patterns
        String[] commonPatterns = {"video", "movie", "film", "clip", "recording", "camera", "screen", "vid", "mov"};
        boolean hasCommonPatternA = false;
        boolean hasCommonPatternB = false;
        
        for (String pattern : commonPatterns) {
            if (nameA.toLowerCase().contains(pattern)) hasCommonPatternA = true;
            if (nameB.toLowerCase().contains(pattern)) hasCommonPatternB = true;
        }
        
        // If both have common patterns, give bonus similarity
        double patternBonus = (hasCommonPatternA && hasCommonPatternB) ? 30.0 : 0.0;
        
        // Check for numeric patterns (like video1, video2, etc.)
        if (nameA.matches(".*\\d+.*") && nameB.matches(".*\\d+.*")) {
            patternBonus += 15.0; // Bonus for numeric patterns
        }
        
        // Calculate Jaccard similarity for words
        String[] wordsA = nameA.split("\\s+");
        String[] wordsB = nameB.split("\\s+");
        
        java.util.Set<String> setA = new java.util.HashSet<>(java.util.Arrays.asList(wordsA));
        java.util.Set<String> setB = new java.util.HashSet<>(java.util.Arrays.asList(wordsB));
        
        java.util.Set<String> intersection = new java.util.HashSet<>(setA);
        intersection.retainAll(setB);
        java.util.Set<String> union = new java.util.HashSet<>(setA);
        union.addAll(setB);
        
        if (union.isEmpty()) return patternBonus;
        
        double jaccardSimilarity = (double) intersection.size() / union.size() * 100.0;
        double finalSimilarity = jaccardSimilarity + patternBonus;
        
        // Additional bonus for partial matches
        for (String wordA : wordsA) {
            for (String wordB : wordsB) {
                if (wordA.length() > 2 && wordB.length() > 2 && 
                    (wordA.contains(wordB) || wordB.contains(wordA))) {
                    finalSimilarity += 10.0; // Bonus for partial word matches
                    break;
                }
            }
        }
        
        return Math.min(95.0, finalSimilarity);
    }
    
    private boolean isAudioFile(String fileType) {
        if (fileType == null) return false;
        return fileType.equals("wav") || fileType.equals("mp3") || fileType.equals("flac") || 
               fileType.equals("aac") || fileType.equals("ogg") || fileType.equals("m4a") ||
               fileType.equals("wma") || fileType.equals("aiff") || fileType.equals("opus") ||
               fileType.equals("wav") || fileType.equals("m3u") || fileType.equals("m3u8") ||
               fileType.equals("pls");
    }
    
    private boolean isVideoFile(String fileType) {
        if (fileType == null) return false;
        return fileType.equals("mp4") || fileType.equals("avi") || fileType.equals("mov") || 
               fileType.equals("mkv") || fileType.equals("wmv") || fileType.equals("flv") ||
               fileType.equals("webm") || fileType.equals("m4v") || fileType.equals("3gp") ||
               fileType.equals("ogv") || fileType.equals("ts") || fileType.equals("mts") ||
               fileType.equals("m2ts") || fileType.equals("vob") || fileType.equals("asf") ||
               fileType.equals("divx") || fileType.equals("xvid") || fileType.equals("h264") ||
               fileType.equals("h265") || fileType.equals("hevc") || fileType.equals("avc") ||
               fileType.equals("mpg") || fileType.equals("mpeg") || fileType.equals("rm") ||
               fileType.equals("rmvb") || fileType.equals("swf") || fileType.equals("f4v");
    }

    private double getEnhancedSimilarityThreshold(String fileType) {
        if (fileType == null) return 80.0;
        if (fileType.equals("txt")) return 75.0; // 75% for text files (more lenient)
        if (isAudioFile(fileType)) return 65.0; // 65% for audio files (more lenient)
        if (isVideoFile(fileType)) return 25.0; // 25% for video files (extremely aggressive for content-based detection)
        return 80.0; // 80% for other binary files
    }

    // Enhanced similarity: size, type, and entropy must match closely
    private boolean isSimilar(ApplicationFile a, ApplicationFile b) {
        if (a.getFileType().equals("txt") && b.getFileType().equals("txt")) {
            try {
                double jaccard = jaccardSimilarity(a.getPath(), b.getPath());
                if (jaccard > 0.75) return true; // threshold for near-duplicate
            } catch (Exception e) { /* ignore */ }
        }
        if (!a.getFileType().equals("txt") && !b.getFileType().equals("txt")) {
            // Fuzzy binary comparison using ssdeep
            if (a.getSsdeepHash() != null && b.getSsdeepHash() != null && !a.getSsdeepHash().isEmpty() && !b.getSsdeepHash().isEmpty()) {
                try {
                    int score = ssdeepCompare(a.getSsdeepHash(), b.getSsdeepHash());
                    if (score > 85) return true; // threshold for near-duplicate binaries
                } catch (Exception e) { /* ignore */ }
            }
        }
        if (a.getSize() != b.getSize()) return false;
        if (!a.getFileType().equals(b.getFileType())) return false;
        return Math.abs(a.getEntropy() - b.getEntropy()) < 0.01;
    }

    private double jaccardSimilarity(String pathA, String pathB) throws java.io.IOException {
        if (pathA == null || pathB == null) return 0.0;
        
        java.util.Set<String> setA = new java.util.HashSet<>(java.util.Arrays.asList(
            new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathA)))
                .toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+")
        ));
        java.util.Set<String> setB = new java.util.HashSet<>(java.util.Arrays.asList(
            new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathB)))
                .toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+")
        ));
        java.util.Set<String> intersection = new java.util.HashSet<>(setA);
        intersection.retainAll(setB);
        java.util.Set<String> union = new java.util.HashSet<>(setA);
        union.addAll(setB);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private double cosineSimilarity(String pathA, String pathB) throws java.io.IOException {
        if (pathA == null || pathB == null) return 0.0;
        
        String contentA = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathA))).toLowerCase();
        String contentB = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathB))).toLowerCase();
        
        java.util.Map<String, Integer> vectorA = new java.util.HashMap<>();
        java.util.Map<String, Integer> vectorB = new java.util.HashMap<>();
        
        // Create word frequency vectors
        for (String word : contentA.replaceAll("[^a-z0-9 ]", " ").split("\\s+")) {
            if (!word.isEmpty()) {
                vectorA.put(word, vectorA.getOrDefault(word, 0) + 1);
            }
        }
        
        for (String word : contentB.replaceAll("[^a-z0-9 ]", " ").split("\\s+")) {
            if (!word.isEmpty()) {
                vectorB.put(word, vectorB.getOrDefault(word, 0) + 1);
            }
        }
        
        // Calculate cosine similarity
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        java.util.Set<String> allWords = new java.util.HashSet<>(vectorA.keySet());
        allWords.addAll(vectorB.keySet());
        
        for (String word : allWords) {
            int freqA = vectorA.getOrDefault(word, 0);
            int freqB = vectorB.getOrDefault(word, 0);
            dotProduct += freqA * freqB;
            normA += freqA * freqA;
            normB += freqB * freqB;
        }
        
        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    private double levenshteinSimilarity(String pathA, String pathB) throws java.io.IOException {
        if (pathA == null || pathB == null) return 0.0;
        
        String contentA = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathA)));
        String contentB = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathB)));
        
        int distance = levenshteinDistance(contentA, contentB);
        int maxLength = Math.max(contentA.length(), contentB.length());
        
        return maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);
    }
    
    private int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    private int ssdeepCompare(String hashA, String hashB) throws java.io.IOException, InterruptedException {
        if (hashA == null || hashB == null || hashA.isEmpty() || hashB.isEmpty()) return 0;
        
        try {
            // Use ssdeep -v for comparison
            ProcessBuilder pb = new ProcessBuilder("ssdeep", "-v", hashA, hashB);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            int score = 0;
            while ((line = reader.readLine()) != null) {
                if (line.contains(":")) {
                    // Expected format: "hash1:hash2:score"
                    String[] parts = line.split(":");
                    if (parts.length == 3) {
                        try {
                            score = Integer.parseInt(parts[2].trim());
                            break;
                        } catch (NumberFormatException e) {
                            // Try alternative parsing
                            String scoreStr = parts[2].replaceAll("[^0-9]", "");
                            if (!scoreStr.isEmpty()) {
                                score = Integer.parseInt(scoreStr);
                                break;
                            }
                        }
                    }
                }
            }
            process.waitFor();
            return score;
        } catch (Exception e) {
            System.err.println("Error in ssdeep comparison: " + e.getMessage());
            return 0;
        }
    }

    private double calculateVideoContentSimilarity(ApplicationFile a, ApplicationFile b) {
        if (a == null || b == null) return 0.0;
        
        // Primary focus on content-based similarity, not name
        double contentSimilarity = calculateVideoContentBasedSimilarity(a, b);
        
        // Only use name similarity as a secondary factor, not primary
        double nameSimilarity = 0.0;
        if (a.getName() != null && b.getName() != null) {
            nameSimilarity = calculateVideoNameSimilarity(a.getName(), b.getName());
        }
        
        // Weight content much higher than name (80% content, 20% name)
        double contentWeight = 0.8;
        double nameWeight = 0.2;
        
        double finalSimilarity = (contentSimilarity * contentWeight) + (nameSimilarity * nameWeight);
        
        return Math.min(100.0, finalSimilarity);
    }
    
    private double calculateVideoContentBasedSimilarity(ApplicationFile a, ApplicationFile b) {
        if (a == null || b == null) return 0.0;
        
        // Multi-level content analysis for video files
        double sizeSimilarity = calculateSizeSimilarity(a, b);
        double entropySimilarity = calculateEntropySimilarity(a, b);
        double ssdeepSimilarity = calculateSsdeepSimilarity(a, b);
        double perceptualSimilarity = calculatePerceptualSimilarity(a, b);
        double structuralSimilarity = analyzeVideoStructure(a, b);
        double contentPatternSimilarity = analyzeContentPatterns(a, b);
        
        // More aggressive weighting for content-based detection
        double sizeWeight = 0.15;
        double entropyWeight = 0.20;
        double ssdeepWeight = 0.25;
        double perceptualWeight = 0.20;
        double structuralWeight = 0.10;
        double patternWeight = 0.10;
        
        double totalSimilarity = (sizeSimilarity * sizeWeight) + 
                               (entropySimilarity * entropyWeight) + 
                               (ssdeepSimilarity * ssdeepWeight) + 
                               (perceptualSimilarity * perceptualWeight) +
                               (structuralSimilarity * structuralWeight) +
                               (contentPatternSimilarity * patternWeight);
        
        // Additional content-based bonuses for video files
        double contentBonus = calculateContentBonus(a, b);
        
        return Math.min(100.0, totalSimilarity + contentBonus);
    }
    
    private double calculatePerceptualSimilarity(ApplicationFile a, ApplicationFile b) {
        // Simulate perceptual hashing for video content
        // This would normally analyze video frames, but we'll use file characteristics
        double sizeRatio = Math.min(a.getSize(), b.getSize()) / Math.max(a.getSize(), b.getSize());
        double entropyRatio = Math.min(a.getEntropy(), b.getEntropy()) / Math.max(a.getEntropy(), b.getEntropy());
        
        // Perceptual similarity based on content characteristics
        if (sizeRatio > 0.95 && entropyRatio > 0.95) return 100.0;
        if (sizeRatio > 0.90 && entropyRatio > 0.90) return 95.0;
        if (sizeRatio > 0.80 && entropyRatio > 0.80) return 90.0;
        if (sizeRatio > 0.70 && entropyRatio > 0.70) return 85.0;
        if (sizeRatio > 0.60 && entropyRatio > 0.60) return 75.0;
        if (sizeRatio > 0.50 && entropyRatio > 0.50) return 65.0;
        return 0.0;
    }
    
    private double analyzeVideoStructure(ApplicationFile a, ApplicationFile b) {
        // Analyze video file structure and metadata patterns
        double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
        double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
        
        // Video structure analysis (similar videos have similar structural patterns)
        if (sizeDiff < 0.05 && entropyDiff < 0.05) return 100.0;
        if (sizeDiff < 0.10 && entropyDiff < 0.10) return 95.0;
        if (sizeDiff < 0.20 && entropyDiff < 0.15) return 90.0;
        if (sizeDiff < 0.30 && entropyDiff < 0.25) return 80.0;
        if (sizeDiff < 0.40 && entropyDiff < 0.35) return 70.0;
        if (sizeDiff < 0.50 && entropyDiff < 0.45) return 60.0;
        return 0.0;
    }
    
    private double calculateContentBonus(ApplicationFile a, ApplicationFile b) {
        double bonus = 0.0;
        
        // Bonus for similar file types
        if (a.getFileType() != null && b.getFileType() != null && 
            a.getFileType().equals(b.getFileType())) {
            bonus += 10.0;
        }
        
        // Bonus for similar size ranges (same content, different compression)
        double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
        if (sizeDiff < 0.1) bonus += 15.0;
        else if (sizeDiff < 0.2) bonus += 10.0;
        else if (sizeDiff < 0.3) bonus += 5.0;
        
        // Bonus for similar entropy (content complexity)
        double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
        if (entropyDiff < 0.1) bonus += 15.0;
        else if (entropyDiff < 0.2) bonus += 10.0;
        else if (entropyDiff < 0.3) bonus += 5.0;
        
        // Bonus for content-based characteristics
        if (sizeDiff < 0.4 && entropyDiff < 0.4) {
            bonus += 20.0; // High bonus for similar content characteristics
        }
        
        return bonus;
    }
    
    private double calculateSizeSimilarity(ApplicationFile a, ApplicationFile b) {
        double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
        
        // More lenient size comparison for videos (same content can have different sizes due to compression)
        if (sizeDiff < 0.05) return 100.0; // Very similar
        if (sizeDiff < 0.1) return 95.0;   // Similar
        if (sizeDiff < 0.2) return 85.0;   // Moderately similar
        if (sizeDiff < 0.4) return 70.0;   // Somewhat similar
        if (sizeDiff < 0.6) return 50.0;   // Weakly similar
        return 0.0; // Too different
    }
    
    private double calculateEntropySimilarity(ApplicationFile a, ApplicationFile b) {
        double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
        
        // Entropy measures content complexity - similar videos should have similar entropy
        if (entropyDiff < 0.05) return 100.0; // Very similar content complexity
        if (entropyDiff < 0.1) return 95.0;   // Similar content complexity
        if (entropyDiff < 0.2) return 85.0;   // Moderately similar
        if (entropyDiff < 0.4) return 70.0;   // Somewhat similar
        if (entropyDiff < 0.6) return 50.0;   // Weakly similar
        return 0.0; // Too different
    }
    
    private double calculateSsdeepSimilarity(ApplicationFile a, ApplicationFile b) {
        if (a.getSsdeepHash() == null || b.getSsdeepHash() == null || 
            a.getSsdeepHash().isEmpty() || b.getSsdeepHash().isEmpty()) {
            return 0.0; // No ssdeep hash available
        }
        
        try {
            int score = ssdeepCompare(a.getSsdeepHash(), b.getSsdeepHash());
            return (double) score; // ssdeep score is already 0-100
        } catch (Exception e) {
            System.err.println("Error in ssdeep comparison: " + e.getMessage());
            return 0.0;
        }
    }

    private List<List<ApplicationFile>> groupSimilarVideos(List<ApplicationFile> videoFiles) {
        List<List<ApplicationFile>> groups = new ArrayList<>();
        boolean[] visited = new boolean[videoFiles.size()];
        
        // First pass: Group videos with exact same base names (e.g., video1.mp4, video1.avi, video1.mov)
        for (int i = 0; i < videoFiles.size(); i++) {
            if (visited[i]) continue;
            
            ApplicationFile current = videoFiles.get(i);
            List<ApplicationFile> group = new ArrayList<>();
            group.add(current);
            visited[i] = true;
            
            // Extract base name without extension
            String baseNameA = current.getName() != null ? 
                current.getName().replaceAll("\\.(mp4|avi|mov|mkv|wmv|flv|webm|m4v|3gp|ogv|ts|mts|m2ts|vob|asf|divx|xvid|h264|h265|hevc|avc|mpg|mpeg|rm|rmvb|swf|f4v)$", "") : "";
            
            // Find videos with same base name
            for (int j = i + 1; j < videoFiles.size(); j++) {
                if (visited[j]) continue;
                
                ApplicationFile candidate = videoFiles.get(j);
                String baseNameB = candidate.getName() != null ? 
                    candidate.getName().replaceAll("\\.(mp4|avi|mov|mkv|wmv|flv|webm|m4v|3gp|ogv|ts|mts|m2ts|vob|asf|divx|xvid|h264|h265|hevc|avc|mpg|mpeg|rm|rmvb|swf|f4v)$", "") : "";
                
                if (baseNameA.equals(baseNameB)) {
                    group.add(candidate);
                    visited[j] = true;
                    candidate.setSimilarityScore(85.0); // High similarity for same base name
                }
            }
            
            if (group.size() > 1) {
                // Set high similarity for same base name group
                for (ApplicationFile file : group) {
                    file.setSimilarityScore(85.0);
                }
                groups.add(group);
            }
        }
        
        // Second pass: Group videos with similar content characteristics
        for (int i = 0; i < videoFiles.size(); i++) {
            if (visited[i]) continue;
            
            ApplicationFile current = videoFiles.get(i);
            List<ApplicationFile> group = new ArrayList<>();
            group.add(current);
            visited[i] = true;
            
            for (int j = i + 1; j < videoFiles.size(); j++) {
                if (visited[j]) continue;
                
                ApplicationFile candidate = videoFiles.get(j);
                
                // Multi-level content analysis
                double contentSimilarity = calculateVideoContentBasedSimilarity(current, candidate);
                double crossFormatSimilarity = calculateCrossFormatVideoSimilarity(current, candidate);
                double sizeDiff = Math.abs(current.getSize() - candidate.getSize()) / Math.max(current.getSize(), candidate.getSize());
                double entropyDiff = Math.abs(current.getEntropy() - candidate.getEntropy());
                
                // Content-based grouping criteria
                boolean shouldGroup = false;
                
                // High content similarity
                if (contentSimilarity > 30.0) {
                    shouldGroup = true;
                }
                // Cross-format similarity
                else if (crossFormatSimilarity > 25.0) {
                    shouldGroup = true;
                }
                // Similar content characteristics
                else if (sizeDiff < 0.3 && entropyDiff < 0.4) {
                    shouldGroup = true;
                }
                // Same file type with similar characteristics
                else if (current.getFileType() != null && candidate.getFileType() != null &&
                         current.getFileType().equals(candidate.getFileType()) &&
                         sizeDiff < 0.5 && entropyDiff < 0.5) {
                    shouldGroup = true;
                }
                
                if (shouldGroup) {
                    group.add(candidate);
                    visited[j] = true;
                    double finalSimilarity = Math.max(contentSimilarity, crossFormatSimilarity);
                    candidate.setSimilarityScore(finalSimilarity);
                }
            }
            
            if (group.size() > 1) {
                // Calculate average similarity for the group
                double avgSimilarity = group.stream()
                    .mapToDouble(ApplicationFile::getSimilarityScore)
                    .average()
                    .orElse(0.0);
                
                // Set similarity for all files in the group
                for (ApplicationFile file : group) {
                    file.setSimilarityScore(avgSimilarity);
                }
                
                groups.add(group);
            }
        }
        
        // Third pass: Very lenient grouping for remaining videos
        List<ApplicationFile> remainingVideos = new ArrayList<>();
        for (int i = 0; i < videoFiles.size(); i++) {
            if (!visited[i]) {
                remainingVideos.add(videoFiles.get(i));
            }
        }
        
        if (remainingVideos.size() > 1) {
            List<List<ApplicationFile>> additionalGroups = performSecondPassGrouping(remainingVideos);
            groups.addAll(additionalGroups);
        }
        
        return groups;
    }
    
    private double calculateCrossFormatVideoSimilarity(ApplicationFile a, ApplicationFile b) {
        if (a == null || b == null) return 0.0;
        
        // Check if files have similar names but different formats
        String nameA = a.getName() != null ? a.getName().toLowerCase() : "";
        String nameB = b.getName() != null ? b.getName().toLowerCase() : "";
        
        // Remove extensions for comparison
        nameA = nameA.replaceAll("\\.(mp4|avi|mov|mkv|wmv|flv|webm|m4v|3gp|ogv|ts|mts|m2ts|vob|asf|divx|xvid|h264|h265|hevc|avc|mpg|mpeg|rm|rmvb|swf|f4v)$", "");
        nameB = nameB.replaceAll("\\.(mp4|avi|mov|mkv|wmv|flv|webm|m4v|3gp|ogv|ts|mts|m2ts|vob|asf|divx|xvid|h264|h265|hevc|avc|mpg|mpeg|rm|rmvb|swf|f4v)$", "");
        
        // Check for similar base names (e.g., video1.mp4, video1.avi, video1.mov)
        if (nameA.equals(nameB)) {
            // Same base name, different formats - high similarity
            double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
            double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
            
            if (sizeDiff < 0.3 && entropyDiff < 0.4) {
                return 85.0; // High similarity for same content, different format
            } else if (sizeDiff < 0.5 && entropyDiff < 0.5) {
                return 75.0; // Medium similarity
            } else if (sizeDiff < 0.7 && entropyDiff < 0.6) {
                return 65.0; // Low similarity
            }
        }
        
        // Check for partial name matches (e.g., movie_001.mp4, my_movie.mp4)
        if (nameA.contains(nameB) || nameB.contains(nameA)) {
            double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
            double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
            
            if (sizeDiff < 0.4 && entropyDiff < 0.4) {
                return 70.0; // High similarity for related content
            } else if (sizeDiff < 0.6 && entropyDiff < 0.5) {
                return 60.0; // Medium similarity
            }
        }
        
        // Check for numeric patterns (e.g., video1, video2, etc.)
        if (nameA.matches(".*\\d+.*") && nameB.matches(".*\\d+.*")) {
            String baseA = nameA.replaceAll("\\d+", "");
            String baseB = nameB.replaceAll("\\d+", "");
            
            if (baseA.equals(baseB)) {
                double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
                double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
                
                if (sizeDiff < 0.5 && entropyDiff < 0.5) {
                    return 65.0; // Similarity for numbered series
                }
            }
        }
        
        return 0.0;
    }
    
    private List<List<ApplicationFile>> performSecondPassGrouping(List<ApplicationFile> remainingVideos) {
        List<List<ApplicationFile>> additionalGroups = new ArrayList<>();
        boolean[] visited = new boolean[remainingVideos.size()];
        
        for (int i = 0; i < remainingVideos.size(); i++) {
            if (visited[i]) continue;
            
            ApplicationFile current = remainingVideos.get(i);
            List<ApplicationFile> group = new ArrayList<>();
            group.add(current);
            visited[i] = true;
            
            for (int j = i + 1; j < remainingVideos.size(); j++) {
                if (visited[j]) continue;
                
                ApplicationFile candidate = remainingVideos.get(j);
                
                // More lenient criteria for second pass
                double sizeDiff = Math.abs(current.getSize() - candidate.getSize()) / Math.max(current.getSize(), candidate.getSize());
                double entropyDiff = Math.abs(current.getEntropy() - candidate.getEntropy());
                double crossFormatSimilarity = calculateCrossFormatVideoSimilarity(current, candidate);
                double nameSimilarity = calculateVideoNameSimilarity(current.getName(), candidate.getName());
                
                // Very lenient grouping for remaining videos
                boolean shouldGroup = false;
                
                // Cross-format similarity
                if (crossFormatSimilarity > 15.0) {
                    shouldGroup = true;
                }
                // Size and entropy similarity
                else if (sizeDiff < 0.7 && entropyDiff < 0.7) {
                    shouldGroup = true;
                }
                // Name similarity with content characteristics
                else if (nameSimilarity > 25.0 && (sizeDiff < 0.8 || entropyDiff < 0.8)) {
                    shouldGroup = true;
                }
                // Perceptual similarity
                else if (sizeDiff < 0.6 && entropyDiff < 0.6) {
                    double perceptualScore = calculatePerceptualSimilarity(current, candidate);
                    if (perceptualScore > 40.0) {
                        shouldGroup = true;
                    }
                }
                
                if (shouldGroup) {
                    group.add(candidate);
                    visited[j] = true;
                    double finalSimilarity = Math.max(crossFormatSimilarity, 50.0);
                    candidate.setSimilarityScore(finalSimilarity);
                }
            }
            
            if (group.size() > 1) {
                for (ApplicationFile file : group) {
                    file.setSimilarityScore(50.0);
                }
                additionalGroups.add(group);
            }
        }
        
        return additionalGroups;
    }

    private double analyzeVideoContentDepth(ApplicationFile a, ApplicationFile b) {
        if (a == null || b == null) return 0.0;
        
        // Analyze file structure and content patterns
        double structureSimilarity = analyzeFileStructure(a, b);
        double patternSimilarity = analyzeContentPatterns(a, b);
        double metadataSimilarity = analyzeMetadata(a, b);
        
        // Weight the analysis factors
        double structureWeight = 0.4;
        double patternWeight = 0.4;
        double metadataWeight = 0.2;
        
        return (structureSimilarity * structureWeight) + 
               (patternSimilarity * patternWeight) + 
               (metadataSimilarity * metadataWeight);
    }
    
    private double analyzeFileStructure(ApplicationFile a, ApplicationFile b) {
        // Analyze file structure characteristics
        double sizeRatio = Math.min(a.getSize(), b.getSize()) / Math.max(a.getSize(), b.getSize());
        double entropyRatio = Math.min(a.getEntropy(), b.getEntropy()) / Math.max(a.getEntropy(), b.getEntropy());
        
        // Similar structure indicates similar content
        if (sizeRatio > 0.9 && entropyRatio > 0.9) return 100.0;
        if (sizeRatio > 0.8 && entropyRatio > 0.8) return 95.0;
        if (sizeRatio > 0.7 && entropyRatio > 0.7) return 85.0;
        if (sizeRatio > 0.6 && entropyRatio > 0.6) return 70.0;
        if (sizeRatio > 0.5 && entropyRatio > 0.5) return 50.0;
        return 0.0;
    }
    
    private double analyzeContentPatterns(ApplicationFile a, ApplicationFile b) {
        // Analyze content patterns using entropy and size characteristics
        double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
        double entropyDiff = Math.abs(a.getEntropy() - b.getEntropy());
        
        // Similar content patterns
        if (sizeDiff < 0.1 && entropyDiff < 0.1) return 100.0;
        if (sizeDiff < 0.2 && entropyDiff < 0.2) return 90.0;
        if (sizeDiff < 0.3 && entropyDiff < 0.3) return 75.0;
        if (sizeDiff < 0.4 && entropyDiff < 0.4) return 60.0;
        return 0.0;
    }
    
    private double analyzeMetadata(ApplicationFile a, ApplicationFile b) {
        // Analyze metadata similarities (file type, size patterns, etc.)
        if (!a.getFileType().equals(b.getFileType())) return 0.0;
        
        // Similar file types with similar characteristics
        double sizeDiff = Math.abs(a.getSize() - b.getSize()) / Math.max(a.getSize(), b.getSize());
        if (sizeDiff < 0.2) return 80.0;
        if (sizeDiff < 0.4) return 60.0;
        if (sizeDiff < 0.6) return 40.0;
        return 0.0;
    }
}