package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;

import java.util.List;

@Repository
public interface ProductColourRepository extends JpaRepository<ProductColour, Integer> {

    List<ProductColour> findByProduct(Product product);

    List<ProductColour> findByProductProductId(Integer productId);

    List<ProductColour> findByProductAndName(Product product, String name);
}
