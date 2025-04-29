package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.PaymentService;
import com.bacos.mokengeli.biloko.presentation.controller.model.PaymentRequest;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //@PreAuthorize("hasAuthority('PROCESS_PAYMENT')")
    @PostMapping
    public DomainOrder recordPayment(@RequestBody PaymentRequest request) {
        try {
            return paymentService.recordPayment(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    request.getNotes(),
                    request.getDiscountAmount()
            );
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e.getTechnicalId()
            );
        }
    }

    //@PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping("/{orderId}/history")
    public List<DomainOrder.DomainPaymentTransaction> getPaymentHistory(@PathVariable Long orderId) {
        try {
            return paymentService.getPaymentHistory(orderId);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e.getTechnicalId()
            );
        }
    }

    //@PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping("/status/{status}")
    public List<DomainOrder> getOrdersByPaymentStatus(@PathVariable OrderPaymentStatus status) {
        try {
            return paymentService.getOrdersByPaymentStatus(status);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e.getTechnicalId()
            );
        }
    }

    //@PreAuthorize("hasAuthority('VIEW_PAYMENT')")
    @GetMapping("/requiring-payment")
    public List<DomainOrder> getOrdersRequiringPayment() {
        try {
            return paymentService.getOrdersRequiringPayment();
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e.getTechnicalId()
            );
        }
    }


}