package com.jonatan.challenge.validation;

import java.util.List;

public record ValidationResult(
        int matchedProducts,
        List<String> discrepancies
) {

    public ValidationResult {
        discrepancies = List.copyOf(discrepancies);
    }
}