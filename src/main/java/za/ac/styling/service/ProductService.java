package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.domain.Category;

import java.util.List;
import java.util.Optional;

public interface ProductService extends IService<Product, Integer> {

    Product readWithRelations(Integer id);

    List<Product> getAllWithRelations();

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(Category category);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategoryIdWithRelations(Long categoryId);

    List<Product> findActiveProducts();

    List<Product> findActiveProductsByCategory(Category category);

    List<Product> searchByName(String name);

    List<Product> findByPriceRange(double minPrice, double maxPrice);

    List<Product> findActiveProductsByPriceRange(double minPrice, double maxPrice);

    List<Product> findLatestProducts();

    List<Product> findProductsSortedByPriceAsc();

    List<Product> findProductsSortedByPriceDesc();

    Product activateProduct(Integer productId);

    Product deactivateProduct(Integer productId);

    ProductImage getImageById(Long imageId);

    List<Product> getAllIncludingDeleted();

    Product restoreProduct(Integer productId);
}
