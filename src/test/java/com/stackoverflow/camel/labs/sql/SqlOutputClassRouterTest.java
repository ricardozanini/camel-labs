package com.stackoverflow.camel.labs.sql;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
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

import com.stackoverflow.camel.labs.sql.model.Model2;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
// https://stackoverflow.com/questions/47831709/camel-jdbc-and-outputclass-option
public class SqlOutputClassRouterTest extends CamelTestSupport {

    EmbeddedDatabase db;

    @Autowired
    private CamelContext camelContext;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        return camelContext;
    }
    
    @Before
    public void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.DERBY).addScript("sql/47831709.sql").build();

        super.setUp();
    }
    
    
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                getContext().getComponent("sql", SqlComponent.class).setDataSource(db);
                BindyCsvDataFormat camelDataFormat = new BindyCsvDataFormat(Model2.class);

                from("sql:select vendor, ean, itemid as itemId, quantity from ElectronicDeliveryNotes?outputType=SelectList&outputClass=com.stackoverflow.camel.labs.sql.model.Model2")
                        .marshal(camelDataFormat)
                        .log("the body:\n${body}")
                        .to("mock:result");
            }
        };
    }

    @Test
    public void testSQL() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedBodyReceived().body().contains("11.0441-5402.2");
        assertMockEndpointsSatisfied();
    }
}
