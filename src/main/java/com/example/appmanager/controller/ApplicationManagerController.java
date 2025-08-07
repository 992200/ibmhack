package com.example.appmanager.controller;

import com.example.appmanager.model.ApplicationFile;
import com.example.appmanager.repository.ApplicationFileRepository;
import com.example.appmanager.service.DuplicateDetectorService;
import com.example.appmanager.service.FileScannerService;
import com.example.appmanager.service.RuleCategorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Controller
public class ApplicationManagerController {
    @Autowired
    private FileScannerService fileScannerService;
    @Autowired
    private DuplicateDetectorService duplicateDetectorService;
    @Autowired
    private RuleCategorizationService ruleCategorizationService;
    @Autowired
    private ApplicationFileRepository applicationFileRepository;

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/test")
    public String test() {
        return "test_simple";
    }

    @GetMapping("/debug")
    public String debug(Model model) {
        try {
            List<ApplicationFile> files = applicationFileRepository.findAll();
            model.addAttribute("fileCount", files.size());
            model.addAttribute("files", files);
            return "debug";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "debug";
        }
    }

    @PostMapping("/scan")
    public String scanDirectory(@RequestParam("directory") String directory, 
                               @RequestParam(value = "enableCategorization", required = false) Boolean enableCategorization,
                               @RequestParam(value = "categories", required = false) List<String> categories,
                               Model model) {
        try {
            List<ApplicationFile> files = fileScannerService.scanDirectory(directory);
            
            // Apply categorization if enabled
            if (enableCategorization != null && enableCategorization && categories != null && !categories.isEmpty()) {
                categorizeAndOrganizeFiles(files, directory, categories);
            }
            
            ruleCategorizationService.categorize(files);
            applicationFileRepository.deleteAll();
            applicationFileRepository.saveAll(files);
            model.addAttribute("files", files);
            model.addAttribute("categorizationEnabled", enableCategorization);
            model.addAttribute("selectedCategories", categories);
            return "scan-result";
        } catch (IOException | NoSuchAlgorithmException e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }

    private void categorizeAndOrganizeFiles(List<ApplicationFile> files, String baseDirectory, List<String> categories) {
        // Define file extensions for each category
        Map<String, List<String>> categoryExtensions = new HashMap<>();
        categoryExtensions.put("photos", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"));
        categoryExtensions.put("documents", Arrays.asList("doc", "docx", "pdf", "txt", "rtf", "odt", "pages"));
        categoryExtensions.put("videos", Arrays.asList("mp4", "avi", "mov", "mkv", "wmv", "flv", "webm"));
        categoryExtensions.put("music", Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "wma"));
        categoryExtensions.put("archives", Arrays.asList("zip", "rar", "7z", "tar", "gz", "bz2"));
        categoryExtensions.put("applications", Arrays.asList("exe", "msi", "apk", "jar", "dmg", "deb", "rpm"));

        // Create category folders and move files
        for (String category : categories) {
            if (categoryExtensions.containsKey(category)) {
                String categoryFolder = baseDirectory + File.separator + category;
                File folder = new File(categoryFolder);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                List<String> extensions = categoryExtensions.get(category);
                for (ApplicationFile file : files) {
                    String fileExtension = getFileExtension(file.getName()).toLowerCase();
                    if (extensions.contains(fileExtension)) {
                        try {
                            File sourceFile = new File(file.getPath());
                            File destFile = new File(categoryFolder + File.separator + file.getName());
                            
                            // Only move if file exists and destination doesn't exist
                            if (sourceFile.exists() && !destFile.exists()) {
                                java.nio.file.Files.move(sourceFile.toPath(), destFile.toPath());
                                // Update the file path in our model
                                file.setPath(destFile.getAbsolutePath());
                            }
                        } catch (Exception e) {
                            // Log error but continue with other files
                            System.err.println("Error moving file " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    }

    @GetMapping("/duplicates")
    public String showDuplicates(Model model) {
        try {
            List<ApplicationFile> files = applicationFileRepository.findAll();
            Map<String, List<ApplicationFile>> duplicates = duplicateDetectorService.findDuplicates(files);
            if (duplicates == null) {
                duplicates = new HashMap<>();
            }
            model.addAttribute("duplicates", duplicates);
            return "duplicates";
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error in showDuplicates: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message to model
            model.addAttribute("error", "An error occurred while processing duplicates: " + e.getMessage());
            model.addAttribute("duplicates", new HashMap<>());
            return "duplicates";
        }
    }

    @PostMapping("/remove")
    public String removeDuplicates(@RequestParam(value = "fileIds", required = false) List<Long> fileIds, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (fileIds != null && !fileIds.isEmpty()) {
            List<ApplicationFile> filesToDelete = applicationFileRepository.findAllById(fileIds);
            for (ApplicationFile appFile : filesToDelete) {
                try {
                    java.io.File file = new java.io.File(appFile.getPath());
                    if (file.exists() && file.delete()) {
                        // File deleted successfully
                    } else {
                        // Could not delete file, maybe log this
                    }
                } catch (Exception e) {
                    // Log error if needed
                }
            }
            applicationFileRepository.deleteAllById(fileIds);
            redirectAttributes.addFlashAttribute("message", "Selected duplicates removed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("message", "No files selected for removal.");
        }
        return "redirect:/duplicates";
    }
}