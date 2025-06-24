package com.example.SuperShop_Enhanced.api;

import com.example.SuperShop_Enhanced.entity.Wishlist;
import com.example.SuperShop_Enhanced.entity.User;
import com.example.SuperShop_Enhanced.service.WishlistService;
import com.example.SuperShop_Enhanced.service.UserService;
import com.example.SuperShop_Enhanced.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistApi {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<Wishlist> addProductToWishlist(@RequestBody Map<String, Integer> requestBody) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        Integer productId = requestBody.get("product_id");
        if (productId == null) {
            return ResponseEntity.badRequest().build();
        }

        Wishlist createdWishlist = wishlistService.addProductToWishlist(user.getId(), productId);
        return ResponseEntity.ok(createdWishlist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wishlist> updateWishlistItem(@PathVariable int id, @RequestBody Map<String, Integer> requestBody) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        Integer quantity = requestBody.get("quantity");
        if (quantity == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Wishlist> existingWishlistOpt = wishlistService.getWishlistByUserId(user.getId()).stream()
                .filter(w -> w.getId() == id)
                .findFirst();

        if (existingWishlistOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        Wishlist existingWishlist = existingWishlistOpt.get();
        existingWishlist.setQuantity(quantity);

        Wishlist updatedWishlist = wishlistService.updateWishlistItem(existingWishlist);
        if (updatedWishlist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedWishlist);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeProductFromWishlist(@PathVariable int id) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        Optional<Wishlist> existingWishlistOpt = wishlistService.getWishlistByUserId(user.getId()).stream()
                .filter(w -> w.getId() == id)
                .findFirst();

        if (existingWishlistOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        wishlistService.removeProductFromWishlist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user")
    public ResponseEntity<List<Wishlist>> getWishlistByUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        List<Wishlist> wishlist = wishlistService.getWishlistByUserId(user.getId());
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/move-to-cart/{id}")
    public ResponseEntity<Void> moveProductToCart(@PathVariable int id) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        boolean success = wishlistService.moveProductToCart(id);
        if (!success) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.noContent().build();
    }
}
