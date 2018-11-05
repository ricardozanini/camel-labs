package com.stackoverflow.camel.labs.cassandra;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.hamcrest.Matchers.is;

/**
 * To run this test one should create the schema based on test/resources/cassandra/test.cql
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KeyspaceUpperCaseTest extends CamelTestSupport {

    private static final String[] DUCKS = new String[] { "Huey", "Dewey", "Louie"};

    @Produce(uri = "direct:select-ducks")
    private ProducerTemplate producerSelectDucks;

    @Produce(uri = "direct:insert-ducks")
    private ProducerTemplate producerInsertDucks;
    
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:insert-ducks")
                    .split(body())
                        .setHeader("duck", body())
                        .to("cql://localhost/TEST?cql=INSERT INTO test_table (name) VALUES (?)")
                        .setBody(header("duck"))
                        .log("${in.body}")
                        .to("mock:result")
                        .end();

                from("direct:select-ducks")
                    .to("cql://localhost/TEST?cql=select name from test_table")
                    .split(body()) // an array of rows
                        .process(p -> {
                            p.getIn().setBody(p.getIn().getBody(Row.class).getString("name").trim());
                        })
                        .log("The body is: ${out.body}")
                        .to("mock:result-select")
                        .end();

            }
        };
    }

    @Test
    public void testInsert() throws InterruptedException {
        producerInsertDucks.sendBody(DUCKS);
        getMockEndpoint("mock:result").expectedMessageCount(3);
        getMockEndpoint("mock:result").allMessages().body(String.class);
        assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);

        Cluster cluster = cassandraCluster();
        Session session = cluster.connect("TEST");
        ResultSet resultSet = session.execute("select name from test_table where name = ?", "Louie");
        Row row = resultSet.one();
        assertNotNull(row);
        assertEquals("Louie", row.getString("name"));
        session.close();
        cluster.close();
    }

    @Test
    public void testSelect() throws InterruptedException {
        producerSelectDucks.sendBody(null);
        List<Exchange> exchanges = getMockEndpoint("mock:result-select").getReceivedExchanges();
        assertThat(exchanges.size(), is(3));
        int i = 0;
        for (Exchange exchange : exchanges) {
            assertTrue(exchange.getIn().getBody(String.class).equals(DUCKS[i]));
            i++;
        }
    }

    @AfterClass
    public static void cleanUp() {
        Cluster cluster = cassandraCluster();
        Session session = cluster.connect("TEST");
        session.execute("TRUNCATE test_table");
        session.close();
        cluster.close();
    }

    public static Cluster cassandraCluster() {
        return Cluster.builder().addContactPoint("localhost").build();
    }

}
