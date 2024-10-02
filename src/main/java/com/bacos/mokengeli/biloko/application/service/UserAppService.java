package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class UserAppService {

    public ConnectedUser getConnectedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (ConnectedUser) authentication.getPrincipal();
    }

    public boolean isAdminUser() {
        ConnectedUser connectedUser = getConnectedUser();
        return connectedUser.getRoles().contains("ROLE_ADMIN");
    }

}
