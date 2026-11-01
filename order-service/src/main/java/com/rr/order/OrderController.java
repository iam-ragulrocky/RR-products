package com.rr.order;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final String PRODUCT = "http://PRODUCT-SERVICE/api/products/";
    private static final String PAYMENT = "http://PAYMENT-SERVICE/api/payments";

    private final OrderRepo repo;
    private final RestTemplate rt;

    public OrderController(OrderRepo repo, RestTemplate rt) { this.repo = repo; this.rt = rt; }

    public static class ItemReq { public Long productId; public int qty; }
    public static class OrderReq { public List<ItemReq> items; }

    /** Place an order. Stock is reserved and the order waits for payment (PENDING_PAYMENT). */
    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> create(@RequestHeader("X-User-Email") String email, @RequestBody OrderReq req) {
        if (req.items == null || req.items.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Your cart is empty"));
        Order o = new Order();
        o.userEmail = email;
        o.status = "PENDING_PAYMENT";
        List<ItemReq> reserved = new ArrayList<>();
        try {
            for (ItemReq i : req.items) {
                Map<String, Object> p = rt.getForObject(PRODUCT + i.productId, Map.class);
                int stock = ((Number) p.get("stock")).intValue();
                if (i.qty < 1 || stock < i.qty) throw new IllegalStateException("Not enough stock for " + p.get("name"));
                OrderItem it = new OrderItem();
                it.productId = i.productId;
                it.name = (String) p.get("name");
                it.price = ((Number) p.get("price")).doubleValue();
                it.qty = i.qty;
                rt.put(PRODUCT + i.productId + "/reduce?qty=" + i.qty, null);
                reserved.add(i);
                o.items.add(it);
                o.total += it.price * it.qty;
            }
        } catch (Exception e) {
            for (ItemReq r : reserved) rt.put(PRODUCT + r.productId + "/restore?qty=" + r.qty, null);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() == null ? "Could not place order" : e.getMessage()));
        }
        return ResponseEntity.ok(repo.save(o));
    }

    @GetMapping("/my")
    public List<Order> mine(@RequestHeader("X-User-Email") String email) {
        return repo.findByUserEmailOrderByIdDesc(email);
    }

    /** Admin only (enforced at the gateway). */
    @GetMapping
    public List<Order> all() { return repo.findAll(Sort.by(Sort.Direction.DESC, "id")); }

    @PostMapping("/{id}/pay")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> pay(@RequestHeader("X-User-Email") String email, @PathVariable Long id,
                                 @RequestBody Map<String, String> b) {
        Order o = repo.findById(id).orElse(null);
        if (o == null || !o.userEmail.equals(email)) return ResponseEntity.notFound().build();
        if (!"PENDING_PAYMENT".equals(o.status))
            return ResponseEntity.badRequest().body(Map.of("message", "This order is not waiting for payment"));
        Map<String, Object> pay = new HashMap<>();
        pay.put("orderId", o.id);
        pay.put("amount", o.total);
        pay.put("method", b.get("method"));
        pay.put("cardNumber", b.get("cardNumber"));
        Map<String, Object> r = rt.postForObject(PAYMENT, pay, Map.class);
        if (r != null && "SUCCESS".equals(r.get("status"))) {
            o.status = "PAID";
            return ResponseEntity.ok(repo.save(o));
        }
        return ResponseEntity.status(402).body(Map.of("message", "Payment failed. Check your card number."));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@RequestHeader("X-User-Email") String email, @PathVariable Long id) {
        Order o = repo.findById(id).orElse(null);
        if (o == null || !o.userEmail.equals(email)) return ResponseEntity.notFound().build();
        if (!"PENDING_PAYMENT".equals(o.status))
            return ResponseEntity.badRequest().body(Map.of("message", "Only unpaid orders can be cancelled"));
        for (OrderItem i : o.items) rt.put(PRODUCT + i.productId + "/restore?qty=" + i.qty, null);
        o.status = "CANCELLED";
        return ResponseEntity.ok(repo.save(o));
    }
}
