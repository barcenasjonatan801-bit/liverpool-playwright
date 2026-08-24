package com.jonatan.challenge.validation;

import com.jonatan.challenge.model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProductValidator {

    public ValidationResult validate(
            List<Product> uiProducts,
            List<Product> networkProducts
    ) {
        Map<String, Product> networkProductsById =
                createProductsByIdMap(networkProducts);

        List<String> discrepancies =
                new ArrayList<>();

        int matchedProducts = 0;

        for (Product uiProduct : uiProducts) {
            Product networkProduct =
                    networkProductsById.get(uiProduct.id());

            if (networkProduct == null) {
                discrepancies.add(
                        String.format(
                                "Producto ausente en API "
                                        + "| id=%s | UI=%s",
                                uiProduct.id(),
                                uiProduct.name()
                        )
                );

                continue;
            }

            /*
             * El producto cuenta como coincidencia porque
             * su identificador aparece en ambas capas.
             */
            matchedProducts++;

            compareNames(
                    uiProduct,
                    networkProduct,
                    discrepancies
            );

            comparePrices(
                    uiProduct,
                    networkProduct,
                    discrepancies
            );
        }

        return new ValidationResult(
                matchedProducts,
                discrepancies
        );
    }

    private Map<String, Product> createProductsByIdMap(
            List<Product> products
    ) {
        Map<String, Product> productsById =
                new LinkedHashMap<>();

        for (Product product : products) {
            productsById.putIfAbsent(
                    product.id(),
                    product
            );
        }

        return productsById;
    }

    private void compareNames(
            Product uiProduct,
            Product networkProduct,
            List<String> discrepancies
    ) {
        String normalizedUiName =
                normalizeName(uiProduct.name());

        String normalizedNetworkName =
                normalizeName(networkProduct.name());

        if (!normalizedUiName.equals(
                normalizedNetworkName
        )) {
            discrepancies.add(
                    String.format(
                            "Nombre diferente | id=%s "
                                    + "| UI=\"%s\" "
                                    + "| API=\"%s\"",
                            uiProduct.id(),
                            uiProduct.name(),
                            networkProduct.name()
                    )
            );
        }
    }

    private void comparePrices(
            Product uiProduct,
            Product networkProduct,
            List<String> discrepancies
    ) {
        if (networkProduct.price() == null) {
            discrepancies.add(
                    String.format(
                            "Precio ausente en API "
                                    + "| id=%s "
                                    + "| UI=$%s",
                            uiProduct.id(),
                            uiProduct.price()
                    )
            );

            return;
        }

        if (uiProduct.price().compareTo(
                networkProduct.price()
        ) != 0) {
            discrepancies.add(
                    String.format(
                            "Precio diferente | id=%s "
                                    + "| UI=$%s "
                                    + "| API=$%s",
                            uiProduct.id(),
                            uiProduct.price(),
                            networkProduct.price()
                    )
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}