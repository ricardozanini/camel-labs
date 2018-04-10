package com.stackoverflow.camel.labs.sql.functions;

import java.math.BigInteger;

public final class DemoFunction {

    public DemoFunction() {

    }

    /**
     * @see <a href="http://www.h2database.com/html/features.html#user_defined_functions">User-Defined Functions and Stored Procedures</a>
     * @param value
     * @return
     */
    public static boolean isPrime(int value) {
        return new BigInteger(String.valueOf(value)).isProbablePrime(100);
    }

}
