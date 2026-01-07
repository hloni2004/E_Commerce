package za.ac.styling.service;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.User;

import java.util.Optional;

public interface CartService extends IService<Cart, Integer> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Integer userId);

    Cart createCartForUser(User user);

    void clearCart(Integer cartId);

    double getCartTotal(Integer cartId);
}
