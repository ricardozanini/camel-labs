package com.stackoverflow.camel.labs.xml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
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
//https://stackoverflow.com/questions/47562342/extracting-the-values-of-elements-xml-which-is-in-an-array-and-put-it-in-the-m/
public class SplitterFlightsXMLRouterTest extends CamelTestSupport {

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
                    .split(xpath("/TransportFeasibilityResponse/Parameters"), new AggregationStrategy() {
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            List<String> beamIDs = null;
                            if (oldExchange == null) { // first
                                beamIDs = new ArrayList<String>();
                            } else {
                                beamIDs = oldExchange.getIn().getBody(List.class);
                            }
    
                            beamIDs.add(newExchange.getIn().getBody(String.class));
                            newExchange.getIn().setBody(beamIDs);
                            return newExchange;
                        }
                    })
                        .setBody(xpath("/Parameters/BeamID/text()"))
                        .end()
                    .log("The final body: ${body}");
            }
        };
    }

    @Test
    public void test() throws CamelExecutionException, IOException {
        template.sendBody("direct:start", IOUtils.toString(this.getClass().getResourceAsStream("/xml/47562342.xml"), Charset.defaultCharset()));
    }

}
