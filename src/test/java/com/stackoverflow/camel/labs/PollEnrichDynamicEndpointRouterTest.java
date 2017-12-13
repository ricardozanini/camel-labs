package com.stackoverflow.camel.labs;

import static org.hamcrest.Matchers.is;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
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
// https://stackoverflow.com/questions/47782520/camel-enrich-sql-syntax-issue
public class PollEnrichDynamicEndpointRouterTest extends CamelTestSupport {
    
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
                    .setHeader("userID", constant(1001))
                    .enrich("mock:direct:enrich${in.header.userID}", new AggregationStrategy() {
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            //nothing
                            newExchange.getIn().setBody(newExchange.getIn().getHeader("userID"));
                            return newExchange;
                        }
                    })
                    .log("the body ${body}")
                    .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        final Object body = template.requestBody("direct:start", "test");
        assertThat((Integer)body, is(1001));
        assertMockEndpointsSatisfied();
    }

}
