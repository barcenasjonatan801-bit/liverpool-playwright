package com.jonatan.challenge.model;

import java.math.BigDecimal;

public record Product(
        String id,
        String name,
        BigDecimal price
) {
}