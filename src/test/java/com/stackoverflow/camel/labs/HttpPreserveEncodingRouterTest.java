package com.stackoverflow.camel.labs;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;

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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47372827/keep-part-of-uri-encoded-in-camel-route
public class HttpPreserveEncodingRouterTest extends CamelTestSupport {

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
                from("direct:start")
                    .setHeader("PRIVATE-TOKEN", constant("myToken"))
                    .setHeader(Exchange.HTTP_URI, simple("http://0.0.0.0:8080?param=folder%2Ffile%2Eextension/raw&ref=master"))
                    .to("http:dummy");
                
                from("jetty:http://0.0.0.0:8080?matchOnUriPrefix=true")
                    .setBody(constant("{ key: value }"))
                    .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                    .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedHeaderReceived(Exchange.HTTP_QUERY, "param=folder%2Ffile%2Eextension/raw&ref=master");
        final Exchange response = template.send("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                // nothing
            }
        });
         
        assertThat(response, notNullValue());
        assertThat(response.getIn().getHeader(Exchange.HTTP_URI).toString(), containsString("folder%2Ffile%2"));
        assertThat(response.getOut().getBody(String.class), containsString("{ key: value }"));
        
        assertMockEndpointsSatisfied();
    }

}
