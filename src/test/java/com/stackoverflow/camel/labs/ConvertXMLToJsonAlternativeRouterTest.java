package com.stackoverflow.camel.labs;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.commons.io.IOUtils;
import org.json.XML;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;


@RunWith(CamelSpringBootRunner.class)
@SpringBootTest()
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47546023/symbol-coming-in-the-output-json-while-converting-from-xml-using-apache-came
public class ConvertXMLToJsonAlternativeRouterTest extends CamelTestSupport {
    
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
                from("direct:input")
                    .convertBodyTo(String.class)
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            final String xmlBody = exchange.getIn().getBody(String.class);
                            final String jsonBody = XML.toJSONObject(xmlBody).toString();
                            exchange.getIn().setBody(jsonBody);
                        }
                    })
                    .log("************* My body in json format is ${body} *********")
                    .to("mock:output");                
            }
        };
    
    }

    @Test
    public void test() throws InterruptedException, CamelExecutionException, IOException {
        getMockEndpoint("mock:output").expectedBodyReceived().body().contains("{\"books\"");
        getMockEndpoint("mock:output").expectedMessageCount(1);
       
        
        template.sendBody("direct:input", 
                          IOUtils.toString(this.getClass().getResourceAsStream("/xml/47546023.xml"), 
                                           Charset.defaultCharset()));
        
        getMockEndpoint("mock:output").assertIsSatisfied();
    }

}
