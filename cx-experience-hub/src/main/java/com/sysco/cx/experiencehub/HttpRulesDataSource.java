package com.sysco.cx.experiencehub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Default RulesDataSource: calls the central cx-experience-hub-service over HTTP.
 */
public class HttpRulesDataSource implements RulesDataSource {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpRulesDataSource(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    @Override
    public FeatureConfig fetch(ExperienceContext ctx) {
        String url = baseUrl + "/api/rules?"
                + "userId="     + enc(ctx.userId())
                + "&siteId="    + enc(ctx.siteId())
                + "&accountId=" + enc(ctx.accountId())
                + "&experience="+ enc(ctx.experience());

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .header("Accept", "application/json")
                .GET().build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("hub HTTP " + resp.statusCode() + ": " + resp.body());
            }
            Map<String, Object> map = mapper.readValue(resp.body(), new TypeReference<>() {});
            return new FeatureConfig(map);
        } catch (Exception e) {
            throw new RuntimeException("cx-experience-hub HTTP fetch failed", e);
        }
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
