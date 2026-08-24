package com.jonatan.challenge.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

public class LiverpoolHomePage {

    private final Page page;
    private final Locator searchInput;

    public LiverpoolHomePage(Page page) {
        this.page = page;

        searchInput = page.getByLabel(
                "Buscar por producto, categoría y más...",
                new Page.GetByLabelOptions()
                        .setExact(true)
        );
    }

    public void open(String baseUrl) {
        page.navigate(
                baseUrl,
                new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(60_000)
        );
    }

    public boolean isLoaded() {
        return searchInput.isVisible()
                && page.title().toLowerCase().contains("liverpool");
    }

    public void searchFor(String searchTerm) {
        searchInput.click();
        searchInput.fill("");

        searchInput.pressSequentially(
                searchTerm,
                new Locator.PressSequentiallyOptions()
                        .setDelay(50)
        );

        Locator exactSuggestion = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions()
                        .setName(searchTerm)
                        .setExact(true)
        );

        try {
            /*
             * Liverpool normalmente muestra una sugerencia.
             * Usarla representa el comportamiento habitual.
             */
            exactSuggestion.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(5_000)
            );

            exactSuggestion.click();

        } catch (TimeoutError exception) {

            searchInput.press("Enter");
        }

        page.waitForURL(
                url -> url.contains("/tienda")
                        && url.contains("s="),
                new Page.WaitForURLOptions()
                        .setTimeout(30_000)
        );
    }
}