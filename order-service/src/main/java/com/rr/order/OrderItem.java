package com.rr.order;

import jakarta.persistence.Embeddable;

@Embeddable
public class OrderItem {
    public Long productId;
    public String name;
    public double price;
    public int qty;
}
