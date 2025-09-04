package com.crediya.loan.security.security.jwt;

import com.crediya.loan.security.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtPropertiesTest {

    @Test
    void testSettersAndGetters() {
        JwtProperties props = new JwtProperties();

        props.setSecret("mysupersecretmysupersecretmysupersecret");
        props.setIssuer("crediya");
        props.setExpirationSec(3600L);

        assertEquals("mysupersecretmysupersecretmysupersecret", props.getSecret());
        assertEquals("crediya", props.getIssuer());
        assertEquals(3600L, props.getExpirationSec());
    }
}
