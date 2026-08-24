package com.jonatan.challenge;

import com.jonatan.challenge.base.BaseTest;
import com.jonatan.challenge.model.Product;
import com.jonatan.challenge.network.SearchResponseCapture;
import com.jonatan.challenge.network.SearchResponseParser;
import com.jonatan.challenge.pages.LiverpoolHomePage;
import com.jonatan.challenge.pages.SearchResultsPage;
import com.jonatan.challenge.validation.ProductValidator;
import com.jonatan.challenge.validation.ValidationResult;
import io.qameta.allure.Allure;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiverpoolSearchTest extends BaseTest {

    private static final int PRODUCT_LIMIT = 5;
    private static final int MINIMUM_NETWORK_MATCHES = 3;
    private static final String REQUIRED_COLOR = "Blanco";

    @ParameterizedTest(
            name = "[{index}] búsqueda={0}"
    )
    @CsvSource({
            "'playstation 5'",
            "'xbox series x'"
    })
    void shouldValidateUiProductsAgainstNetworkResponse(
            String searchTerm
    ) {
        Allure.parameter(
                "searchTerm",
                searchTerm
        );

        Allure.parameter(
                "color",
                REQUIRED_COLOR
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
         * Parte 1: abrir la página principal de Liverpool.
         */
        homePage.open(BASE_URL);

        assertTrue(
                homePage.isLoaded(),
                "La página principal no cargó correctamente"
        );

        /*
         * El listener de red se registra antes de buscar.
         * try-with-resources garantiza que sea eliminado
         * antes de que BaseTest cierre el contexto.
         */
        try (SearchResponseCapture responseCapture =
                     new SearchResponseCapture(page)) {

            homePage.searchFor(searchTerm);

            resultsPage.waitUntilLoaded();
            resultsPage.filterByColor(REQUIRED_COLOR);
            resultsPage.sortByLowestPrice();

            /*
             * Extraer los primeros cinco productos
             * mostrados en la interfaz.
             */
            List<Product> uiProducts =
                    resultsPage.getFirstProducts(PRODUCT_LIMIT);

            assertEquals(
                    PRODUCT_LIMIT,
                    uiProducts.size(),
                    String.format(
                            "Deben extraerse cinco productos para "
                                    + "la búsqueda '%s' y color '%s'",
                            searchTerm,
                            REQUIRED_COLOR
                    )
            );

            printUiProducts(
                    searchTerm,
                    REQUIRED_COLOR,
                    uiProducts
            );

            assertProductsAreSorted(uiProducts);

            /*
             * Parte 2: obtener la respuesta interceptada
             * y extraer sus productos.
             */
            String responseBody =
                    responseCapture.getLatestBody();

            List<Product> networkProducts =
                    responseParser.parseProducts(responseBody);

            assertFalse(
                    networkProducts.isEmpty(),
                    "La respuesta interceptada no contiene productos"
            );

            /*
             * Comparar los productos de la interfaz
             * contra los productos del servicio.
             */
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
             * Requisito del challenge:
             * al menos tres de los cinco productos UI
             * deben aparecer en la respuesta interceptada.
             */
            assertTrue(
                    validationResult.matchedProducts()
                            >= MINIMUM_NETWORK_MATCHES,
                    String.format(
                            "Para '%s' se esperaban al menos %d "
                                    + "coincidencias, pero se encontraron %d",
                            searchTerm,
                            MINIMUM_NETWORK_MATCHES,
                            validationResult.matchedProducts()
                    )
            );
        }
    }

    private void printUiProducts(
            String searchTerm,
            String color,
            List<Product> products
    ) {
        System.out.println();

        System.out.println(
                "Búsqueda: " + searchTerm
                        + " | Color: " + color
        );

        System.out.println("Primeros 5 productos:");
        System.out.println("--------------------");

        for (int index = 0;
             index < products.size();
             index++) {

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
        for (int index = 1;
             index < products.size();
             index++) {

            Product previousProduct =
                    products.get(index - 1);

            Product currentProduct =
                    products.get(index);

            assertTrue(
                    previousProduct.price()
                            .compareTo(
                                    currentProduct.price()
                            ) <= 0,
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
                "Respuesta interceptada: "
                        + responseUrl
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
            System.out.println(
                    "Discrepancias: ninguna"
            );
            return;
        }

        System.out.println("Discrepancias:");

        for (String discrepancy
                : validationResult.discrepancies()) {

            System.out.println(
                    " - " + discrepancy
            );
        }
    }
}