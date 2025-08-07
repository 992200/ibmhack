package com.example.appmanager.model;

import jakarta.persistence.*;

@Entity
public class ApplicationFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name = "";
    private String path = "";
    private String hash = "";
    private long size = 0L;
    private String fileType = "unknown"; // e.g., extension or MIME type
    private double entropy = 0.0; // Shannon entropy for file content
    private String ssdeepHash = "";
    private double similarityScore = 0.0; // Percentage similarity (0-100)

    @ManyToOne
    private Category category;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name != null ? name : ""; }
    public String getPath() { return path != null ? path : ""; }
    public void setPath(String path) { this.path = path != null ? path : ""; }
    public String getHash() { return hash != null ? hash : ""; }
    public void setHash(String hash) { this.hash = hash != null ? hash : ""; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getFileType() { return fileType != null ? fileType : "unknown"; }
    public void setFileType(String fileType) { this.fileType = fileType != null ? fileType : "unknown"; }
    public double getEntropy() { return entropy; }
    public void setEntropy(double entropy) { this.entropy = entropy; }
    public String getSsdeepHash() { return ssdeepHash != null ? ssdeepHash : ""; }
    public void setSsdeepHash(String ssdeepHash) { this.ssdeepHash = ssdeepHash != null ? ssdeepHash : ""; }
    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}