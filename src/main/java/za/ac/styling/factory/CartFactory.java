package za.ac.styling.factory;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class CartFactory {

    public static Cart createCart(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Cart createGuestCart() {

        return Cart.builder()
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Cart createCartWithUserAssociation(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        Cart cart = createCart(user);
        user.setCart(cart);

        return cart;
    }
}
