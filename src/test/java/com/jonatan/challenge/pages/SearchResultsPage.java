package com.jonatan.challenge.pages;

import com.jonatan.challenge.model.Product;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsPage {

    private static final Pattern PRICE_PATTERN =
            Pattern.compile("\\$\\s*([\\d,]+)(?:\\.(\\d{2}))?");

    private final Page page;
    private final Locator productCards;

    public SearchResultsPage(Page page) {
        this.page = page;

        productCards = page.locator(
                "main a[data-testid$='-card-card-link']"
        );
    }

    public void waitUntilLoaded() {
        productCards.first().waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(60_000)
        );
    }

    public void filterByColor(String color) {
        String escapedColor = color
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        Locator colorCheckbox = page.locator(
                "input[type='checkbox'][value^=\""
                        + escapedColor
                        + "~~\"]"
        );

        colorCheckbox.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(30_000)
        );

        String previousUrl = page.url();

        colorCheckbox.click();

        page.waitForURL(
                url -> !url.equals(previousUrl),
                new Page.WaitForURLOptions()
                        .setTimeout(30_000)
        );

        if (!colorCheckbox.isChecked()) {
            throw new IllegalStateException(
                    "El filtro de color no quedó seleccionado: " + color
            );
        }
    }

    public void sortByLowestPrice() {
        String previousUrl = page.url();

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Ordenar por:")
                        .setExact(true)
        ).click();

        page.getByRole(
                AriaRole.OPTION,
                new Page.GetByRoleOptions()
                        .setName("Menor precio")
                        .setExact(true)
        ).click();

        page.waitForURL(
                url -> !url.equals(previousUrl)
                        && url.contains("sort=sortPrice"),
                new Page.WaitForURLOptions()
                        .setTimeout(30_000)
        );
    }

    public List<Product> getFirstProducts(int limit) {
        waitUntilLoaded();

        int availableProducts = productCards.count();

        if (availableProducts < limit) {
            throw new IllegalStateException(
                    "Se esperaban al menos " + limit
                            + " productos, pero solamente aparecieron "
                            + availableProducts
            );
        }

        List<Product> products = new ArrayList<>();

        for (int index = 0; index < limit; index++) {
            Locator card = productCards.nth(index);

            String testId = card.getAttribute("data-testid");
            String id = extractProductId(testId);

            String name = card.locator("h3")
                    .innerText()
                    .trim();

            Locator priceContainer = card.locator(
                    "[data-testid$='-price']"
            );

            String priceText = priceContainer
                    .locator(":scope > span > span")
                    .first()
                    .textContent();

            products.add(
                    new Product(
                            id,
                            name,
                            parsePrice(priceText)
                    )
            );
        }

        return products;
    }

    private String extractProductId(String testId) {
        if (testId == null || testId.isBlank()) {
            throw new IllegalStateException(
                    "La tarjeta no contiene data-testid"
            );
        }

        return testId.replace("-card-card-link", "");
    }

    private BigDecimal parsePrice(String priceText) {
        Matcher matcher = PRICE_PATTERN.matcher(priceText);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "No se pudo interpretar el precio: " + priceText
            );
        }

        String integerPart = matcher.group(1)
                .replace(",", "");

        String decimalPart = matcher.group(2);

        if (decimalPart == null) {
            decimalPart = "00";
        }

        return new BigDecimal(
                integerPart + "." + decimalPart
        );
    }
}