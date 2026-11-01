package com.rr.payment;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long orderId;
    public double amount;
    public String method;
    public String status;
    public String cardLast4;
    public LocalDateTime createdAt = LocalDateTime.now();
}
