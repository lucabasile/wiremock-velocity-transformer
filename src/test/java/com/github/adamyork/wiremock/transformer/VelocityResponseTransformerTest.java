package com.github.adamyork.wiremock.transformer;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.adamyork.wiremock.transformer.VelocityResponseTransformer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;;

public class VelocityResponseTransformerTest {
    
    private WireMockServer server;
    private WireMockTestClient client;
    
    @Before
    public void setUp() {
        final WireMockConfiguration config = new WireMockConfiguration();
        config.port(8089);
        config.extensions(new VelocityResponseTransformer());
        server = new WireMockServer(config);
        WireMock.configureFor("localhost", 8089);
        server.start();
        client = new WireMockTestClient(8089);
    }
    
    @After
    public void tearDown() {
        server.stop();
        server.shutdownServer();
    }
    
    @Test
    public void testDefaultHeadersArePresent() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"));
        System.out.println(response.content());
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertThat(response.statusCode(),equalTo(200));
        assertTrue(body.getRequestAbsoluteUrl().contains("http://localhost:8089/my/resource"));
        assertTrue(body.getRequestBody().contains("$requestBody"));
        assertTrue(body.getRequestMethod().contains("GET"));
        assertTrue(body.getRequestHeaderHost().contains("localhost:8089"));
        assertTrue(body.getRequestHeaderConnection().contains("keep-alive"));
    }
    
    @Test
    public void testExtendedHeadersArePresent() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"),
                                                new TestHttpHeader("Accept-Language","en-US"),
                                                new TestHttpHeader("Accept-Encoding","UTF-8"),
                                                new TestHttpHeader("User-Agent","Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertEquals(body.getRequestHeaderAcceptEncoding(),"[UTF-8]");
        assertEquals(body.getRequestHeaderAcceptLanguage(),"[en-US]");
        assertEquals(body.getRequestHeaderUserAgent(),"[Mozilla/5.0]");
    }
    
    @Test
    public void testExtendVelocitySyntaxSupport() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"),
                                                new TestHttpHeader("Accept-Language","en-US"),
                                                new TestHttpHeader("Accept-Encoding","UTF-8"),
                                                new TestHttpHeader("User-Agent","Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertNotNull(body.getCustomProp2());
    }
    
    @Test
    public void testDateToolReturnsMonth() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"),
                                                new TestHttpHeader("Accept-Language","en-US"),
                                                new TestHttpHeader("Accept-Encoding","UTF-8"),
                                                new TestHttpHeader("User-Agent","Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        final Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final String month = Integer.toString(cal.get(Calendar.MONTH));
        assertEquals(month,body.getDate());
    }
    
    @Test 
    public void mathToolFloorsValue() {
        stubFor(get(urlEqualTo("/my/resource"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBodyFile("response-test-body.vm")));
        WireMockResponse response = client.get("/my/resource",
                                                new TestHttpHeader("Accept","application/json"),
                                                new TestHttpHeader("Accept-Language","en-US"),
                                                new TestHttpHeader("Accept-Encoding","UTF-8"),
                                                new TestHttpHeader("User-Agent","Mozilla/5.0"));
        final WiremockResponseTestBody body = Json.read(response.content(), WiremockResponseTestBody.class );
        assertEquals("2",body.getMath());
    }

}
