package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import za.ac.styling.domain.Category;
import za.ac.styling.service.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private CategoryService categoryService;
    private za.ac.styling.service.SupabaseStorageService supabaseStorageService;

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setSupabaseStorageService(za.ac.styling.service.SupabaseStorageService supabaseStorageService) {
        this.supabaseStorageService = supabaseStorageService;
    }

    @PostMapping("/upload/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadCategoryImage(@PathVariable Long categoryId, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            Category category = categoryService.read(categoryId);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Category not found"));
            }

            za.ac.styling.service.SupabaseStorageService.UploadResult result = supabaseStorageService.uploadCategoryImage(file, categoryId);
            category.setImageUrl(result.getUrl());
            categoryService.update(category);

            return ResponseEntity.ok(Map.of("success", true, "message", "Image uploaded successfully", "url", result.getUrl(), "data", category));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", iae.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error uploading image: " + e.getMessage()));
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        try {
            Category created = categoryService.create(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Long id) {
        try {
            Category category = categoryService.read(id);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Category not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving category: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Category category) {
        try {
            Category updated = categoryService.update(category);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Category not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating category: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Category> categories = categoryService.getAll();
            System.out.println("Fetched " + categories.size() + " categories");
            for (Category cat : categories) {
                System.out.println(
                        "Category: " + cat.getName() + ", ID: " + cat.getCategoryId() + ", Active: " + cat.isActive());
            }
            return ResponseEntity.ok(Map.of("success", true, "data", categories));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving categories: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Category deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error deleting category: " + e.getMessage()));
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getCategoryByName(@PathVariable String name) {
        try {
            Category category = categoryService.findByName(name)
                    .orElse(null);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Category not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving category: " + e.getMessage()));
        }
    }
}
