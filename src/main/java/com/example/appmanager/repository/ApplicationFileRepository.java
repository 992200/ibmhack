package com.example.appmanager.repository;

import com.example.appmanager.model.ApplicationFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationFileRepository extends JpaRepository<ApplicationFile, Long> {
    List<ApplicationFile> findByHash(String hash);
}