package com.jonatan.challenge.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonatan.challenge.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class SearchResponseParser {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public List<Product> parseProducts(String responseBody) {
        try {
            JsonNode root =
                    objectMapper.readTree(responseBody);

            JsonNode productsNode =
                    findProductsNode(root);

            if (!productsNode.isArray()) {
                throw new IllegalStateException(
                        "La respuesta no contiene un arreglo products"
                );
            }

            List<Product> products =
                    new ArrayList<>();

            for (JsonNode productNode : productsNode) {
                String id = readText(
                        productNode,
                        "id"
                );

                if (id.isBlank()) {
                    continue;
                }

                String name = readText(
                        productNode,
                        "recordTitle"
                );

                if (name.isBlank()) {
                    name = readText(
                            productNode,
                            "title"
                    );
                }

                BigDecimal price =
                        extractProductPrice(productNode);

                products.add(
                        new Product(
                                id,
                                name,
                                price
                        )
                );
            }

            return products;

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudo interpretar el JSON "
                            + "de productos de Liverpool",
                    exception
            );
        }
    }

    private JsonNode findProductsNode(JsonNode root) {
        JsonNode products = root.path("products");

        if (products.isArray()) {
            return products;
        }

        products = root.path("initialResults")
                .path("products");

        if (products.isArray()) {
            return products;
        }

        products = root.findValue("products");

        if (products != null && products.isArray()) {
            return products;
        }

        throw new IllegalStateException(
                "No se encontró products en la respuesta"
        );
    }

    private String readText(
            JsonNode productNode,
            String fieldName
    ) {
        JsonNode value = productNode.get(fieldName);

        if (value == null || value.isNull()) {
            return "";
        }

        return value.asText("").trim();
    }

    private BigDecimal extractProductPrice(
            JsonNode productNode
    ) {
        /*
         * Primero buscamos precios mínimos calculados
         * directamente por el servicio.
         */
        String[] minimumPriceFields = {
                "minimumPromoPrice",
                "minimumSalePrice",
                "minimumPrice"
        };

        for (String field : minimumPriceFields) {
            BigDecimal price = readPositiveDecimal(
                    productNode.get(field)
            );

            if (price != null) {
                return price;
            }
        }

        /*
         * Si no existen, calculamos el menor precio
         * efectivo entre las variantes.
         */
        JsonNode variants = productNode.path("variants");

        BigDecimal lowestVariantPrice = null;

        if (variants.isArray()) {
            for (JsonNode variant : variants) {
                JsonNode prices = variant.path("prices");

                BigDecimal effectivePrice =
                        extractEffectiveVariantPrice(prices);

                if (effectivePrice != null
                        && (lowestVariantPrice == null
                        || effectivePrice.compareTo(
                        lowestVariantPrice
                ) < 0)) {

                    lowestVariantPrice = effectivePrice;
                }
            }
        }

        if (lowestVariantPrice != null) {
            return lowestVariantPrice;
        }

        /*
         * Último respaldo: precio de lista mínimo.
         */
        String[] listPriceFields = {
                "minimumListPrice",
                "listPrice"
        };

        for (String field : listPriceFields) {
            BigDecimal price = readPositiveDecimal(
                    productNode.get(field)
            );

            if (price != null) {
                return price;
            }
        }

        /*
         * null permite que el validador reporte
         * que la API no proporcionó un precio.
         */
        return null;
    }

    private BigDecimal extractEffectiveVariantPrice(
            JsonNode prices
    ) {
        String[] effectivePriceFields = {
                "promoPrice",
                "salePrice",
                "listPrice"
        };

        for (String field : effectivePriceFields) {
            BigDecimal price = readPositiveDecimal(
                    prices.get(field)
            );

            if (price != null) {
                return price;
            }
        }

        return null;
    }

    private BigDecimal readPositiveDecimal(
            JsonNode value
    ) {
        if (value == null || value.isNull()) {
            return null;
        }

        try {
            BigDecimal number;

            if (value.isNumber()) {
                number = value.decimalValue();
            } else {
                String normalized = value.asText()
                        .replace("$", "")
                        .replace(",", "")
                        .trim();

                number = new BigDecimal(normalized);
            }

            return number.compareTo(BigDecimal.ZERO) > 0
                    ? number
                    : null;

        } catch (NumberFormatException exception) {
            return null;
        }
    }
}