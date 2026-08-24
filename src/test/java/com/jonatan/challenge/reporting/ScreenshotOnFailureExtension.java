package com.jonatan.challenge.reporting;

import com.jonatan.challenge.base.BaseTest;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayInputStream;

public final class ScreenshotOnFailureExtension
        implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }

        Object testInstance = context.getRequiredTestInstance();

        if (!(testInstance instanceof BaseTest baseTest)) {
            return;
        }

        Page currentPage = baseTest.getPage();

        if (currentPage == null || currentPage.isClosed()) {
            return;
        }

        try {
            byte[] screenshot = currentPage.screenshot(
                    new Page.ScreenshotOptions()
                            .setFullPage(true)
            );

            Allure.addAttachment(
                    "Screenshot on failure",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );

            System.out.println(
                    "Screenshot automático adjuntado al reporte Allure."
            );
        } catch (RuntimeException exception) {
            System.err.println(
                    "No fue posible capturar el screenshot: "
                            + exception.getMessage()
            );
        }
    }
}