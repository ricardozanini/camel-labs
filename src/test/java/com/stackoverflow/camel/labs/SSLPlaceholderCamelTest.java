package com.stackoverflow.camel.labs;

import static org.hamcrest.Matchers.is;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//https://stackoverflow.com/questions/47530999/how-to-externalise-sslcontextparameters
public class SSLPlaceholderCamelTest extends CamelSpringTestSupport {

    @Test
    public void test() {
        assertNotNull(super.context);
        KeyStoreParameters ksp = (KeyStoreParameters)super.applicationContext.getBean("ksp");
        assertThat(ksp.getType(), is("jks"));
        assertThat(ksp.getProvider(), is("jks"));
        assertThat(ksp.getResource(), is("/users/home/server/keystore.jks"));
        assertThat(ksp.getPassword(), is("test"));

        KeyStoreParameters tsp = (KeyStoreParameters)super.applicationContext.getBean("tsp");
        assertThat(tsp.getType(), is("jks"));
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("com/stackoverflow/camel/labs/SSLPlaceholderCamelTest.xml");
    }

}
