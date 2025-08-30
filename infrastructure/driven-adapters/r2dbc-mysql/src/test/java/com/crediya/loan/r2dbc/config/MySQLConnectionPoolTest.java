package com.crediya.loan.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MySQLConnectionPoolTest {

    private MySQLConnectionPool pool;         // SUT (lo haremos spy para verificar llamada a getConnectionConfig)
    private MysqlConnectionProperties properties;

    @BeforeEach
    void setUp() {
        // Mock de properties
        properties = Mockito.mock(MysqlConnectionProperties.class);
        when(properties.host()).thenReturn("localhost");
        when(properties.port()).thenReturn(5432);   // usa tu DEFAULT_PORT si quieres probar fallback
        when(properties.database()).thenReturn("dbName");
        when(properties.username()).thenReturn("username");
        when(properties.password()).thenReturn("password");

        // Spy del pool para poder verificar la llamada a getConnectionConfig(...)
        pool = Mockito.spy(new MySQLConnectionPool());
    }

    @Test
    void getConnectionConfig_success() {
        assertNotNull(pool.getConnectionConfig(properties));
    }

    @Test
    void connectionPoolBean_created_ok() {
        ConnectionPool cp = pool.connectionPool(properties);
        assertNotNull(cp);

        // Verifica que el bean se construyó pasando por getConnectionConfig(...)
        verify(pool, times(1)).getConnectionConfig(properties);

        // Cierra recursos (no intenta conectar a la BD)
        cp.dispose();
    }
}
