package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.*;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrderItem;
import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.domain.model.UpdateOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import com.bacos.mokengeli.biloko.application.service.OrderNotificationService;
import com.bacos.mokengeli.biloko.infrastructure.mapper.OrderMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.RefTableMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.TenantMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import com.bacos.mokengeli.biloko.infrastructure.repository.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.proxy.UserProxy;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderAdapter implements OrderPort {
    private final OrderRepository orderRepository;
    private final CurrencyRepository currencyRepository;
    private final TenantRepository tenantRepository;
    private final DishRepository dishRepository;
    private final RefTableRepository refTableRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserProxy userProxy;
    private final DebtValidationRepository debtValidationRepository;
    private final FinancialLossRepository financialLossRepository;
    private final OrderNotificationService orderNotificationService;


    @Autowired
    public OrderAdapter(OrderRepository orderRepository, CurrencyRepository currencyRepository,
                        TenantRepository tenantRepository, DishRepository dishRepository, RefTableRepository refTableRepository, OrderItemRepository orderItemRepository, InventoryService inventoryService, PaymentTransactionRepository paymentTransactionRepository, UserProxy userProxy, DebtValidationRepository debtValidationRepository, FinancialLossRepository financialLossRepository, OrderNotificationService orderNotificationService) {
        this.orderRepository = orderRepository;
        this.currencyRepository = currencyRepository;
        this.tenantRepository = tenantRepository;
        this.dishRepository = dishRepository;
        this.refTableRepository = refTableRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryService = inventoryService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.userProxy = userProxy;
        this.debtValidationRepository = debtValidationRepository;
        this.financialLossRepository = financialLossRepository;
        this.orderNotificationService = orderNotificationService;
    }

    @Transactional
    @Override
    public Optional<DomainOrder> createOrder(CreateOrder createOrder) throws ServiceException {
        String tenantCode = createOrder.getTenantCode();
        Optional<Tenant> optTenant = this.tenantRepository.findByCode(tenantCode);
        Tenant tenant = optTenant.orElse(null);
        if (tenant == null) {
            DomainTenant domainTenant = this.userProxy.getTenantByCode(tenantCode)
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No tenant found with code " + tenantCode));
            tenant = TenantMapper.toEntity(domainTenant);
            tenant = this.tenantRepository.save(tenant);

        }
        Currency currency = this.currencyRepository.findById(createOrder.getCurrencyId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + createOrder.getCurrencyId()));
        RefTable refTable = this.refTableRepository.findById(createOrder.getTableId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No Ref table found with the id " + createOrder.getTableId()));
        String requesterEmployeeNumber = this.getRequesterEmployeeNumber(createOrder.getRegisteredBy());
        Order order = Order.builder()
                .refTable(refTable)
                .totalPrice(0.0)
                .currency(currency)
                .paidAmount(0.0)
                .paymentStatus(OrderPaymentStatus.UNPAID)
                .tenant(tenant)
                .registeredBy(requesterEmployeeNumber)
                .createdAt(OffsetDateTime.now())
                .build();
        createAndSetOrderItems(order, currency, createOrder.getOrderItems());
        order = this.orderRepository.save(order);
        DomainOrder domainOrder = OrderMapper.toDomain(order);
        domainOrder.setWaiterName(this.getRequesterName(createOrder.getRegisteredBy()));
        return Optional.of(domainOrder);
    }

    @Override
    public Optional<List<DomainOrder>> getOrdersByState(OrderItemState orderItemState, String tenantCode) throws ServiceException {
        boolean existsByTenantCode = this.tenantRepository.existsByCode(tenantCode);

        if (!existsByTenantCode) {
            DomainTenant domainTenant = this.userProxy.getTenantByCode(tenantCode)
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No tenant  find with tenant_code=" + tenantCode));
            Tenant tenant = TenantMapper.toEntity(domainTenant);
            this.tenantRepository.save(tenant);
        }


        List<Object[]> results = orderRepository.findOrderAndItemsByTenantCodeAndItemState(tenantCode, orderItemState);
        List<DomainOrder> list = new ArrayList<>();

        Map<Order, List<OrderItem>> orderItemMap = new HashMap<>();

        for (Object[] result : results) {
            Order order = (Order) result[0];
            OrderItem orderItem = (OrderItem) result[1];

            orderItemMap.computeIfAbsent(order, k -> new ArrayList<>()).add(orderItem);
        }

        for (Map.Entry<Order, List<OrderItem>> entry : orderItemMap.entrySet()) {
            Order key = entry.getKey();
            List<OrderItem> value = entry.getValue();
            DomainOrder domainOrderWithoutItem = OrderMapper.toDomainOrderWithoutItem(key);
            domainOrderWithoutItem.setWaiterName(this.getRequesterName(domainOrderWithoutItem.getWaiterIdentifier()));
            List<DomainOrder.DomainOrderItem> orderItems = new ArrayList<>();
            for (OrderItem orderItem : value) {
                DomainOrder.DomainOrderItem ligthDomainOrderItem = OrderMapper.toDomainOrderItem(orderItem);
                orderItems.add(ligthDomainOrderItem);
            }

            domainOrderWithoutItem.setItems(orderItems);
            domainOrderWithoutItem.sortItemsByCategoryPriority();
            list.add(domainOrderWithoutItem);
        }

        return Optional.of(list);
    }

    @Override
    public boolean isRefTableBelongToTenant(String refTableName, String tenantCode) {
        return this.refTableRepository.existsByNameAndTenantCode(refTableName, tenantCode);
    }

    @Override
    public Page<DomainRefTable> getRefTablesByTenantCode(String tenantCode, int page, int size) {
        // 1) Construire le Pageable
        Pageable pageable = PageRequest.of(page, size);

        // 2) Appeler le repo paginé
        Page<RefTable> refPage =
                refTableRepository.findByTenantCode(tenantCode, pageable);

        // 3) Mapper chaque RefTable en DomainRefTable
        return refPage.map(x ->
                DomainRefTable.builder()
                        .id(x.getId())
                        .name(x.getName())
                        .build()
        );
    }

    @Override
    public boolean isOrderItemOfTenant(Long id, String tenantCode) {
        return this.orderItemRepository.isOrderItemOfTenantCode(id, tenantCode);
    }


    private void changeStateAndSave(Long id, OrderItemState orderItemState) throws ServiceException {
        OrderItem orderItem = this.orderItemRepository.findById(id).orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                "No OrderItem found with id " + id));
        orderItem.setState(orderItemState);
        this.orderItemRepository.save(orderItem);
    }

    @Override
    public void prepareOrderItem(Long id) throws ServiceException {
        OrderItem orderItem = this.orderItemRepository.findById(id).orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                "No OrderItem found with id " + id));
        Dish dish = orderItem.getDish();
        List<DishProduct> dishProducts = dish.getDishProducts();
        List<ActionArticleRequest> actionArticleRequests = new ArrayList<>();
        for (DishProduct dishProduct : dishProducts) {
            ActionArticleRequest actionArticleRequest = ActionArticleRequest.builder()
                    .productId(dishProduct.getProductId()).quantity(dishProduct.getQuantity()).build();
            actionArticleRequests.add(actionArticleRequest);
        }
        this.inventoryService.removeArticle(actionArticleRequests);
        orderItem.setState(OrderItemState.READY);
        this.orderItemRepository.save(orderItem);
    }


    @Override
    public void changeOrderItemState(Long id, OrderItemState orderItemState) throws ServiceException {
        OrderItem orderItem = this.orderItemRepository.findById(id).orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                "No OrderItem found with id " + id));
        orderItem.setState(orderItemState);
        this.orderItemRepository.save(orderItem);
    }

    @Override
    public OrderItemState getOrderItemState(Long id) throws ServiceException {
        OrderItem orderItem = this.orderItemRepository.findById(id).orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                "No OrderItem found with id " + id));
        return orderItem.getState();
    }

    @Transactional
    @Override
    public DomainRefTable createRefTable(DomainRefTable refTable) throws ServiceException {

        Optional<Tenant> optTenant = this.tenantRepository.findByCode(refTable.getTenantCode());
        Tenant tenant = optTenant.orElse(null);
        if (tenant == null) {
            DomainTenant domainTenant = this.userProxy.getTenantByCode(refTable.getTenantCode())
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No tenant found with code " + refTable.getTenantCode()));
            tenant = TenantMapper.toEntity(domainTenant);
            tenant = this.tenantRepository.save(tenant);

        }

        RefTable refT = RefTableMapper.toEntity(refTable);
        refT.setCreatedAt(OffsetDateTime.now());
        refT.setTenant(tenant);
        RefTable save = this.refTableRepository.save(refT);
        return RefTableMapper.toDomain(save);
    }

    @Override
    public List<DomainOrder> getActiveOrdersByTable(Long refTableId) {
        List<Order> orders = this.orderRepository
                .findActiveOrdersByRefTableId(refTableId);
        return orders.stream()
                .map(x -> {
                    DomainOrder domainOrder = OrderMapper.toDomain(x);
                    domainOrder.setWaiterName(this.getRequesterName(domainOrder.getWaiterIdentifier()));
                    return domainOrder;
                })
                .toList();
    }

    @Override
    public boolean isRefTableBelongToTenant(Long refTableId, String tenantCode) {
        return this.refTableRepository.existsByIdAndTenantCode(refTableId, tenantCode);

    }

    @Transactional
    @Override
    public DomainOrder addItems(UpdateOrder updateOrder) throws ServiceException {
        Order order = this.orderRepository.findById(updateOrder.getOrderId())
                .orElseThrow(() ->
                        new ServiceException(UUID.randomUUID().toString(),
                                "No Order found with id = " + updateOrder.getOrderId())
                );
        Currency currency = order.getCurrency();
        createAndSetOrderItems(order, currency, updateOrder.getOrderItems());

        order = this.orderRepository.save(order);
        return OrderMapper.toDomain(order);
    }

    @Override
    public boolean isOrderBelongToTenant(Long orderId, String tenantCode) {
        return this.orderRepository.existsByIdAndTenantCode(orderId, tenantCode);
    }

    @Override
    public Optional<DomainOrder> getOrder(Long id) {
        Optional<Order> optionalOrder = this.orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            return Optional.empty();
        }
        Order order = optionalOrder.get();
        DomainOrder domainOrder = OrderMapper.toDomain(order);
        domainOrder.setWaiterName(this.getRequesterName(domainOrder.getWaiterIdentifier()));
        return Optional.of(domainOrder);
    }

    private void createAndSetOrderItems(Order order, Currency currency, List<CreateOrderItem> orderItems) throws ServiceException {
        Double totalPrice = order.getTotalPrice();
        for (CreateOrderItem orderItem : orderItems) {
            Optional<Dish> optionalDish = this.dishRepository.findById(orderItem.getDishId());
            if (optionalDish.isEmpty()) {
                throw new ServiceException(UUID.randomUUID().toString(), "No dish found with id " + orderItem.getDishId());
            }
            Dish dish = optionalDish.get();
            for (int i = 0; i < orderItem.getCount(); i++) {
                totalPrice += dish.getPrice();
                OrderItem build = OrderItem.builder()
                        .state(OrderItemState.PENDING)
                        .note(orderItem.getNote() == null ? "" : orderItem.getNote())
                        .currency(currency)
                        .createdAt(OffsetDateTime.now())
                        .dish(dish)
                        .order(order)
                        .unitPrice(dish.getPrice())
                        .build();
                order.addItem(build);
            }
        }
        order.setTotalPrice(totalPrice);

    }

    @Transactional
    @Override
    public DomainOrder recordPayment(Long orderId, Double amount, String paymentMethod,
                                     String employeeNumber, String notes, Double discountAmount) throws ServiceException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "No Order found with id = " + orderId));
        String requesterEmployeeNumber = this.getRequesterEmployeeNumber(employeeNumber);
        // Créer la transaction de paiement
        PaymentTransaction payment = PaymentTransaction.builder()
                .order(order)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .employeeNumber(requesterEmployeeNumber)
                .notes(notes)
                .createdAt(OffsetDateTime.now())
                .isRefund(false)
                .discountAmount(discountAmount != null ? discountAmount : 0.0)
                .build();

        // Ajouter le paiement à la commande
        order.addPayment(payment);
        order.setUpdatedAt(OffsetDateTime.now());

        // Sauvegarder la commande mise à jour
        Order savedOrder = orderRepository.save(order);

        // Mapper et retourner le domaine
        return orderToDomainWithPayments(savedOrder);
    }

    @Override
    public List<DomainOrder.DomainPaymentTransaction> getPaymentHistory(Long orderId) {
        List<PaymentTransaction> payments = paymentTransactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return payments.stream()
                .map(this::paymentToDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DomainOrder refundPayment(Long paymentId, String employeeNumber, String reason) throws ServiceException {
        PaymentTransaction payment = paymentTransactionRepository.findById(paymentId)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "No Payment found with id = " + paymentId));

        Order order = payment.getOrder();
        String requesterEmployeeNumber = this.getRequesterEmployeeNumber(employeeNumber);
        // Créer une transaction de remboursement
        PaymentTransaction refund = PaymentTransaction.builder()
                .order(order)
                .amount(-payment.getAmount()) // Montant négatif pour un remboursement
                .paymentMethod(payment.getPaymentMethod())
                .employeeNumber(requesterEmployeeNumber)
                .notes("Refund for payment #" + paymentId + ": " + reason)
                .createdAt(OffsetDateTime.now())
                .isRefund(true)
                .discountAmount(0.0)
                .build();

        // Ajouter le remboursement à la commande
        order.addPayment(refund);
        order.setUpdatedAt(OffsetDateTime.now());

        // Sauvegarder la commande mise à jour
        Order savedOrder = orderRepository.save(order);

        // Mapper et retourner le domaine
        return orderToDomainWithPayments(savedOrder);
    }

    @Override
    public List<DomainOrder> getOrdersByPaymentStatus(OrderPaymentStatus status, String tenantCode) {
        List<Order> orders = orderRepository.findByPaymentStatusAndTenantCode(status, tenantCode);
        return orders.stream()
                .map(this::orderToDomainWithPayments)
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainOrder> getOrdersRequiringPayment(String tenantCode) {
        List<Order> orders = orderRepository.findByPaymentStatusInAndTenantCode(
                List.of(OrderPaymentStatus.UNPAID, OrderPaymentStatus.PARTIALLY_PAID),
                tenantCode
        );
        return orders.stream()
                .map(this::orderToDomainWithPayments)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DomainOrder> getOrderByOrderItemId(Long orderItemId) throws ServiceException {
        Order order = this.orderRepository.findByItemId(orderItemId)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "No Order found with id = " + orderItemId));
        DomainOrder domainOrder = OrderMapper.toDomain(order);
        domainOrder.setWaiterName(this.getRequesterName(domainOrder.getWaiterIdentifier()));
        return Optional.of(domainOrder);
    }

    @Override
    public boolean isTableFree(Long refTableId) {
        return this.orderRepository.isTableFree(refTableId);
    }

    @Override
    @Transactional
    public DomainCloseOrderWithDebt closeOrderWithDebt(
            DomainCloseOrderWithDebtRequest req,
            String tenantCode,
            String employeeNumber
    ) throws ServiceException {
        // 1. Récupérer la commande
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "Commande non trouvée = " + req.getOrderId()));

        // 2. Vérifier qu'elle n'est pas déjà payée
        if (order.getPaymentStatus() == OrderPaymentStatus.FULLY_PAID) {
            throw new ServiceException(UUID.randomUUID().toString(), "La commande est déjà entièrement payée");
        }

        boolean validationRequired = false;
        Long validationRequestId = null;

        // 3 et 4. Selon le type de validation
        if (ValidationType.IMMEDIATE.name().equals(req.getValidationType())) {
            // PIN requis et vérification
            if (req.getValidationCode() == null) {
                throw new ServiceException(UUID.randomUUID().toString(), "PIN requis pour validation immédiate");
            }
            if (!userProxy.isValidationPinOk(req.getValidationCode())) {
                throw new ServiceException(UUID.randomUUID().toString(), "PIN incorrect");
            }
            // création de la validation approuvée
            DebtValidation dv = DebtValidation.builder()
                    .order(order)
                    .tenantCode(tenantCode)
                    .amount(BigDecimal.valueOf(req.getAmount()))
                    .reason(req.getReason())
                    .currency(order.getCurrency())
                    .requestedBy(employeeNumber)
                    .validatedBy(employeeNumber)
                    .status(DebtValidationStatus.APPROVED)
                    .createdAt(OffsetDateTime.now())
                    .validatedAt(OffsetDateTime.now())
                    .build();
            debtValidationRepository.save(dv);

        } else {
            // REMOTE
            validationRequired = true;
            DebtValidation dv = DebtValidation.builder()
                    .order(order)
                    .tenantCode(tenantCode)
                    .amount(BigDecimal.valueOf(req.getAmount()))
                    .reason(req.getReason())
                    .currency(order.getCurrency())
                    .requestedBy(employeeNumber)
                    .status(DebtValidationStatus.PENDING)
                    .createdAt(OffsetDateTime.now())
                    .build();
            debtValidationRepository.save(dv);
            validationRequestId = dv.getId();
            /** ws.notifyManagers(tenantCode, new DebtValidationNotification(
             dv.getId(), order.getId(), order.getTable().getId(), tenantCode, req.getAmount()
             ));*/

        }

        // 5. Mise à jour de la commande et de la table
        order.setPaymentStatus(OrderPaymentStatus.CLOSED_WITH_DEBT);
        orderRepository.save(order);
        DebtValidation debtValidation = null;
        if (validationRequired) {
            Optional<DebtValidation> optDebValidation = debtValidationRepository.findById(validationRequestId);
            if (optDebValidation.isPresent()) {
                debtValidation = optDebValidation.get();
            }
        }
        // Création de la perte financière
        FinancialLoss loss = FinancialLoss.builder()
                .order(order)
                .tenantCode(tenantCode)
                .amount(BigDecimal.valueOf(req.getAmount()))
                .currency(order.getCurrency())
                .reason(req.getReason())
                .debtValidation(debtValidation)
                .createdBy(employeeNumber)
                .createdAt(OffsetDateTime.now())
                .build();
        financialLossRepository.save(loss);

        // Notification de clôture avec dette
        this.orderNotificationService.notifyDebtValidation(order.getId(), order.getRefTable().getId(),
                OrderNotification.OrderNotificationStatus.ORDER_CLOSED_WITH_DEBT,
                order.getPaymentStatus().name(),
                OrderNotification.OrderNotificationStatus.PAYMENT_UPDATE.name(),
                TableState.FREE.name(),
                "Commande clôturée avec impayé" + (validationRequired ? " - Validation requise" : ""),
                validationRequestId);

        // Notification de validation requise si applicable
        /**  if (validationRequired && validationRequestId != null) {
         this.orderNotificationService.notifyDebtValidation(order.getId(), order.getRefTable().getId(),
         OrderNotification.OrderNotificationStatus.DEBT_VALIDATION_REQUIRED,
         "PENDING", "VALIDATION_REQUIRED", TableState.FREE.name(),
         "Validation de dette requise pour cette commande",
         validationRequestId);
         }*/

        return DomainCloseOrderWithDebt.builder()
                .message("Commande clôturée avec impayé")
                .validationRequired(validationRequired)
                .validationRequestId(validationRequestId)
                .build();
    }

    // Méthode helper pour mapper une Order en DomainOrder avec paiements
    private DomainOrder orderToDomainWithPayments(Order order) {
        DomainOrder domainOrder = OrderMapper.toDomain(order);

        // Ajouter les informations de paiement
        domainOrder.setPaymentStatus(order.getPaymentStatus());
        domainOrder.setPaidAmount(order.getPaidAmount());
        domainOrder.setRemainingAmount(order.getRemainingAmount());
        domainOrder.setWaiterName(this.getRequesterName(domainOrder.getWaiterIdentifier()));

        // Mapper les transactions de paiement
        if (order.getPayments() != null) {
            List<DomainOrder.DomainPaymentTransaction> domainPayments = order.getPayments().stream()
                    .map(this::paymentToDomain)
                    .collect(Collectors.toList());
            domainOrder.setPayments(domainPayments);
        }

        return domainOrder;
    }

    // Méthode helper pour mapper une PaymentTransaction en DomainPaymentTransaction
    private DomainOrder.DomainPaymentTransaction paymentToDomain(PaymentTransaction payment) {
        String requesterEmployeeNumber = this.getRequesterEmployeeNumber(payment.getEmployeeNumber());
        return DomainOrder.DomainPaymentTransaction.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .employeeNumber(requesterEmployeeNumber)
                .notes(payment.getNotes())
                .isRefund(payment.getIsRefund())
                .discountAmount(payment.getDiscountAmount())
                .build();
    }

    @Override
    @Transactional
    public List<DomainPendingDebtValidation> getPendingDebtValidations(
            String tenantCode
    ) {
        // 1. récupérer les 50 dernières demandes pending
        List<DebtValidation> list = debtValidationRepository
                .findByTenantCodeAndStatusOrderByCreatedAtDesc(
                        tenantCode,
                        DebtValidationStatus.PENDING,
                        PageRequest.of(0, 50)
                );
        // 2. transformer en DTO


        return list.stream().map(dv -> {
            DomainPendingDebtValidation dto = new DomainPendingDebtValidation();
            dto.setId(dv.getId());
            dto.setOrderId(dv.getOrder().getId());
            RefTable table = dv.getOrder().getRefTable();
            dto.setTableId(table.getId());
            dto.setTableName(table.getName());
            dto.setAmount(dv.getAmount().doubleValue());
            dto.setCurrency(dv.getCurrency().getCode());
            dto.setReason(dv.getReason());
            dto.setServerName(getRequesterName(dv.getRequestedBy()));
            dto.setCreatedAt(dv.getCreatedAt());
            dto.setStatus(dv.getStatus().name());
            return dto;
        }).collect(Collectors.toList());
    }

    private String getRequesterName(String identifier) {
        if (Strings.isEmpty(identifier))
            return "";
        Optional<DomainUser> domainUserOptional = this.userProxy.getUserByIdentifier(identifier);
        if (domainUserOptional.isPresent()) {
            DomainUser domainUser = domainUserOptional.get();
            return domainUser.getFirstName() + " " + domainUser.getLastName();
        }
        return "";
    }

    private String getRequesterEmployeeNumber(String identifier) {
        if (Strings.isEmpty(identifier))
            return "";
        Optional<DomainUser> domainUserOptional = this.userProxy.getUserByIdentifier(identifier);
        if (domainUserOptional.isPresent()) {
            DomainUser domainUser = domainUserOptional.get();
            return domainUser.getEmployeeNumber();
        }
        return "";
    }

    @Override
    @Transactional
    public DomainValidateDebtValidation processDebtValidation(
            DomainValidateDebtValidationRequest req,
            String identifier
    ) throws ServiceException {
        // 1. Récupérer la demande PENDING
        DebtValidation dv = debtValidationRepository
                .findByIdAndStatus(req.getDebtValidationId(), DebtValidationStatus.PENDING)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "Validation request not found or already processed"));
        // 2.

        Order order = dv.getOrder();
        // 3. Traitement
        Optional<DomainUser> userByIdentifier = this.userProxy.getUserByIdentifier(identifier);
        String tenantCode = dv.getTenantCode();
        DomainUser domainUser = userByIdentifier.get();
        if (req.isApproved()) {
            // PIN manager
            if (req.getValidationCode() == null) {
                throw new ServiceException(UUID.randomUUID().toString(), "PIN required for approval");
            }
            if (!userProxy.isValidationPinOk(req.getValidationCode())) {
                throw new ServiceException(UUID.randomUUID().toString(), "Invalid PIN");
            }


            dv.setStatus(DebtValidationStatus.APPROVED);
            dv.setValidatedBy(domainUser.getEmployeeNumber());
            dv.setValidatedAt(OffsetDateTime.now());
            debtValidationRepository.save(dv);
            // confirmer la perte financière
            FinancialLoss loss = new FinancialLoss();
            loss.setOrder(order);
            loss.setTenantCode(tenantCode);
            loss.setAmount(dv.getAmount());
            loss.setCurrency(dv.getCurrency());
            loss.setReason(dv.getReason());
            loss.setDebtValidation(dv);
            loss.setCreatedBy(domainUser.getEmployeeNumber());
            loss.setCreatedAt(OffsetDateTime.now());
            financialLossRepository.save(loss);

            // Notification de validation approuvée
            this.orderNotificationService.notifyDebtValidation(order.getId(), order.getRefTable().getId(),
                    OrderNotification.OrderNotificationStatus.DEBT_VALIDATION_APPROVED,
                    "VALIDATION_PENDING", "VALIDATION_APPROVED",
                    TableState.FREE.name(),
                    "Votre demande de validation de dette a été approuvée",
                    req.getDebtValidationId());
        } else {
            // refus
            if (req.getRejectionReason() == null) {
                throw new ServiceException(UUID.randomUUID().toString(), "Rejection reason required");
            }
            dv.setStatus(DebtValidationStatus.REJECTED);
            dv.setRejectionReason(req.getRejectionReason());
            dv.setValidatedBy(domainUser.getEmployeeNumber());
            dv.setValidatedAt(OffsetDateTime.now());
            debtValidationRepository.save(dv);
            // rouvrir commande
            order.setPaymentStatus(OrderPaymentStatus.UNPAID);

            orderRepository.save(order);

            // Notification de validation refusée
            this.orderNotificationService.notifyDebtValidation(order.getId(), order.getRefTable().getId(),
                    OrderNotification.OrderNotificationStatus.DEBT_VALIDATION_REJECTED,
                    "VALIDATION_PENDING", "VALIDATION_REJECTED",
                    TableState.OCCUPIED.name(),
                    "Votre demande de validation de dette a été refusée",
                    req.getDebtValidationId());
        }
        return new DomainValidateDebtValidation(
                "Validation processed successfully", req.isApproved()
        );
    }

    @Override
    public String getDebtValidationTenantCode(Long debtValidationId) {
        return debtValidationRepository.findTenantCode(debtValidationId)
                .orElse("");
    }


    @Override
    @Transactional
    public void rejectOrReturnOrderItem(Long orderItemId, OrderItemState orderItemState) throws ServiceException {
        OrderItem orderItem = this.orderItemRepository.findById(orderItemId).orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                "No OrderItem found with id " + orderItemId));
        if (!OrderItemState.RETURNED.equals(orderItemState)
                && !OrderItemState.REJECTED.equals(orderItemState)) {
            throw new ServiceException(UUID.randomUUID().toString(), "Invalid OrderItemState");
        }
        orderItem.setState(orderItemState);

        Order order = orderItem.getOrder();
        Double totalPrice = order.getTotalPrice();
        Double unitPrice = orderItem.getUnitPrice();
        Double adjustedTotalPrice = totalPrice - unitPrice > 0 ? totalPrice - unitPrice : 0;
        order.setTotalPrice(adjustedTotalPrice);
        this.orderItemRepository.save(orderItem);
    }

    @Override
    public void forceCloseOrder(Long orderId) {
        Optional<Order> optionalOrder = this.orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            return;
        }
        Order order = optionalOrder.get();
        order.setPaymentStatus(OrderPaymentStatus.FORCED_CLOSED);
        this.orderRepository.save(order);
    }
}
