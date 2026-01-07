package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.repository.ProductColourRepository;
import za.ac.styling.service.ProductColourService;

import java.util.List;

@Service
public class ProductColourServiceImpl implements ProductColourService {

    private ProductColourRepository productColourRepository;

    @Autowired
    public ProductColourServiceImpl(ProductColourRepository productColourRepository) {
        this.productColourRepository = productColourRepository;
    }

    @Override
    public ProductColour create(ProductColour productColour) {
        return productColourRepository.save(productColour);
    }

    @Override
    public ProductColour read(Integer id) {
        return productColourRepository.findById(id).orElse(null);
    }

    @Override
    public ProductColour update(ProductColour productColour) {
        return productColourRepository.save(productColour);
    }

    @Override
    public List<ProductColour> getAll() {
        return productColourRepository.findAll();
    }

    @Override
    public List<ProductColour> findByProduct(Product product) {
        return productColourRepository.findByProduct(product);
    }

    @Override
    public List<ProductColour> findByProductId(Integer productId) {
        return productColourRepository.findByProductProductId(productId);
    }

    @Override
    public List<ProductColour> findByProductAndName(Product product, String name) {
        return productColourRepository.findByProductAndName(product, name);
    }

    @Override
    public boolean hasColour(Product product, String colourName) {
        List<ProductColour> colours = findByProductAndName(product, colourName);
        return !colours.isEmpty();
    }

    @Override
    public void delete(Integer id) {
        productColourRepository.deleteById(id);
    }
}
