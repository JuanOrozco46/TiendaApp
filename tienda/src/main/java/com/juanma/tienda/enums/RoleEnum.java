package com.juanma.tienda.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

public enum RoleEnum {
    ADMIN(1),
    CLIENTE(2),
    VENDEDOR(3);

    private final Integer id;
    RoleEnum(Integer id) {
        this.id = id;
    }
}