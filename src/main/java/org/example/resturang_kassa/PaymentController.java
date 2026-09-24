package org.example.resturang_kassa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @RequestBody CreatePaymentRequest request
    ) {
        return paymentService.createPayment(request.orderId());
    }

    @PostMapping("/{reference}/complete-test-payment")
    public PaymentResponse completeTestPayment(
            @PathVariable String reference
    ){
        return paymentService.completeTestPayment(reference);
    }

    @GetMapping("/{reference}")
    public PaymentResponse getPayment(
            @PathVariable String reference
    ){
        return paymentService.getPayment(reference);
    }
}
