package com.jonatan.challenge.network;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class SearchResponseCapture implements AutoCloseable {

    private static final String SEARCH_ENDPOINT =
            "/web-bff/product/search";

    private final Page page;
    private final Consumer<Response> responseListener;
    private final AtomicReference<Response> latestResponse =
            new AtomicReference<>();

    public SearchResponseCapture(Page page) {
        this.page = Objects.requireNonNull(page);
        this.responseListener = this::captureSearchResponse;

        page.onResponse(responseListener);
    }

    private void captureSearchResponse(Response response) {
        String url = response.url();
        int status = response.status();

        if (url.contains(SEARCH_ENDPOINT)
                && status >= 200
                && status < 300) {
            latestResponse.set(response);
        }
    }

    public String getLatestBody() {
        Response response = requireLatestResponse();

        response.finished();

        return response.text();
    }

    public String getLatestUrl() {
        return requireLatestResponse().url();
    }

    private Response requireLatestResponse() {
        Response response = latestResponse.get();

        if (response == null) {
            throw new IllegalStateException(
                    "No se capturó una respuesta exitosa de "
                            + SEARCH_ENDPOINT
            );
        }

        return response;
    }

    @Override
    public void close() {
        page.offResponse(responseListener);
    }
}