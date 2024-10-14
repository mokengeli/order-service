package com.bacos.mokengeli.biloko.infrastructure.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TenantContextCategoryId implements Serializable {

    private Long tenantContext;
    private Long category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantContextCategoryId that = (TenantContextCategoryId) o;
        return Objects.equals(tenantContext, that.tenantContext) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantContext, category);
    }
}
