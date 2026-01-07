package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductProductId(Integer productId);

    List<ProductImage> findByProductOrderByDisplayOrderAsc(Product product);

    Optional<ProductImage> findByProductAndIsPrimaryTrue(Product product);

    List<ProductImage> findByProductAndIsPrimaryFalse(Product product);
}
