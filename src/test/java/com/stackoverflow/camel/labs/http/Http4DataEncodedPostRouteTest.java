package com.stackoverflow.camel.labs.http;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
public class Http4DataEncodedPostRouteTest extends CamelTestSupport {
    
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
                    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/x-www-form-urlencoded"))
                    .process(new Processor() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Map<String, Object> bodyParams = exchange.getIn().getBody(Map.class);
                            final StringBuilder sb = new StringBuilder();
                            final Iterator<Map.Entry<String, Object>> it = bodyParams.entrySet().iterator();
                            Entry<String, Object> entry = null;
                            
                            while(it.hasNext()) {
                                entry = it.next();
                                sb.append(entry.getKey())
                                    .append("=")
                                    .append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                                if(it.hasNext()) {
                                   sb.append("&"); 
                                }
                            }
                            exchange.getIn().setBody(sb.toString());
                        }
                    })
                    .to("http4://localhost:9000");
                
                from("jetty:http://localhost:9000")
                    .log("Request received: ${body}")
                    .to("mock:result");
            }
        };      
    
    }

    @Test
    public void test() {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        
        final Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("data1", 1000L);
        bodyParams.put("data2", "this is a test please url encoded it");
        final Exchange exchangeResponse = template.request("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(bodyParams);
            }
        });
        assertNotNull(exchangeResponse);
    }

}
