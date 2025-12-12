package za.ac.styling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.repository.ProductColourSizeRepository;

import java.util.List;

/**
 * Service for managing real-time inventory operations
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductColourSizeRepository productColourSizeRepository;

    /**
     * Check if sufficient stock is available for an order
     * @param items List of order items to check
     * @return true if all items have sufficient stock
     */
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

    /**
     * Reserve stock for order items (when order is placed)
     * @param items List of order items
     * @throws InsufficientStockException if stock is not available
     */
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
            
            // Reserve the stock
            size.setReservedQuantity(size.getReservedQuantity() + item.getQuantity());
            productColourSizeRepository.save(size);
            
            System.out.println("Reserved " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + ")");
        }
    }

    /**
     * Commit stock (convert reserved to sold when order is confirmed)
     * @param items List of order items
     */
    @Transactional
    public void commitStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();
            
            // Reduce both reserved and total stock
            size.setReservedQuantity(size.getReservedQuantity() - item.getQuantity());
            size.setStockQuantity(size.getStockQuantity() - item.getQuantity());
            
            productColourSizeRepository.save(size);
            
            System.out.println("Committed " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + 
                "). Remaining stock: " + size.getStockQuantity());
        }
    }

    /**
     * Release reserved stock (when order is cancelled)
     * @param items List of order items
     */
    @Transactional
    public void releaseStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();
            
            // Release reserved stock
            size.setReservedQuantity(Math.max(0, size.getReservedQuantity() - item.getQuantity()));
            
            productColourSizeRepository.save(size);
            
            System.out.println("Released " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + ")");
        }
    }

    /**
     * Return stock (when item is returned/refunded)
     * @param items List of order items
     */
    @Transactional
    public void returnStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ProductColourSize size = item.getColourSize();
            
            // Add back to stock
            size.setStockQuantity(size.getStockQuantity() + item.getQuantity());
            
            productColourSizeRepository.save(size);
            
            System.out.println("Returned " + item.getQuantity() + " units of " + 
                item.getProduct().getName() + " (Size: " + size.getSizeName() + 
                "). New stock: " + size.getStockQuantity());
        }
    }

    /**
     * Get available stock for a specific size
     * @param sizeId Size ID
     * @return Available stock quantity
     */
    public int getAvailableStock(Integer sizeId) {
        return productColourSizeRepository.findById(sizeId)
            .map(size -> size.getStockQuantity() - size.getReservedQuantity())
            .orElse(0);
    }

    /**
     * Check if product size needs reorder
     * @param size Product colour size
     * @return true if stock is at or below reorder level
     */
    public boolean needsReorder(ProductColourSize size) {
        int availableStock = size.getStockQuantity() - size.getReservedQuantity();
        return availableStock <= size.getReorderLevel();
    }

    /**
     * Get all low stock items
     * @return List of sizes with low stock
     */
    public List<ProductColourSize> getLowStockItems() {
        return productColourSizeRepository.findLowStockItems();
    }

    /**
     * Get all out of stock items
     * @return List of sizes that are out of stock
     */
    public List<ProductColourSize> getOutOfStockItems() {
        return productColourSizeRepository.findOutOfStockItems();
    }

    /**
     * Custom exception for insufficient stock
     */
    public static class InsufficientStockException extends Exception {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
