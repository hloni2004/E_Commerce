package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Category;
import za.ac.styling.repository.CategoryRepository;
import za.ac.styling.service.CategoryService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "categories", allEntries = true)
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "categories", key = "#id")
    public Category read(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "categories", allEntries = true)
    public Category update(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable("categories")
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> findActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Override
    public List<Category> findRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    @Override
    public List<Category> findSubCategories(Category parentCategory) {
        return categoryRepository.findByParentCategory(parentCategory);
    }

    @Override
    public List<Category> findActiveRootCategories() {
        return categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "categories", allEntries = true)
    public Category activateCategory(Long categoryId) {
        Category category = read(categoryId);
        if (category != null) {
            category.setActive(true);
            return update(category);
        }
        return null;
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "categories", allEntries = true)
    public Category deactivateCategory(Long categoryId) {
        Category category = read(categoryId);
        if (category != null) {
            category.setActive(false);
            return update(category);
        }
        return null;
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
