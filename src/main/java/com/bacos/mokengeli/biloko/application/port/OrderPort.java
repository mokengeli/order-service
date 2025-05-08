package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.domain.model.UpdateOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface OrderPort {
    Optional<DomainOrder> createOrder(CreateOrder order) throws ServiceException;

    Optional<List<DomainOrder>> getOrdersByState(OrderItemState orderState, String tenantCode) throws ServiceException;

    boolean isRefTableBelongToTenant(String refTableName, String tenantCode);

    Page<DomainRefTable> getRefTablesByTenantCode(String tenantCode, int page, int size);

    boolean isOrderItemOfTenant(Long id, String tenantCode);

    void prepareOrderItem(Long id) throws ServiceException;

    void changeOrderItemState(Long id, OrderItemState orderItemState) throws ServiceException;

    OrderItemState getOrderItemState(Long id) throws ServiceException;

    DomainRefTable createRefTable(DomainRefTable refTable) throws ServiceException;

    List<DomainOrder> getActiveOrdersByTable(Long refTableId);

    boolean isRefTableBelongToTenant(Long refTableId, String tenantCode);

    DomainOrder addItems(UpdateOrder order) throws ServiceException;

    boolean isOrderBelongToTenant(Long orderId, String tenantCode);

    Optional<DomainOrder> getOrder(Long id);

    /**
     * Enregistre un paiement pour une commande
     */
    DomainOrder recordPayment(Long orderId, Double amount, String paymentMethod,
                              String employeeNumber, String notes, Double discountAmount) throws ServiceException;

    /**
     * Récupère l'historique des paiements pour une commande
     */
    List<DomainOrder.DomainPaymentTransaction> getPaymentHistory(Long orderId);

    /**
     * Annule un paiement (remboursement)
     */
    DomainOrder refundPayment(Long paymentId, String employeeNumber, String reason) throws ServiceException;

    /**
     * Récupère les commandes par statut de paiement
     */
    List<DomainOrder> getOrdersByPaymentStatus(OrderPaymentStatus status, String tenantCode);

    /**
     * Récupère les commandes qui nécessitent un paiement (partiellement ou non payées)
     */
    List<DomainOrder> getOrdersRequiringPayment(String tenantCode);

    Optional<DomainOrder> getOrderByOrderItemId(Long orderItemId) throws ServiceException;

    boolean isTableFree(Long refTableId);


}
