package com.stackoverflow.camel.labs.aggregate;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.CompletionAwareAggregationStrategy;
import org.apache.camel.processor.aggregate.TimeoutAwareAggregationStrategy;

public class AggregateUseRecoveryTrue implements AggregationStrategy, TimeoutAwareAggregationStrategy, CompletionAwareAggregationStrategy {

    @Override
    public void onCompletion(Exchange exchange) {
        System.out.println("=======> onCompletion");
    }

    @Override
    public void timeout(Exchange oldExchange, int index, int total, long timeout) {
        oldExchange.setProperty("aggregateTimeout", true);
        System.out.println("=======>  timeout");
    }

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null)
            return newExchange;
        
        final StringBuffer sb = new StringBuffer();
        sb.append(oldExchange.getIn().getBody());
        sb.append(newExchange.getIn().getBody());
        
        oldExchange.setProperty("aggregateSuccess", true);
        oldExchange.getIn().setBody(sb.toString());
        return oldExchange;
    }

}
