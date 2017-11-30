package com.stackoverflow.camel.labs;

import static org.hamcrest.CoreMatchers.is;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
//https://stackoverflow.com/questions/47517933/jboss-fuse-camel-dsl-using-camel-netty4-http-how-to-i-obtain-the-rest-return-c
public class Netty4RetrieveStatusCode extends CamelTestSupport {

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
                from("direct:start")
                    .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .to("netty4-http:http://localhost:8080")
                    .log("The response code is: ${header["+Exchange.HTTP_RESPONSE_CODE+"]}")
                    .log("The body is: '${body}'")
                    .bean(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            final Integer code = (Integer)exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE);
                            exchange.getIn().setHeader("responseCode", code);
                        }
                    });

                from("jetty:http://0.0.0.0:8080").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setBody("OK");
                    }
                });
            }
        };
    }

    @Test
    public void test() {
        final String body = "{say : 'hello'}";
        final Exchange response = template.send("direct:start", createExchangeWithBody(body));
        assertThat((Integer)response.getIn().getHeader("responseCode"), is(200));
    }

}
