package com.crediya.loan.r2dbc.config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = MysqlConnectionPropertiesTest.Config.class,
        properties = {
                "adapters.r2dbc.host=localhost",
                "adapters.r2dbc.port=3306",
                "adapters.r2dbc.database=testdb",
                "adapters.r2dbc.username=root",
                "adapters.r2dbc.password=secret"
        }
)
class MysqlConnectionPropertiesTest {

    @EnableConfigurationProperties(MysqlConnectionProperties.class)
    static class Config { }

    @Autowired
    private MysqlConnectionProperties props;

    @Test
    void bindsProperties() {
        assertNotNull(props);
        assertEquals("localhost", props.host());
        assertEquals(3306, props.port());
        assertEquals("testdb", props.database());
        assertEquals("root", props.username());
        assertEquals("secret", props.password());
    }
}