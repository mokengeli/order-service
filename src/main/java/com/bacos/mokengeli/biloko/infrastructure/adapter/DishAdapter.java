package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.IdsDto;
import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainDishProduct;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DishAdapter implements DishPort {

    private final DishRepository dishRepository;
    private final TenantContextRepository tenantContextRepository;
    private final DishProductRepository dishProductRepository;
    private final DishCategoryRepository dishCategoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public DishAdapter(DishRepository dishRepository, TenantContextRepository tenantContextRepository, DishProductRepository dishProductRepository, DishCategoryRepository dishCategoryRepository, ProductRepository productRepository, CategoryRepository categoryRepository, CurrencyRepository currencyRepository) {
        this.dishRepository = dishRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishProductRepository = dishProductRepository;
        this.dishCategoryRepository = dishCategoryRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    @Override
    public DomainDish createDish(DomainDish domainDish) throws ServiceException {
        List<DomainDishProduct> domainDishProducts = domainDish.getDishProducts();
        if (domainDishProducts == null || domainDishProducts.isEmpty()) {
            throw new ServiceException(UUID.randomUUID().toString(), "The compostion of the dish must be provided");
        }
        DomainCurrency domainCurrency = domainDish.getCurrency();
        if (domainCurrency == null) {
            throw new ServiceException(UUID.randomUUID().toString(), "The currency must be provided");
        }
        Currency currency = this.currencyRepository.findById(domainCurrency.getId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + domainCurrency.getId()));

        Dish dish = DishMapper.toEntity(domainDish);
        String tenantCode = domainDish.getTenantCode();
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode));
        dish.setTenantContext(tenantContext);
        dish.setCurrency(currency);
        dish.setCreatedAt(LocalDateTime.now());
        dish = dishRepository.save(dish);

        List<DishProduct> dishProducts = createDishProducts(domainDish.getDishProducts(), dish);
        this.dishProductRepository.saveAll(dishProducts);
        List<String> categories = domainDish.getCategories();
        if (categories != null && !categories.isEmpty()) {
            List<DishCategory> dishCategories = createDishCategories(categories, dish);
            this.dishCategoryRepository.saveAll(dishCategories);
        }

        DomainDish domainDish1 = DishMapper.toDomain(dish);
        domainDish1.setCategories(categories);
        return domainDish1;
    }

    private List<DishProduct> createDishProducts(List<DomainDishProduct> domainDishProducts, Dish dish) throws ServiceException {
        List<DishProduct> dishProducts = new ArrayList<>();
        for (DomainDishProduct domainDishProduct : domainDishProducts) {
            Product product = productRepository.findById(domainDishProduct.getProductId())
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No article found with the given id " + domainDishProduct.getProductId()));

            DishProduct dishProduct = DishProduct.builder()
                    .dish(dish)
                    .productId(product.getId())
                    .quantity(domainDishProduct.getQuantity())
                    .build();

            dishProducts.add(dishProduct);
        }
        return dishProducts;
    }

    private List<DishCategory> createDishCategories(List<String> categories, Dish dish) throws ServiceException {
        List<DishCategory> dishCategories = new ArrayList<>();
        for (String name : categories) {
            Category category = this.categoryRepository.findByName(name)
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No category found with the name " + name));

            DishCategory dishCategory = DishCategory.builder()
                    .dish(dish)
                    .category(category)
                    .build();

            dishCategories.add(dishCategory);
        }
        return dishCategories;
    }

    @Override
    public Optional<List<DomainDish>> findAllDishesByTenant(String tenantCode) {
        Optional<List<Dish>> optDish = dishRepository.findByTenantContextTenantCode(tenantCode);
        if (optDish.isEmpty()) {
            return Optional.empty();
        }
        List<DomainDish> domainDishes = optDish.get().stream().map(DishMapper::toDomain).toList();
        return Optional.of(domainDishes);
    }

    @Override
    public boolean isAllDishesOfTenant(String tenantCode, List<Long> dishIds) {
        return this.dishRepository.isAllDishesOfTenant(tenantCode, dishIds, dishIds.size());
    }

    @Override
    public Optional<DomainDish> getDish(Long id) throws ServiceException {
        Dish dish = this.dishRepository.findById(id).orElseThrow(
                () -> new ServiceException(UUID.randomUUID().toString(), "No dish found with id " + id)
        );
        List<DishProduct> dishProducts = dish.getDishProducts();
        List<Long> ids = dishProducts.stream().map(DishProduct::getProductId)
                .toList();

        Optional<List<Product>> products = this.productRepository.findByIds(ids);
        DomainDish domainDish = DishMapper.toDomain(dish);
        List<DomainDishProduct> domainDishProducts = new ArrayList<>();
        products.ifPresent(productList -> productList.forEach(product -> {
           Double quantity =  this.dishProductRepository.getQuantityByProductIdAndDishId(product.getId(),
                   dish.getId());
            domainDishProducts.add(DomainDishProduct.builder().productId(product.getId()).productName(product.getName())
                    .unitOfMeasure(product.getUnitOfMeasure())
                            .quantity(quantity)
                    .build());
        }));
        domainDish.setDishProducts(domainDishProducts);
        return Optional.of(domainDish);
    }
}
