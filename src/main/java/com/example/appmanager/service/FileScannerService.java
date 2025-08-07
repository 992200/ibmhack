package com.example.appmanager.service;

import com.example.appmanager.model.ApplicationFile;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class FileScannerService {
    public List<ApplicationFile> scanDirectory(String directoryPath) throws IOException, NoSuchAlgorithmException {
        List<ApplicationFile> applicationFiles = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(directoryPath), null, true);
        for (File file : files) {
            ApplicationFile appFile = new ApplicationFile();
            appFile.setName(file.getName());
            appFile.setPath(file.getAbsolutePath());
            appFile.setSize(file.length());
            appFile.setFileType(getEnhancedFileExtension(file));
            if (appFile.getFileType().equals("txt")) {
                appFile.setHash(computeNormalizedTextHash(file));
            } else {
                appFile.setHash(computeSHA256(file));
            }
            if (!appFile.getFileType().equals("txt")) {
                appFile.setSsdeepHash(computeEnhancedSsdeepHash(file));
            }
            appFile.setEntropy(calculateEnhancedEntropy(file));
            applicationFiles.add(appFile);
        }
        return applicationFiles;
    }

    private String computeSHA256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String getEnhancedFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return "unknown";
        
        String extension = name.substring(lastDot + 1).toLowerCase();
        
        // Enhanced file type detection
        if (isAudioFile(extension)) return extension;
        if (isVideoFile(extension)) return extension;
        if (isImageFile(extension)) return extension;
        if (isDocumentFile(extension)) return extension;
        if (isArchiveFile(extension)) return extension;
        if (isApplicationFile(extension)) return extension;
        
        return extension;
    }
    
    private boolean isAudioFile(String extension) {
        return extension.equals("wav") || extension.equals("mp3") || extension.equals("flac") || 
               extension.equals("aac") || extension.equals("ogg") || extension.equals("m4a") ||
               extension.equals("wma") || extension.equals("aiff") || extension.equals("opus") ||
               extension.equals("m3u") || extension.equals("m3u8") || extension.equals("pls");
    }
    
    private boolean isVideoFile(String extension) {
        return extension.equals("mp4") || extension.equals("avi") || extension.equals("mov") || 
               extension.equals("mkv") || extension.equals("wmv") || extension.equals("flv") ||
               extension.equals("webm") || extension.equals("m4v") || extension.equals("3gp") ||
               extension.equals("ogv") || extension.equals("ts") || extension.equals("mts") ||
               extension.equals("m2ts") || extension.equals("vob") || extension.equals("asf") ||
               extension.equals("divx") || extension.equals("xvid") || extension.equals("h264") ||
               extension.equals("h265") || extension.equals("hevc") || extension.equals("avc") ||
               extension.equals("mpg") || extension.equals("mpeg") || extension.equals("rm") ||
               extension.equals("rmvb") || extension.equals("swf") || extension.equals("f4v");
    }
    
    private boolean isImageFile(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || 
               extension.equals("gif") || extension.equals("bmp") || extension.equals("tiff") ||
               extension.equals("webp") || extension.equals("svg") || extension.equals("ico");
    }
    
    private boolean isDocumentFile(String extension) {
        return extension.equals("pdf") || extension.equals("doc") || extension.equals("docx") || 
               extension.equals("txt") || extension.equals("rtf") || extension.equals("odt") ||
               extension.equals("xls") || extension.equals("xlsx") || extension.equals("ppt") ||
               extension.equals("pptx") || extension.equals("csv");
    }
    
    private boolean isArchiveFile(String extension) {
        return extension.equals("zip") || extension.equals("rar") || extension.equals("7z") || 
               extension.equals("tar") || extension.equals("gz") || extension.equals("bz2") ||
               extension.equals("xz") || extension.equals("cab") || extension.equals("iso");
    }
    
    private boolean isApplicationFile(String extension) {
        return extension.equals("exe") || extension.equals("msi") || extension.equals("apk") || 
               extension.equals("jar") || extension.equals("dmg") || extension.equals("deb") ||
               extension.equals("rpm") || extension.equals("app") || extension.equals("ipa");
    }

    private double calculateEnhancedEntropy(File file) throws IOException {
        int[] freq = new int[256];
        int total = 0;
        long fileSize = file.length();
        
        // For large files, sample instead of reading entirely
        int sampleSize = Math.min((int) fileSize, 1024 * 1024); // 1MB sample
        int bytesToRead = (int) Math.min(fileSize, sampleSize);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[bytesToRead];
            int bytesRead = fis.read(buffer);
            
            for (int i = 0; i < bytesRead; i++) {
                freq[buffer[i] & 0xFF]++;
                total++;
            }
        }
        
        double entropy = 0.0;
        for (int f : freq) {
            if (f > 0) {
                double p = (double) f / total;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        
        // Normalize entropy based on file size
        if (fileSize > 0) {
            double sizeFactor = Math.min(1.0, (double) total / fileSize);
            entropy *= sizeFactor;
        }
        
        return entropy;
    }

    private String computeNormalizedTextHash(File file) throws IOException, NoSuchAlgorithmException {
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        String[] words = content.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        java.util.Arrays.sort(words);
        String normalized = String.join(" ", words).trim();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(normalized.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String computeEnhancedSsdeepHash(File file) {
        try {
            // Enhanced ssdeep computation with better error handling
            ProcessBuilder pb = new ProcessBuilder("ssdeep", "-b", file.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            String hash = null;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        // ssdeep output format: "filename,hash"
                        hash = parts[1].trim();
                        break;
                    }
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("ssdeep process exited with code: " + exitCode);
                return "";
            }
            
            return hash != null ? hash : "";
        } catch (Exception e) {
            System.err.println("Error computing enhanced ssdeep hash for " + file.getName() + ": " + e.getMessage());
            return "";
        }
    }
}