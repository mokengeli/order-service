package com.bacos.mokengeli.biloko;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class IdsDto {
    private List<Long> ids;
}
