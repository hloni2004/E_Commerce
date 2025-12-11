package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Cart;
import za.ac.styling.domain.User;
import za.ac.styling.repository.CartRepository;
import za.ac.styling.service.CartService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Cart entity
 */
@Service
public class CartServiceImpl implements CartService {

    private CartRepository cartRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart create(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart read(Integer id) {
        return cartRepository.findById(id).orElse(null);
    }

    @Override
    public Cart update(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getAll() {
        return cartRepository.findAll();
    }

    @Override
    public Optional<Cart> findByUser(User user) {
        return cartRepository.findByUser(user);
    }

    @Override
    public Optional<Cart> findByUserId(Integer userId) {
        return cartRepository.findByUserUserId(userId);
    }

    @Override
    public Cart createCartForUser(User user) {
        Optional<Cart> existingCart = findByUser(user);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        Cart cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return create(cart);
    }

    @Override
    public void clearCart(Integer cartId) {
        Cart cart = read(cartId);
        if (cart != null && cart.getItems() != null) {
            cart.getItems().clear();
            cart.setUpdatedAt(LocalDateTime.now());
            update(cart);
        }
    }

    @Override
    public double getCartTotal(Integer cartId) {
        Cart cart = read(cartId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0.0;
        }

        return cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getBasePrice() * item.getQuantity())
                .sum();
    }

    @Override
    public void delete(Integer id) {
        cartRepository.deleteById(id);
    }
}
