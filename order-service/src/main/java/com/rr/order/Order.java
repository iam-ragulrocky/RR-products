package com.rr.order;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String userEmail;
    public double total;
    /** PENDING_PAYMENT, PAID or CANCELLED */
    public String status;
    public LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    public List<OrderItem> items = new ArrayList<>();
}
