package com.stackoverflow.camel.labs.sql;

import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47782520/camel-enrich-sql-syntax-issue
public class PollEnrichDynamicEndpointRouterTest extends CamelTestSupport {
    
    EmbeddedDatabase db;

    @Before
    public void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.DERBY).addScript("sql/createAndPopulateDatabase.sql").build();

        super.setUp();
    }
    
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
                getContext().getComponent("sql", SqlComponent.class).setDataSource(db);
                
                from("direct:start")
                    .setHeader("userID", constant(1001))
                    .enrich("mock:direct:enrich${in.header.userID}", new AggregationStrategy() {
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            //nothing
                            newExchange.getIn().setBody(newExchange.getIn().getHeader("userID"));
                            return newExchange;
                        }
                    })
                    .log("the body ${body}")
                    .to("mock:result");
                
                from("direct:sql_start")
                    .setHeader("userID", constant(1))
                    .enrich("sql:select project from projects where id = :#${in.header.userID}", new AggregationStrategy() {
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            //nothing
                            return newExchange;
                        }
                    })
                    .log("the body ${body}")
                    .to("mock:result_sql");
            }
        };
    }

    @Test
    public void test() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        final Object body = template.requestBody("direct:start", "test");
        assertThat((Integer)body, is(1001));
        assertMockEndpointsSatisfied();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void test_sql() throws InterruptedException {
        getMockEndpoint("mock:result_sql").expectedMessageCount(1);
        final ArrayList<Map<String, Object>> body = (ArrayList)template.requestBody("direct:sql_start", "test");
        assertNotNull(body);
        assertThat(body.get(0).get("PROJECT").toString(), is("Camel"));
        assertMockEndpointsSatisfied();
    }

}
