package com.example.SuperShop_Enhanced.service;

import com.example.SuperShop_Enhanced.entity.Cart;
import com.example.SuperShop_Enhanced.entity.Product;
import com.example.SuperShop_Enhanced.entity.User;
import com.example.SuperShop_Enhanced.repository.CartRepository;
import com.example.SuperShop_Enhanced.repository.ProductRepository;
import com.example.SuperShop_Enhanced.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    public Cart addProductToCart(int userId, int productId) {
        Optional<User> userOpt = userService.getUserById(userId);
        Product product = productRepository.getProductById(productId);

        if (userOpt.isEmpty() || product == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setUser(userOpt.get());
        cart.setProduct(product);
        cart.setQuantity(1); 

        return cartRepository.save(cart);
    }

    public Cart updateCartItem(Cart cart) {
        Optional<Cart> existingCartOpt = cartRepository.findById(cart.getId());
        if (existingCartOpt.isPresent()) {
            Cart existingCart = existingCartOpt.get();
            if (cart.getQuantity() != 0) {
                existingCart.setQuantity(cart.getQuantity());
            }
            return cartRepository.save(existingCart);
        } else {
            return null;
        }
    }

    public void removeProductFromCart(int cartId) {
        cartRepository.deleteById(cartId);
    }

    public List<Cart> getCartByUserId(int userId) {
        return cartRepository.findAll().stream()
                .filter(c -> c.getUser().getId() == userId)
                .toList();
    }
}
