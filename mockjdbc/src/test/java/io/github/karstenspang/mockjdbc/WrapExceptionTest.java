package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WrapExceptionTest {
    @Test
    @DisplayName("Throwing SQLException in Connection.setClientInfo results in UnsupportedOperationException")
    public void testConnectionSetClientInfoSQLException()
        throws SQLException
    {
        SQLException ex=new SQLException();
        MockDriver.setProgram(Arrays.asList(
            new WrapperStep<Connection>(ConnectionWrap::new,Arrays.asList(
                new ExceptionStep(ex)))));
        try(Connection conn=DriverManager.getConnection("jdbc:mock:noop:")){
            Properties prop=new Properties();
            UnsupportedOperationException e=assertThrows(UnsupportedOperationException.class,()->conn.setClientInfo(prop));
            assertSame(ex,e.getCause());
        }
    }
    
    @Test
    @DisplayName("Throwing SQLClientInfoException in Connection.setClientInfo results in SQLClientInfoException")
    public void testConnectionSetClientInfoSQLClientInfoException()
        throws SQLException
    {
        SQLClientInfoException ex=new SQLClientInfoException();
        MockDriver.setProgram(Arrays.asList(
            new WrapperStep<Connection>(ConnectionWrap::new,Arrays.asList(
                new ExceptionStep(ex)))));
        try(Connection conn=DriverManager.getConnection("jdbc:mock:noop:")){
            Properties prop=new Properties();
            SQLClientInfoException e=assertThrows(SQLClientInfoException.class,()->conn.setClientInfo(prop));
            assertSame(ex,e);
        }
    }
}
