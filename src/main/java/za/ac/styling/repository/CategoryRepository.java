package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByIsActiveTrue();

    List<Category> findByIsActiveFalse();

    List<Category> findByParentCategoryIsNull();

    List<Category> findByParentCategory(Category parentCategory);

    List<Category> findByParentCategoryIsNullAndIsActiveTrue();

    boolean existsByName(String name);
}
