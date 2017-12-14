package com.stackoverflow.camel.labs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.soap.name.ElementNameStrategy;
import org.apache.camel.dataformat.soap.name.TypeNameStrategy;
import org.apache.camel.model.dataformat.SoapJaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.tempuri.calculator.jaxb2.Add;

// https://stackoverflow.com/questions/47750592/how-to-use-camel-soap-unmarshal
public class SoapDataFormatRouteTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate producer;

    protected SoapJaxbDataFormat createDataFormat() {
        String jaxbPackage = Add.class.getPackage().getName();
        ElementNameStrategy elStrat = new TypeNameStrategy();
        SoapJaxbDataFormat answer = new SoapJaxbDataFormat(jaxbPackage, elStrat);
        answer.setVersion("1.2");
        return answer;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                SoapJaxbDataFormat df = createDataFormat();
                from("direct:start") //
                    .marshal(df) //
                    .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws InterruptedException, IOException {
        InputStream in = this.getClass().getResourceAsStream("/xml/AddRequest.xml");
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.expectedBodiesReceived(IOUtils.toString(in, Charset.defaultCharset()));
        Add request = new Add();
        request.setIntA(10);
        request.setIntB(20);
        producer.sendBody(request);
        resultEndpoint.assertIsSatisfied();
    }

}
