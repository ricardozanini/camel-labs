package com.stackoverflow.camel.labs;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.apache.cxf.message.MessageContentsList;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//https://stackoverflow.com/questions/47551481/camel-cxf-endpoint-not-returning-pojo
public class CXFSoapClientPOJODataFormatTest extends CamelSpringTestSupport {

    @Test
    public void test() {
        final Exchange sender = new DefaultExchange(context, ExchangePattern.InOut);

        final List<Object> messageList = new ArrayList<Object>();
        messageList.add(10);
        messageList.add(10);

        sender.getIn().setHeader(CxfConstants.OPERATION_NAME, "Add");
        sender.getIn().setBody(messageList);

        final Exchange response = template.send("direct:start", sender);

        assertNotNull(response);
        assertThat(response.getIn().getBody(), is(instanceOf(MessageContentsList.class)));
        assertThat((Integer)response.getIn().getBody(MessageContentsList.class).get(0), is(20));
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .to("cxf:bean:calculatorEndpoint?synchronous=true").log("*********** my body is: " + body());
            }
        };
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("com/stackoverflow/camel/labs/CXFSoapClientPOJODataFormatTest.xml");
    }

}
