package com.jonatan.challenge;

import com.jonatan.challenge.base.BaseTest;
import com.jonatan.challenge.model.Product;
import com.jonatan.challenge.network.SearchResponseCapture;
import com.jonatan.challenge.network.SearchResponseParser;
import com.jonatan.challenge.pages.LiverpoolHomePage;
import com.jonatan.challenge.pages.SearchResultsPage;
import com.jonatan.challenge.validation.ProductValidator;
import com.jonatan.challenge.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiverpoolSearchTest extends BaseTest {

    private static final int PRODUCT_LIMIT = 5;
    private static final int MINIMUM_NETWORK_MATCHES = 3;

    @Test
    void shouldValidateUiProductsAgainstNetworkResponse() {
        String searchTerm = System.getProperty(
                "searchTerm",
                "playstation 5"
        );

        String color = System.getProperty(
                "color",
                "Blanco"
        );

        LiverpoolHomePage homePage =
                new LiverpoolHomePage(page);

        SearchResultsPage resultsPage =
                new SearchResultsPage(page);

        SearchResponseParser responseParser =
                new SearchResponseParser();

        ProductValidator productValidator =
                new ProductValidator();

        /*
         * Parte 1: abrir Liverpool.
         */
        homePage.open(BASE_URL);

        assertTrue(
                homePage.isLoaded(),
                "La página principal no cargó correctamente"
        );

        /*
         * El try-with-resources garantiza que el listener
         * se retire antes de que BaseTest cierre el contexto.
         */
        try (SearchResponseCapture responseCapture =
                     new SearchResponseCapture(page)) {

            /*
             * La captura ya está registrada antes de buscar,
             * filtrar y ordenar.
             */
            homePage.searchFor(searchTerm);

            resultsPage.waitUntilLoaded();
            resultsPage.filterByColor(color);
            resultsPage.sortByLowestPrice();

            /*
             * Extraer los primeros cinco productos de la UI.
             */
            List<Product> uiProducts =
                    resultsPage.getFirstProducts(PRODUCT_LIMIT);

            assertEquals(
                    PRODUCT_LIMIT,
                    uiProducts.size(),
                    "Deben extraerse exactamente cinco productos"
            );

            printUiProducts(uiProducts);
            assertProductsAreSorted(uiProducts);

            /*
             * Parte 2: obtener y procesar la última respuesta
             * de /web-bff/product/search.
             */
            String responseBody =
                    responseCapture.getLatestBody();

            List<Product> networkProducts =
                    responseParser.parseProducts(responseBody);

            assertFalse(
                    networkProducts.isEmpty(),
                    "La respuesta interceptada no contiene productos"
            );

            ValidationResult validationResult =
                    productValidator.validate(
                            uiProducts,
                            networkProducts
                    );

            printValidationResult(
                    responseCapture.getLatestUrl(),
                    networkProducts,
                    validationResult
            );

            /*
             * Requisito principal de la task:
             * al menos 3 de los 5 productos UI deben aparecer
             * en la respuesta consumida por el frontend.
             */
            assertTrue(
                    validationResult.matchedProducts()
                            >= MINIMUM_NETWORK_MATCHES,
                    String.format(
                            "Se esperaban al menos %d coincidencias, "
                                    + "pero solamente se encontraron %d",
                            MINIMUM_NETWORK_MATCHES,
                            validationResult.matchedProducts()
                    )
            );
        }
    }

    private void printUiProducts(
            List<Product> products
    ) {
        System.out.println();
        System.out.println("Primeros 5 productos:");
        System.out.println("--------------------");

        for (int index = 0; index < products.size(); index++) {
            Product product = products.get(index);

            System.out.printf(
                    "%d. [%s] %s - $%s%n",
                    index + 1,
                    product.id(),
                    product.name(),
                    product.price()
            );
        }
    }

    private void assertProductsAreSorted(
            List<Product> products
    ) {
        for (int index = 1; index < products.size(); index++) {
            Product previousProduct =
                    products.get(index - 1);

            Product currentProduct =
                    products.get(index);

            assertTrue(
                    previousProduct.price()
                            .compareTo(currentProduct.price()) <= 0,
                    String.format(
                            "Los productos no están ordenados: "
                                    + "$%s aparece antes que $%s",
                            previousProduct.price(),
                            currentProduct.price()
                    )
            );
        }
    }

    private void printValidationResult(
            String responseUrl,
            List<Product> networkProducts,
            ValidationResult validationResult
    ) {
        System.out.println();
        System.out.println("Validación UI vs API:");
        System.out.println("--------------------");
        System.out.println(
                "Respuesta interceptada: " + responseUrl
        );
        System.out.println(
                "Productos encontrados en API: "
                        + networkProducts.size()
        );
        System.out.println(
                "Coincidencias UI/API: "
                        + validationResult.matchedProducts()
                        + "/"
                        + PRODUCT_LIMIT
        );

        if (validationResult.discrepancies().isEmpty()) {
            System.out.println("Discrepancias: ninguna");
            return;
        }

        System.out.println("Discrepancias:");

        for (String discrepancy
                : validationResult.discrepancies()) {

            System.out.println(" - " + discrepancy);
        }
    }
}