package com.stackoverflow.camel.labs.recipientlist;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

// https://stackoverflow.com/questions/47937712/ordering-of-responses-from-endpoints-in-recipientlist
public class RecipientListSequenceAggregateRouteTest extends CamelTestSupport {
    
    EmbeddedDatabase db;

    @Before
    public void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.DERBY).addScript("sql/47937712.sql").build();

        super.setUp();
    }
    
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            
            @Override
            public void configure() throws Exception {
                getContext().getComponent("sql", SqlComponent.class).setDataSource(db);
                
                from("direct:start")
                    .recipientList(constant("direct:a1, direct:a2, direct:a3, direct:select"));
                
                from("direct:a1")
                    .log("got message in a1: waiting 3s")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Thread.sleep(3000);
                        }
                    })
                    .setBody(constant("a1"))
                    .recipientList(constant("direct:db, direct:flow"));
                
                from("direct:a2")
                    .log("got message in a2: waiting 5s")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Thread.sleep(5000);
                        }                   
                    })
                    .setBody(constant("a2"))
                    .recipientList(constant("direct:db, direct:flow"));
                
                from("direct:a3")
                    .log("got message in a3: waiting 1s")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Thread.sleep(1000);
                        }
                    });
                
                from("direct:db")
                    .log("got message in db from ${body}")
                    .setBody(simple("db_${in.body}"))
                    .to("sql:insert into log (body_in) values (:#${in.body})");
                    ;
                
                from("direct:flow")
                    .log("got message in flow from ${body}")
                    .setBody(simple("flow_${in.body}"))
                    .to("sql:insert into log (body_in) values (:#${in.body})");
                    ;
                
               from("direct:select")
                   .to("sql:select * from log order by time_in")
                   .log("results:\n ${body}")
                   .to("mock:result");
            }
        };
    }

    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        Object results = template.requestBody("direct:start", "");
        assertNotNull(results);
        assertMockEndpointsSatisfied();
    }

}
