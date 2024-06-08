package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CrptApi {
    private final int requestLimit;
    private final long timeUnitMillis;
    private int requestCount = 0;
    private long startTime;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Request limit must be positive");
        }
        this.requestLimit = requestLimit;
        this.timeUnitMillis = timeUnit.toMillis(1);
        this.startTime = System.currentTimeMillis();
    }

    private synchronized void waitIfLimitExceeded() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime > timeUnitMillis) {
            startTime = currentTime;
            requestCount = 0;
        }
        while (requestCount >= requestLimit) {
            long waitTime = timeUnitMillis - (currentTime - startTime);
            if (waitTime > 0) {
                wait(waitTime);
            }
            currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeUnitMillis) {
                startTime = currentTime;
                requestCount = 0;
            }
        }
        requestCount++;
    }

    public void createDocument(Object document, String signature) throws Exception {
        synchronized (this) {
            waitIfLimitExceeded();
        }

        String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.putPOJO("document", document);
        json.put("signature", signature);
        String requestBody = mapper.writeValueAsString(json);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(url))
            .header("Content-Type", "application/json")
            //.header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.statusCode());
        }
    }

    public static void main(String[] args) {
        try {
            // Пример использования
            CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);

            // Пример документа и подписи
            Object document = new Object() {
                public String description = "example";
                public String participantInn = "string";
                public String docId = "string";
                public String docStatus = "string";
                public String docType = "LP_INTRODUCE_GOODS";
                public String productDocument = "productRequest";
                public String ownerInn = "string";
                public String producerInn = "string";
                public String productionDate = "2020-01-23";
                public String productionType = "string";
                public String certificateDocument = "certificateDocument";
                public String certificateDocumentDate = "2020-01-23";
                public String certificateDocumentNum = "string";
                public String tnVedCode = "string";
                public String uitCode = "string";
                public String uituCode = "string";
                public String regDate = "2020-01-23";
                public String regNumber = "string";
            };
            String signature = "example_signature";

            api.createDocument(document, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
