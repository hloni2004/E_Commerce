package za.ac.styling.service;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.CartItem;
import za.ac.styling.domain.Product;

import java.util.List;

public interface CartItemService extends IService<CartItem, Integer> {

    List<CartItem> findByCart(Cart cart);

    List<CartItem> findByCartId(Integer cartId);

    CartItem addToCart(Cart cart, Product product, Integer quantity);

    CartItem updateQuantity(Integer cartItemId, Integer quantity);

    void removeFromCart(Integer cartItemId);

    long countByCart(Cart cart);
}
