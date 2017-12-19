package com.stackoverflow.camel.labs.hl7;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import ca.uhn.hl7v2.model.Message;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest()
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47674480/how-to-read-hl7-file-and-parse-it-using-apache-camel-hapi-spring-java-config
public class HL7SimpleRouteTest extends CamelTestSupport {

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
                from("file:src/test/resources/hl7?noop=true")
                    .convertBodyTo(String.class)
                    .unmarshal()
                    .hl7(false)
                    .log("The Message body is: ${body}")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            final Message message = exchange.getIn().getBody(Message.class);
                            System.out.println("Original message: " + message);
                        }
                    })
                    .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodyReceived().body(Message.class);

        assertMockEndpointsSatisfied();
    }

}
