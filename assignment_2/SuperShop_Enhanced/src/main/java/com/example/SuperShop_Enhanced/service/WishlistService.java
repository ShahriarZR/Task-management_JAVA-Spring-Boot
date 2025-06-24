package com.example.SuperShop_Enhanced.service;

import com.example.SuperShop_Enhanced.entity.Wishlist;
import com.example.SuperShop_Enhanced.entity.Product;
import com.example.SuperShop_Enhanced.entity.User;
import com.example.SuperShop_Enhanced.repository.WishlistRepository;
import com.example.SuperShop_Enhanced.repository.ProductRepository;
import com.example.SuperShop_Enhanced.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    public Wishlist addProductToWishlist(int userId, int productId) {
        Optional<User> userOpt = userService.getUserById(userId);
        Product product = productRepository.getProductById(productId);

        if (userOpt.isEmpty() || product == null) {
            return null;
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(userOpt.get());
        wishlist.setProduct(product);
        wishlist.setQuantity(1);

        return wishlistRepository.save(wishlist);
    }

    public Wishlist updateWishlistItem(Wishlist wishlist) {
        Optional<Wishlist> existingWishlistOpt = wishlistRepository.findById(wishlist.getId());
        if (existingWishlistOpt.isPresent()) {
            Wishlist existingWishlist = existingWishlistOpt.get();
            if (wishlist.getQuantity() != 0) {
                existingWishlist.setQuantity(wishlist.getQuantity());
            }
            return wishlistRepository.save(existingWishlist);
        } else {
            return null;
        }
    }

    public void removeProductFromWishlist(int wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }

    public List<Wishlist> getWishlistByUserId(int userId) {
        return wishlistRepository.findAll().stream()
                .filter(w -> w.getUser().getId() == userId)
                .toList();
    }

    public boolean moveProductToCart(int wishlistId) {
        Optional<Wishlist> wishlistOpt = wishlistRepository.findById(wishlistId);
        if (wishlistOpt.isEmpty()) {
            return false;
        }
        Wishlist wishlist = wishlistOpt.get();
        int userId = wishlist.getUser().getId();
        int productId = wishlist.getProduct().getId();

        var cart = cartService.addProductToCart(userId, productId);
        if (cart == null) {
            return false;
        }

        wishlistRepository.deleteById(wishlistId);
        return true;
    }
}
