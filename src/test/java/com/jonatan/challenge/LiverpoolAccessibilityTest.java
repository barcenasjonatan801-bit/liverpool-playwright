package com.jonatan.challenge;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonatan.challenge.base.BaseTest;
import com.jonatan.challenge.pages.LiverpoolHomePage;
import com.jonatan.challenge.pages.SearchResultsPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Liverpool E-commerce")
@Feature("Accesibilidad")
class LiverpoolAccessibilityTest extends BaseTest {

    private static final String SEARCH_TERM = "playstation 5";
    private static final String REQUIRED_COLOR = "Blanco";

    private static final List<String> WCAG_TAGS = List.of(
            "wcag2a",
            "wcag2aa",
            "wcag21a",
            "wcag21aa"
    );

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    @Story("Accesibilidad de los resultados de búsqueda")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName(
            "Debe analizar con axe-core la página de resultados"
    )
    void shouldGenerateAccessibilityReport()
            throws JsonProcessingException {

        Allure.parameter("searchTerm", SEARCH_TERM);
        Allure.parameter("color", REQUIRED_COLOR);
        Allure.parameter("estándar", "WCAG 2.1 A/AA");

        LiverpoolHomePage homePage =
                new LiverpoolHomePage(page);

        SearchResultsPage resultsPage =
                new SearchResultsPage(page);


        homePage.open(BASE_URL);

        assertFalse(
                !homePage.isLoaded(),
                "La página principal de Liverpool no cargó"
        );

        homePage.searchFor(SEARCH_TERM);

        resultsPage.waitUntilLoaded();
        resultsPage.filterByColor(REQUIRED_COLOR);
        resultsPage.sortByLowestPrice();
        resultsPage.waitUntilLoaded();


        AxeResults axeResults = new AxeBuilder(page)
                .withTags(WCAG_TAGS)
                .analyze();

        validateScanExecution(axeResults);
        attachEvidenceToAllure(axeResults);
    }

    private void validateScanExecution(
            AxeResults axeResults
    ) {
        assertNotNull(
                axeResults,
                "axe-core no devolvió resultados"
        );

        assertFalse(
                axeResults.isErrored(),
                "axe-core no pudo ejecutar el análisis: "
                        + axeResults.getErrorMessage()
        );

        assertNotNull(
                axeResults.getViolations(),
                "axe-core no devolvió la lista de violaciones"
        );

        assertNotNull(
                axeResults.getPasses(),
                "axe-core no devolvió las reglas aprobadas"
        );

        assertNotNull(
                axeResults.getIncomplete(),
                "axe-core no devolvió las revisiones manuales"
        );


        assertFalse(
                axeResults.getPasses().isEmpty(),
                "axe-core no ejecutó ninguna regla exitosamente"
        );
    }

    private void attachEvidenceToAllure(
            AxeResults axeResults
    ) throws JsonProcessingException {

        String summary = String.format(
                """
                URL analizada: %s
                Estándar evaluado: WCAG 2.1 A/AA
                Reglas aprobadas: %d
                Violaciones encontradas: %d
                Revisiones manuales requeridas: %d
                """,
                axeResults.getUrl(),
                axeResults.getPasses().size(),
                axeResults.getViolations().size(),
                axeResults.getIncomplete().size()
        );

        String jsonReport = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(axeResults);

        Allure.addAttachment(
                "Resumen de accesibilidad",
                "text/plain",
                summary,
                ".txt"
        );

        Allure.addAttachment(
                "Resultado completo de axe-core",
                "application/json",
                jsonReport,
                ".json"
        );
    }
}