package com.example.SuperShop_Enhanced.api;

import com.example.SuperShop_Enhanced.entity.Cart;
import com.example.SuperShop_Enhanced.entity.User;
import com.example.SuperShop_Enhanced.service.CartService;
import com.example.SuperShop_Enhanced.service.UserService;
import com.example.SuperShop_Enhanced.service.OrderService;
import com.example.SuperShop_Enhanced.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
public class CartApi {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<Cart> addProductToCart(@RequestBody Map<String, Integer> requestBody) {
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

        Cart createdCart = cartService.addProductToCart(user.getId(), productId);
        return ResponseEntity.ok(createdCart);
    }

    @PostMapping("/proceed-to-order")
    public ResponseEntity<Void> proceedToOrder() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        List<Cart> cartItems = cartService.getCartByUserId(user.getId());
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        for (Cart cartItem : cartItems) {
            orderService.createOrder(user.getId(), cartItem.getProduct().getId());
            cartService.removeProductFromCart(cartItem.getId());
        }

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateCartItem(@PathVariable int id, @RequestBody Map<String, Integer> requestBody) {
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

        Optional<Cart> existingCartOpt = cartService.getCartByUserId(user.getId()).stream()
                .filter(c -> c.getId() == id)
                .findFirst();

        if (existingCartOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        Cart existingCart = existingCartOpt.get();
        existingCart.setQuantity(quantity);

        Cart updatedCart = cartService.updateCartItem(existingCart);
        if (updatedCart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable int id) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        Optional<Cart> existingCartOpt = cartService.getCartByUserId(user.getId()).stream()
                .filter(c -> c.getId() == id)
                .findFirst();

        if (existingCartOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        cartService.removeProductFromCart(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user")
    public ResponseEntity<List<Cart>> getCartByUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();

        List<Cart> cart = cartService.getCartByUserId(user.getId());
        return ResponseEntity.ok(cart);
    }
}