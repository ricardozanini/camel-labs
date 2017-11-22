package com.stackoverflow.camel.labs;

import static org.hamcrest.CoreMatchers.containsString;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
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

//https://stackoverflow.com/questions/47429592/camel-how-to-add-request-parameterthrowexceptiononfailure-to-url
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
public class QueryParameterHttpRouterTest extends CamelTestSupport {

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
                from("direct:querytest")
                    .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                    .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                    // won't work since throwExceptionOnFailure should be on the endpoint definition
                    .setHeader(Exchange.HTTP_QUERY, constant("throwExceptionOnFailure=false"))
                    .to("http://localhost:9090");
                
                from("jetty:http://localhost:9090")
                    .log("***** The querystring is: ${header.CamelHttpQuery}")
                    .log("***** The querystring is: ${header.CamelHttpQuery}")
                    .setHeader(Exchange.HTTP_QUERY, header(Exchange.HTTP_QUERY))
                    .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedHeaderReceived(Exchange.HTTP_QUERY, "throwExceptionOnFailure=false");
        final Exchange response = template.request("direct:querytest", null);
        assertThat(response.getIn().getHeader(Exchange.HTTP_QUERY).toString(), containsString("throwExceptionOnFailure=false"));
        assertMockEndpointsSatisfied();
    }

}
