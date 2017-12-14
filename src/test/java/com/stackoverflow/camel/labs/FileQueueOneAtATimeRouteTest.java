package com.stackoverflow.camel.labs;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
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

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47806345/apache-camel-file-end-point-reading-the-file-one-by-one-sorted-in-the-created/
public class FileQueueOneAtATimeRouteTest extends CamelTestSupport {

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
                from("file:src/test/resources/file2?noop=true&maxMessagesPerPoll=1&eagerMaxMessagesPerPoll=false&sortBy=file:modified&delay=1000")
                    .log("${in.header.CamelFileName}")
                    .convertBodyTo(String.class)
                    .to("mock:result");
            }
        };
    }
    
    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(10);
        getMockEndpoint("mock:result").assertMessagesAscending(body());
        assertMockEndpointsSatisfied(30, TimeUnit.SECONDS);
    }

}
