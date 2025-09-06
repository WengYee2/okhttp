package okhttp3;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class OkHttpCustomTest {

    private static OkHttpClient client;

    @BeforeAll
    static void setup() {
        client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS) // safety timeout
                .build();
    }

    @Test
    void testSimpleGetRequest() throws Exception {
        Request request = new Request.Builder()
                .url("https://httpbin.org/get")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code(), "Expected HTTP 200 OK");
            assertNotNull(response.body(), "Response body should not be null");
            assertTrue(response.body().string().contains("url"), "Response should contain 'url'");
        }
    }

    @Test
    void testPostRequestWithJsonBody() throws Exception {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\"student\":\"okhttp\"}", JSON);

        Request request = new Request.Builder()
                .url("https://httpbin.org/post")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code(), "Expected HTTP 200 OK");
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("okhttp"), "Response should contain the JSON body content");
        }
    }

    @Test
    void testCustomHeaderSent() throws Exception {
        Request request = new Request.Builder()
                .url("https://httpbin.org/headers")
                .addHeader("X-Test-Header", "JUnit5")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            String responseBody = response.body().string();
            assertTrue(responseBody.contains("JUnit5"), "Response should contain custom header");
        }
    }

    @Test
    void testTimeoutForUnreachableServer() {
        OkHttpClient shortTimeoutClient = client.newBuilder()
                .connectTimeout(1, TimeUnit.MILLISECONDS) // impossible to succeed
                .build();

        Request request = new Request.Builder()
                .url("http://10.255.255.1/") // non-routable IP
                .build();

        Exception exception = assertThrows(Exception.class, () -> {
            shortTimeoutClient.newCall(request).execute();
        });

        assertNotNull(exception, "Expected timeout exception for unreachable server");
    }

    @Test
    void testInvalidUrlThrowsException() {
        Request request = new Request.Builder()
                .url("http://invalid_url") // malformed host
                .build();

        Exception exception = assertThrows(Exception.class, () -> {
            client.newCall(request).execute();
        });

        assertNotNull(exception, "Expected exception for invalid URL");
    }

    // ✅ New extra tests

    @Test
    void testRedirectHandling() throws Exception {
        Request request = new Request.Builder()
                .url("http://httpbin.org/redirect-to?url=https://httpbin.org/get")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code(), "Redirect should resolve to HTTP 200 OK");
            assertTrue(response.request().url().toString().contains("/get"),
                    "Final URL should be the redirected target");
        }
    }

    @Test
    void testNotFoundError() throws Exception {
        Request request = new Request.Builder()
                .url("https://httpbin.org/status/404")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(404, response.code(), "Expected 404 Not Found");
        }
    }

    @Test
    void testParallelRequests() throws Exception {
        int requestCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<Response>> futures = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            futures.add(executor.submit(() -> {
                Request request = new Request.Builder()
                        .url("https://httpbin.org/get")
                        .build();
                return client.newCall(request).execute();
            }));
        }

        for (Future<Response> future : futures) {
            try (Response response = future.get()) {
                assertEquals(200, response.code(), "Parallel GET should return 200 OK");
            }
        }

        executor.shutdown();
    }

    @Test
    void testResponseHeaderContentType() throws Exception {
        Request request = new Request.Builder()
                .url("https://httpbin.org/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code(), "Expected HTTP 200 OK");
            assertEquals("application/json", response.header("Content-Type").split(";")[0],
                    "Expected Content-Type to be JSON");
        }
    }
}
