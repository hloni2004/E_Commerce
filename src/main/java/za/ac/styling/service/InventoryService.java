package za.ac.styling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.repository.ProductColourSizeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductColourSizeRepository productColourSizeRepository;

    public boolean checkStockAvailability(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();
            int availableStock = size.getStockQuantity() - size.getReservedQuantity();

            if (availableStock < item.getQuantity()) {
                System.err.println("Insufficient stock for product: " + item.getProduct().getName() + 
                    ", Size: " + size.getSizeName() + 
                    ", Required: " + item.getQuantity() + 
                    ", Available: " + availableStock);
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void reserveStock(List<OrderItem> items) throws InsufficientStockException {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();
            int availableStock = size.getStockQuantity() - size.getReservedQuantity();

            if (availableStock < item.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for " + item.getProduct().getName() + 
                    " (Size: " + size.getSizeName() + "). Available: " + availableStock
                );
            }

            size.setReservedQuantity(size.getReservedQuantity() + item.getQuantity());
            productColourSizeRepository.save(size);

            System.out.println("Reserved " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + ")");
        }
    }

    @Transactional
    public void commitStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();

            size.setReservedQuantity(size.getReservedQuantity() - item.getQuantity());
            size.setStockQuantity(size.getStockQuantity() - item.getQuantity());

            productColourSizeRepository.save(size);

            System.out.println("Committed " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + 
                "). Remaining stock: " + size.getStockQuantity());
        }
    }

    @Transactional
    public void releaseStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();

            size.setReservedQuantity(Math.max(0, size.getReservedQuantity() - item.getQuantity()));

            productColourSizeRepository.save(size);

            System.out.println("Released " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + ")");
        }
    }

    @Transactional
    public void returnStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();

            size.setStockQuantity(size.getStockQuantity() + item.getQuantity());

            productColourSizeRepository.save(size);

            System.out.println("Returned " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + 
                "). New stock: " + size.getStockQuantity());
        }
    }

    public int getAvailableStock(Integer sizeId) {
        return productColourSizeRepository.findById(sizeId)
            .map(size -> size.getStockQuantity() - size.getReservedQuantity())
            .orElse(0);
    }

    public boolean needsReorder(ProductColourSize size) {
        int availableStock = size.getStockQuantity() - size.getReservedQuantity();
        return availableStock <= size.getReorderLevel();
    }

    public List<ProductColourSize> getLowStockItems() {
        return productColourSizeRepository.findLowStockItems();
    }

    public List<ProductColourSize> getOutOfStockItems() {
        return productColourSizeRepository.findOutOfStockItems();
    }

    public static class InsufficientStockException extends Exception {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
