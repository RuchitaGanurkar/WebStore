package com.webstore.service.whatsapp.business;

import com.webstore.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryBusinessService {

    private final CategoryRepository categoryRepository;

    public CategoryBusinessService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<String> getAllCategoryNames() {
        try {
            return categoryRepository.findAllCategoryNames();
        } catch (Exception e) {
            return categoryRepository.findTop3CategoryNames();
        }
    }

    public List<String> getTop3CategoryNames() {
        return categoryRepository.findTop3CategoryNames();
    }

    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }

    public Integer getCategoryIdByName(String categoryName) {
        return categoryRepository.findCategoryIdByCategoryName(categoryName);
    }

    public boolean shouldUseButtonsForCategories() {
        return getTotalCategoryCount() <= 3;
    }
}
