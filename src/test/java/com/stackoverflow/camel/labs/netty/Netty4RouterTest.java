package com.stackoverflow.camel.labs.netty;

import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
public class Netty4RouterTest extends CamelTestSupport {

    private static final String ENDPOINT = "netty4:tcp://localhost:5150?textline=true&delimiter=NULL";

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
                from(ENDPOINT).routeId("nett4-route").log("Body is: " + body()).process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        final String body = exchange.getIn().getBody(String.class);
                        final String response = String.format("%s,%s,%s", body, body.length(), body.getBytes());
                        exchange.getOut().setBody(response);
                    }
                });
            }
        };
    }

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        assertNotNull(camelContext);
        final String body = "hello world!" + System.getProperty("line.separator");
        final Map<String, Object> request = new HashMap<String, Object>();
        request.put("message", body);
        request.put("len", body.length());
        final String responseBody = template.requestBody(ENDPOINT, body, String.class);
        assertNotNull(responseBody);
        final String[] responseArr = responseBody.split(",");

        assertThat(responseArr[0], is(request.get("message")));
        assertThat(Integer.parseInt(responseArr[1]), is((Integer)request.get("len")));
    }

}
