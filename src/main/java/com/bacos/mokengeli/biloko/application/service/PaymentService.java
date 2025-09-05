package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.domain.TableState;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {
    private final OrderPort orderPort;
    private final UserAppService userAppService;
    private final OrderNotificationService orderNotificationService;

    @Autowired
    public PaymentService(OrderPort orderPort,
                          UserAppService userAppService,
                          OrderNotificationService orderNotificationService) {
        this.orderPort = orderPort;
        this.userAppService = userAppService;
        this.orderNotificationService = orderNotificationService;
    }

    /**
     * Enregistre un paiement pour une commande
     */
    public DomainOrder recordPayment(Long orderId, Double amount, String paymentMethod,
                                     String notes, Double discountAmount) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        // Vérifier que la commande appartient au tenant
        if (!this.orderPort.isOrderBelongToTenant(orderId, connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to record payment for order [{}] of other tenant code [{}]",
                    errorId, connectedUser.getEmployeeNumber(), orderId, connectedUser.getTenantCode());
            throw new ServiceException(errorId, "A problem occurred with the payment.");
        }

        try {
            Optional<DomainOrder> order = this.orderPort.getOrder(orderId);

            DomainOrder domainOrder = order.get();
            OrderPaymentStatus paymentStatus = domainOrder.getPaymentStatus();


            // si on est pas dans un etat necessitant un paiement cela signifie que la commande
            // a deja été réglé
            // on notifie quand meme
            if (!OrderPaymentStatus.PARTIALLY_PAID.equals(paymentStatus)
                    && !OrderPaymentStatus.UNPAID.equals(paymentStatus)) {
                boolean isTableFree = this.orderPort.isTableFree(domainOrder.getTableId());
                String tableStateStr = isTableFree ? "FREE" : "OCCUPIED";
                
                this.orderNotificationService.notifyStateChange(
                        orderId,
                        domainOrder.getTableId(),
                        OrderNotification.OrderNotificationStatus.PAYMENT_UPDATE,
                        paymentStatus.name(),
                        paymentStatus.name(),
                        tableStateStr,
                        "No payment register because already fully paid."
                );
                return domainOrder;
            }
            // Enregistrer le paiement
            DomainOrder updatedOrder = this.orderPort.recordPayment(
                    orderId,
                    amount,
                    paymentMethod,
                    connectedUser.getEmployeeNumber(),
                    notes,
                    discountAmount
            );

            // Notifier du changement de statut de paiement
            boolean isTableFree = this.orderPort.isTableFree(domainOrder.getTableId());
            String tableStateStr = isTableFree ? "FREE" : "OCCUPIED";
            
            this.orderNotificationService.notifyStateChange(
                    orderId,
                    domainOrder.getTableId(),
                    OrderNotification.OrderNotificationStatus.PAYMENT_UPDATE,
                    paymentStatus.name(),
                    updatedOrder.getPaymentStatus().name(),
                    tableStateStr
            );

            return updatedOrder;
        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error recording payment for order [{}]: {}",
                    errorId, orderId, e.getMessage(), e);
            throw new ServiceException(errorId, "An internal error occurred");
        }
    }

    /**
     * Récupère l'historique des paiements pour une commande
     */
    public List<DomainOrder.DomainPaymentTransaction> getPaymentHistory(Long orderId) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        // Vérifier que la commande appartient au tenant
        if (!this.orderPort.isOrderBelongToTenant(orderId, connectedUser.getTenantCode())) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] try to get payment history for order [{}] of other tenant code [{}]",
                    errorId, connectedUser.getEmployeeNumber(), orderId, connectedUser.getTenantCode());
            throw new ServiceException(errorId, "A problem occurred retrieving payment history.");
        }

        try {
            return this.orderPort.getPaymentHistory(orderId);
        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error getting payment history for order [{}]: {}",
                    errorId, orderId, e.getMessage(), e);
            throw new ServiceException(errorId, "An internal error occurred");
        }
    }

    /**
     * Récupère les commandes par statut de paiement
     */
    public List<DomainOrder> getOrdersByPaymentStatus(OrderPaymentStatus status) throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        try {
            return this.orderPort.getOrdersByPaymentStatus(status, connectedUser.getTenantCode());
        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error getting orders by payment status [{}]: {}",
                    errorId, status, e.getMessage(), e);
            throw new ServiceException(errorId, "An internal error occurred");
        }
    }

    /**
     * Récupère les commandes nécessitant un paiement
     */
    public List<DomainOrder> getOrdersRequiringPayment() throws ServiceException {
        ConnectedUser connectedUser = this.userAppService.getConnectedUser();

        try {
            return this.orderPort.getOrdersRequiringPayment(connectedUser.getTenantCode());
        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error getting orders requiring payment: {}",
                    errorId, e.getMessage(), e);
            throw new ServiceException(errorId, "An internal error occurred");
        }
    }
}
