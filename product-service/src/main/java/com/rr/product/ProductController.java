package com.rr.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepo repo;

    public ProductController(ProductRepo repo) { this.repo = repo; }

    @GetMapping
    public List<Product> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Product> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product add(@RequestBody Product p) {
        p.id = null;
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product p) {
        Product x = repo.findById(id).orElse(null);
        if (x == null) return ResponseEntity.notFound().build();
        x.name = p.name; x.description = p.description; x.price = p.price;
        x.stock = p.stock; x.category = p.category; x.emoji = p.emoji;
        return ResponseEntity.ok(repo.save(x));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }

    /** Called by order-service when an order is placed. */
    @PutMapping("/{id}/reduce")
    public ResponseEntity<?> reduce(@PathVariable Long id, @RequestParam int qty) {
        Product x = repo.findById(id).orElse(null);
        if (x == null) return ResponseEntity.notFound().build();
        if (x.stock < qty) return ResponseEntity.badRequest().body(Map.of("message", "Not enough stock"));
        x.stock -= qty;
        return ResponseEntity.ok(repo.save(x));
    }

    /** Called by order-service when an order is cancelled. */
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable Long id, @RequestParam int qty) {
        Product x = repo.findById(id).orElse(null);
        if (x == null) return ResponseEntity.notFound().build();
        x.stock += qty;
        return ResponseEntity.ok(repo.save(x));
    }
}
