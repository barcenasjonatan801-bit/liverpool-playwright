package com.jonatan.challenge.network;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.concurrent.atomic.AtomicReference;

public final class SearchResponseCapture {

    private static final String SEARCH_ENDPOINT =
            "/web-bff/product/search";

    private final AtomicReference<Response> latestResponse =
            new AtomicReference<>();

    public SearchResponseCapture(Page page) {
        page.onResponse(this::captureSearchResponse);
    }

    private void captureSearchResponse(Response response) {
        String contentType =
                response.headerValue("content-type");

        boolean isSearchEndpoint =
                response.url().contains(SEARCH_ENDPOINT);

        boolean isJson =
                contentType != null
                        && contentType.contains("application/json");

        if (response.ok()
                && isSearchEndpoint
                && isJson) {

            /*
             * Cada búsqueda, filtro u ordenamiento reemplaza
             * la respuesta anterior. Al final conservamos la
             * correspondiente al estado final de la UI.
             */
            latestResponse.set(response);
        }
    }

    public String getLatestBody() {
        Response response = getLatestResponse();

        response.finished();

        return response.text();
    }

    public String getLatestUrl() {
        return getLatestResponse().url();
    }

    private Response getLatestResponse() {
        Response response = latestResponse.get();

        if (response == null) {
            throw new IllegalStateException(
                    "No se interceptó la respuesta de búsqueda: "
                            + SEARCH_ENDPOINT
            );
        }

        return response;
    }
}