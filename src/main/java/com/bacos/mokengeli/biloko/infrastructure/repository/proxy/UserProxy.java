package com.bacos.mokengeli.biloko.infrastructure.repository.proxy;


import com.bacos.mokengeli.biloko.application.domain.DomainTenant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@FeignClient(name = "user-service",
        configuration = com.bacos.mokengeli.biloko.config.feign.FeignClientConfig.class)
public interface UserProxy {
    @GetMapping("/api/user/tenant")
    Optional<DomainTenant> getTenantByCode(@RequestParam("code") String tenantCode);
}