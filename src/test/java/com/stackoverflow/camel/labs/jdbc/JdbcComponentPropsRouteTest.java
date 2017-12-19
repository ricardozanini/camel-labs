package com.stackoverflow.camel.labs.jdbc;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class JdbcComponentPropsRouteTest extends CamelTestSupport {

    private EmbeddedDatabase db;

    public JdbcComponentPropsRouteTest() {

    }

    @Before
    public void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.DERBY)
            .addScript("sql/createAndPopulateDatabase.sql")
            .build();

        super.setUp();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();
        reg.bind("testdb", db);
        reg.bind("rowMapper", new MyModelBeanRowMapper());
        return reg;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:startRowMapper")
                    .to("jdbc:testdb?beanRowMapper=#rowMapper")
                    .to("mock:result");
            }
        };
    }

    @Test
    public void testBeanRowMapper() throws InterruptedException {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:startRowMapper", "select * from projects");
        
        assertMockEndpointsSatisfied();
    }
}
