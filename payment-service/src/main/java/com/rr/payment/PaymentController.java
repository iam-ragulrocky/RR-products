package com.rr.payment;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Mock payment gateway: a Card payment succeeds when the card has 12+ digits. UPI and Net banking always succeed. */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepo repo;

    public PaymentController(PaymentRepo repo) { this.repo = repo; }

    @PostMapping
    public Payment pay(@RequestBody Map<String, Object> b) {
        String method = String.valueOf(b.get("method"));
        String card = b.get("cardNumber") == null ? "" : b.get("cardNumber").toString().replaceAll("\\D", "");
        boolean ok = !"Card".equals(method) || card.length() >= 12;
        Payment p = new Payment();
        p.orderId = ((Number) b.get("orderId")).longValue();
        p.amount = ((Number) b.get("amount")).doubleValue();
        p.method = method;
        p.cardLast4 = card.length() >= 4 ? card.substring(card.length() - 4) : "";
        p.status = ok ? "SUCCESS" : "FAILED";
        return repo.save(p);
    }

    @GetMapping
    public List<Payment> all() { return repo.findAll(Sort.by(Sort.Direction.DESC, "id")); }
}
