package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import io.github.karstenspang.mockjdbc.wrap.PreparedStatementWrap;
import io.github.karstenspang.mockjdbc.wrap.StatementWrap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.github.valfirst.slf4jtest.JulConfigExtension;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

@ExtendWith(JulConfigExtension.class)
public class ExampleTest {
    @Test
    @DisplayName("Example 1: If the connection fails once, and then succeeds, you will get a connection")
    public void testOneFailure()
        throws SQLException,InterruptedException
    {
        // Set up a program for MockDriver of two steps, one simulating
        // the error that we want to test, and the second step simply
        // passes the connction request to the wrapped URL.
        SQLException ex=new SQLException("db overloaded","00000",12520);
        ExceptionStep step1=new ExceptionStep(ex);
        PassThruStep step2=PassThruStep.instance();
        MockDriver.setProgram(Arrays.asList(step1,step2));
        // Run the test
        Connection conn=Example.getConnection("jdbc:mock:noop:","user","pwd",2,0L);
        conn.close();
    }
    
    @Test
    @DisplayName("Example 2: If the connection was broken, it is reconnected")
    public void testCheckConnection()
        throws SQLException
    {
        SQLException disconnect=new SQLException("Connection broken");
        SQLException closeFail=new SQLException("close failed");
        
        List<Step> program=Arrays.asList(
            new WrapperStep<Connection>(ConnectionWrap::new,Arrays.asList( // Initial connection
                new WrapperStep<Statement>(StatementWrap::new,Arrays.asList( // createStatement
                    new ExceptionStep(disconnect), // execute
                    PassThruStep.instance() // Statement.close
                )),
                new ExceptionStep(closeFail) // Connection.close
            )),
            PassThruStep.instance() // Reconnect
        );
        
        TestLogger exampleLogger=TestLoggerFactory.getTestLogger(Example.class);
        exampleLogger.clear();
        MockDriver.setProgram(program);
        Connection conn=Example.checkOrConnect(null,"jdbc:mock:noop:","user","pwd");
        conn=Example.checkOrConnect(conn,"jdbc:mock:noop","user","pwd");
        conn.close();
        List<LoggingEvent> events=exampleLogger.getLoggingEvents();
        List<LoggingEvent> expectedEvents=Arrays.asList(
            LoggingEvent.info("Connecting to jdbc:mock:noop:"),
            LoggingEvent.debug("Checking connection"),
            LoggingEvent.error(disconnect,"Connection broken, closing"),
            LoggingEvent.debug(closeFail,"close failed"),
            LoggingEvent.info("Connecting to jdbc:mock:noop")
        );
        assertEquals(expectedEvents,events);
    }
    
    @Test
    @DisplayName("Example 3: If close() fails for both statements, the exception is from the first with the second suppressed")
    public void testClose()
        throws SQLException
    {
        try(Connection conn=DriverManager.getConnection("jdbc:noop:","user","pwd")){
            final SQLException ex1=new SQLException("1");
            final SQLException ex2=new SQLException("2");
            final Connection wrappedConnection=new ConnectionWrap(conn,Arrays.asList(
                new WrapperStep<PreparedStatement>(PreparedStatementWrap::new,Arrays.asList(
                    new ExceptionStep(ex1)
                )),
                new WrapperStep<PreparedStatement>(PreparedStatementWrap::new,Arrays.asList(
                    new ExceptionStep(ex2)
                ))
            ));
            Example.UsesConnection usesConnection=new Example.UsesConnection(wrappedConnection);
            SQLException ex=assertThrows(SQLException.class,()->usesConnection.close());
            assertSame(ex1,ex,"1");
            Throwable[] suppressed=ex.getSuppressed();
            assertEquals(1,suppressed.length,"# supp");
            assertSame(ex2,suppressed[0],"2");
        }
    }
}
