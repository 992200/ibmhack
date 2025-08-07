package com.example.appmanager.service;

import com.example.appmanager.model.ApplicationFile;
import com.example.appmanager.model.Category;
import com.example.appmanager.model.Rule;
import com.example.appmanager.repository.CategoryRepository;
import com.example.appmanager.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleCategorizationService {
    @Autowired
    private RuleRepository ruleRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public void categorize(List<ApplicationFile> files) {
        List<Rule> rules = ruleRepository.findAll();
        for (ApplicationFile file : files) {
            for (Rule rule : rules) {
                // For simplicity, ruleExpression is a substring to match in file name or path
                if (file.getName().toLowerCase().contains(rule.getRuleExpression().toLowerCase()) ||
                    file.getPath().toLowerCase().contains(rule.getRuleExpression().toLowerCase())) {
                    Category category = categoryRepository.findByName(rule.getRuleName());
                    if (category != null) {
                        file.setCategory(category);
                        break; // Assign first matching category
                    }
                }
            }
        }
    }
}