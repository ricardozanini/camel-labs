package com.stackoverflow.camel.labs.bean;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Properties;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest()
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47515393/using-parameter-bind-anotations-properties-is-null
public class BindAnnotationsPropertiesRouteTest extends CamelTestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BindAnnotationsPropertiesRouteTest.class);

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
                    .setProperty("myProp", constant("Hello World"))
                    .bean(new BeanBindProps(), "methodA('serviceName')")
                    .log("the body is +" + body())
                    .to("mock:result");
            }
        };
    }

    public static class BeanBindProps {
        public BeanBindProps() {
            LOGGER.info("new class");
        }

        public void methodA(String eventName, @Properties Map<String, Object> properties) {
            LOGGER.info("The properties are: {}", properties);
            LOGGER.info("The event name is: {}", eventName);
        }
    }

    @Test
    public void test() throws InterruptedException {
        // with mocks nothing works
        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedPropertyReceived("myProp", "Hello World");
        template.sendBody("direct:start", "myBody");
        assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
    }
}
