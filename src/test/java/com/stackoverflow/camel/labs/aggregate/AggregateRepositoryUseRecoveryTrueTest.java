package com.stackoverflow.camel.labs.aggregate;

import java.util.Arrays;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hawtdb.HawtDBAggregationRepository;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class AggregateRepositoryUseRecoveryTrueTest extends CamelTestSupport {

    @Test
    public void test() throws InterruptedException {
        MockEndpoint endpoint = getMockEndpoint("mock:result");
        endpoint.expectedMessageCount(1);
        
        template.sendBody("direct:start", "test");
        Thread.sleep(3000);
        endpoint.assertIsSatisfied(10000);
    }
    
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            
            @Override
            public void configure() throws Exception {
                HawtDBAggregationRepository hawtDBRepo = new HawtDBAggregationRepository("repo1", "hawtdb.dat");
                hawtDBRepo.setUseRecovery(true);
                
                from("direct:start")
                    .setHeader("id", constant(01))
                    .log("${body}")                
                    .to("direct:serv_01")
                    .log("${body}")
                    .enrich("direct:serv_02", new AggregationStrategy() {
                        @Override
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            String[] str = new String[] {
                                oldExchange.getIn().getBody(String.class),
                                newExchange.getIn().getBody(String.class)
                            };
                            newExchange.getIn().setBody(Arrays.toString(str));
                            return newExchange;
                        }
                    })
                    .log("${body}");
                
              //servico 1
                from("direct:serv_01")
                    .log("inside serv 01")
                    .transform().constant("retorno do servico 01")
                .to("direct:aggregate");

                //servico 2
                from("direct:serv_02")
                    .log("inside serv 02")
                    .transform().constant("retorno do servico 02")
                    .to("direct:aggregate");
                
                from("direct:aggregate")
                    .aggregate(header("id"), new AggregateUseRecoveryTrue())
                    .aggregationRepository(hawtDBRepo)
                    .completionSize(2)
                    .completionTimeout(10000)
                .to("direct:end_aggregate");
                
                from("direct:end_aggregate")
                    .log("agregacaoComSucesso: ${property.aggregateSuccess}")
                    .log("Final da Agregação: ${body}")
                .to("mock:result");
            }
        };
    }

}
