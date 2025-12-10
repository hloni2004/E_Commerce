package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Cart;
import za.ac.styling.domain.CartItem;
import za.ac.styling.domain.Product;
import za.ac.styling.repository.CartItemRepository;
import za.ac.styling.service.CartItemService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for CartItem entity
 */
@Service
public class CartItemServiceImpl implements CartItemService {

    private CartItemRepository cartItemRepository;

    @Autowired
    public CartItemServiceImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartItem create(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem read(Integer id) {
        return cartItemRepository.findById(id).orElse(null);
    }

    @Override
    public CartItem update(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public List<CartItem> getAll() {
        return cartItemRepository.findAll();
    }

    @Override
    public List<CartItem> findByCart(Cart cart) {
        return cartItemRepository.findByCart(cart);
    }

    @Override
    public List<CartItem> findByCartId(Integer cartId) {
        return cartItemRepository.findByCartCartId(cartId);
    }

    @Override
    public CartItem addToCart(Cart cart, Product product, Integer quantity) {
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return update(item);
        }

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();

        return create(cartItem);
    }

    @Override
    public CartItem updateQuantity(Integer cartItemId, Integer quantity) {
        CartItem cartItem = read(cartItemId);
        if (cartItem != null) {
            cartItem.setQuantity(quantity);
            return update(cartItem);
        }
        return null;
    }

    @Override
    public void removeFromCart(Integer cartItemId) {
        CartItem cartItem = read(cartItemId);
        if (cartItem != null) {
            cartItemRepository.delete(cartItem);
        }
    }

    @Override
    public long countByCart(Cart cart) {
        return cartItemRepository.countByCart(cart);
    }
}
