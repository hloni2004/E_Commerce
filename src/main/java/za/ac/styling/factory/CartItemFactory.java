package za.ac.styling.factory;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.CartItem;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.domain.ProductColourSize;

public class CartItemFactory {

    public static CartItem createCartItem(Cart cart, Product product, ProductColour colour,
                                         ProductColourSize size, int quantity) {

        if (cart == null) {
            throw new IllegalArgumentException("Cart is required");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }

        if (colour == null) {
            throw new IllegalArgumentException("Product colour is required");
        }

        if (size == null) {
            throw new IllegalArgumentException("Product size is required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (quantity > size.getStockQuantity()) {
            throw new IllegalArgumentException("Quantity exceeds available stock");
        }

        return CartItem.builder()
                .cart(cart)
                .product(product)
                .colour(colour)
                .size(size)
                .quantity(quantity)
                .build();
    }

    public static CartItem createCartItem(Cart cart, Product product, ProductColour colour,
                                         ProductColourSize size) {
        return createCartItem(cart, product, colour, size, 1);
    }

    public static CartItem updateQuantity(CartItem cartItem, int newQuantity) {
        if (cartItem == null) {
            throw new IllegalArgumentException("CartItem cannot be null");
        }

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (newQuantity > cartItem.getSize().getStockQuantity()) {
            throw new IllegalArgumentException("Quantity exceeds available stock");
        }

        cartItem.setQuantity(newQuantity);
        return cartItem;
    }
}
