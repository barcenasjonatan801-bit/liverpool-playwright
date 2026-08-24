package com.jonatan.challenge;

import com.jonatan.challenge.base.BaseTest;
import com.jonatan.challenge.pages.LiverpoolHomePage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LiverpoolSmokeTest extends BaseTest {

    @Test
    void shouldOpenLiverpoolHomePage() {
        LiverpoolHomePage homePage =
                new LiverpoolHomePage(page);

        homePage.open(BASE_URL);

        System.out.println("Título: " + page.title());
        System.out.println("URL final: " + page.url());

        assertTrue(
                homePage.isLoaded(),
                "La página principal de Liverpool no cargó correctamente"
        );

    }
}