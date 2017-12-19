package com.stackoverflow.camel.labs.jetty;

import static org.hamcrest.CoreMatchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

//https://stackoverflow.com/questions/47406799/serving-static-files-with-camel-routes

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
public class JettyServeStaticRouterTest extends CamelTestSupport {

    private static final String URL = "http://0.0.0.0:8080/user/dist";
    private static final String ENDPOINT = "jetty:" + URL + "?matchOnUriPrefix=true";

    @Autowired
    private CamelContext camelContext;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        return camelContext;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                from(ENDPOINT).process(new Processor() {

                    public void process(Exchange exchange) throws Exception {
                        Message in = exchange.getIn();

                        String relativepath = in.getHeader(Exchange.HTTP_PATH, String.class);
                        String requestPath = in.getHeader("CamelServletContextPath", String.class); //CamelServletContextPath

                        if (relativepath.isEmpty() || relativepath.equals("/")) {
                            relativepath = "index.html";
                        }

                        final String formattedPath = String.format("%s/%s", requestPath, relativepath);
                        InputStream pathStream = this.getClass().getResourceAsStream(formattedPath);
                        Path path = FileSystems.getDefault().getPath(this.getClass().getResource(formattedPath).getPath());

                        Message out = exchange.getOut();
                        try {
                            out.setBody(IOUtils.toByteArray(pathStream));
                            out.setHeader(Exchange.CONTENT_TYPE, Files.probeContentType(path));
                        } catch (IOException e) {
                            out.setBody(relativepath + " not found.");
                            out.setHeader(Exchange.HTTP_RESPONSE_CODE, "404");
                        }
                    }
                }).routeId("static");

            }
        };
    }

    @Test
    public void testMessage() throws InterruptedException {
        final String response = template.requestBody(ENDPOINT, "", String.class);
        assertNotNull(response);
        assertThat(response, containsString("Hello"));
        //Thread.sleep(100000); //small pause to test in browser.
    }

    @Test
    public void testRequest() throws ClientProtocolException, IOException {
        final Response response = Request.Get(URL  + "index.html").execute();
        assertThat(response.returnContent().asString(), containsString("Hello"));
    }

    @Test
    public void testRequestBrowserReload() throws ClientProtocolException, IOException {
        final Response response = Request.Get(URL).execute();
        assertThat(response.returnContent().asString(), containsString("Hello"));
    }

}
