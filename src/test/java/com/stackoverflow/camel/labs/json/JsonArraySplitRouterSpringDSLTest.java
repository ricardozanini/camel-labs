package com.stackoverflow.camel.labs.json;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

// https://stackoverflow.com/questions/47796217/apache-camel-with-json-array-split
public class JsonArraySplitRouterSpringDSLTest extends CamelSpringTestSupport {

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("json/JsonArraySplitRouterSpringDSLTest.xml");
    }

    @Test
    public void test() throws IOException, InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(2);

        sendBodies("direct:start", 
                   IOUtils.toString(this.getClass().getResourceAsStream("/json/47796217.json"), Charset.defaultCharset()));

        assertMockEndpointsSatisfied();
    }

}
