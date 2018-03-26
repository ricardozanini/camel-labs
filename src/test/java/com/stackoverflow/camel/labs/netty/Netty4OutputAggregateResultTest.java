package com.stackoverflow.camel.labs.netty;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.toolbox.AggregationStrategies;
import org.junit.Test;

public class Netty4OutputAggregateResultTest extends CamelTestSupport {
    private static final String ENDPOINT = "netty4:tcp://localhost:4321?sync=true&textline=true&autoAppendDelimiter=false";

    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(2);
        getMockEndpoint("mock:result").expectedBodyReceived().outBody().contains("return message");
        //template.sendBody("direct:start", "line1");
        //template.sendBody("direct:start", "line2");
        //template.sendBody("direct:start", "line3");
        //template.sendBody("direct:start", "EOM");
        
        assertMockEndpointsSatisfied(30, TimeUnit.SECONDS);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            

            @Override
            public void configure() throws Exception {
                from("direct:start")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.info("Processing message: {}", exchange.getIn().getBody());
                        exchange.getOut().setBody("return message");
                    }
                })
                ;
                //.to("mock:result");
                
                
                from("direct:aggregate")
                    .aggregate(constant(true), new AggregationStrategy() { //
                        @Override
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            log.info("Processing message into aggregate: {}", newExchange.getIn().getBody());
                            if (oldExchange == null) {
                                return newExchange;
                            }
                            final String oldBody = oldExchange.getIn().getBody(String.class);
                            final String newBody = newExchange.getIn().getBody(String.class);
                            newExchange.getIn().setBody(oldBody + newBody);
                            log.info("Returning : {}", newExchange.getIn().getBody());
                            return newExchange;
                        }
                    }) //
                    .completion(exchange -> {
                        log.info("completion : {}", exchange.getIn().getBody());
                        final String body = exchange.getIn().getBody(String.class);
                        final boolean done = body.endsWith("EOM");
                        return done;
                    }) //
                    .completionTimeout(3000)
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            log.info("Processing message: {}", exchange.getOut().getBody());
                            exchange.getOut().setBody("return message");
                        }
                    });
                    //.to("mock:result");
                
                from(ENDPOINT) //
                    .setHeader("incoming", constant(true))
                    .inOut("direct:aggregate");
                   
            }
        };
    }

}
