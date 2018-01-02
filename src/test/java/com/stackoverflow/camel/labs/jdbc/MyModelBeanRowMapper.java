package com.stackoverflow.camel.labs.jdbc;

import org.apache.camel.component.jdbc.BeanRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyModelBeanRowMapper implements BeanRowMapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MyModelBeanRowMapper.class);

    public MyModelBeanRowMapper() {
    }

    @Override
    public String map(String row, Object value) {
        LOGGER.info("Mapping row '{}' to value '{}'", row, value);
        return row;
    }

}
