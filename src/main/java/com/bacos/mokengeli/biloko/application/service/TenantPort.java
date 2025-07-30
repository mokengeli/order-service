package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.exception.ServiceException;

public interface TenantPort {
    String getTenantName(String tenantCode) throws ServiceException;
}
