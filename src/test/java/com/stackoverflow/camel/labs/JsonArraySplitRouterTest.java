package com.stackoverflow.camel.labs;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.camel.util.toolbox.AggregationStrategies;
import org.apache.commons.io.IOUtils;
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
// https://stackoverflow.com/questions/47796217/apache-camel-with-json-array-split
public class JsonArraySplitRouterTest extends CamelTestSupport {
    
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
                    .split().jsonpath("$")
                        .streaming()
                        .aggregate(AggregationStrategies.groupedExchange())
                        .constant("true")
                        .completionSize(5)
                        .completionTimeout(1000)
                        .log("${body}")
                    .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws IOException, InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(2);
        
        sendBodies("direct:start", 
                   IOUtils.toString(
                                    this.getClass().getResourceAsStream("/json/47796217.json"), Charset.defaultCharset()));
        
        assertMockEndpointsSatisfied();
    }

}
