package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class ExampleTest {
    @BeforeAll
    static void init()
        throws ClassNotFoundException
    {
        // Make sure the H2 driver is loaded.
        Class.forName("org.h2.Driver");
        TestLogging.setup();
    }
    
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
        Connection conn=Example.getConnection("jdbc:mock:h2:mem:","user","pwd",2,0L);
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
                new ExceptionStep(disconnect), // createStatement
                new ExceptionStep(closeFail) // Connection.close
            )),
            PassThruStep.instance() // Reconnect
        );
        
        TestLogger exampleLogger=TestLoggerFactory.getTestLogger(Example.class);
        exampleLogger.clear();
        MockDriver.setProgram(program);
        Connection conn=Example.checkOrConnect(null,"jdbc:mock:h2:mem:","user","pwd");
        conn=Example.checkOrConnect(conn,"jdbc:mock:h2:mem:","user","pwd");
        conn.close();
        List<LoggingEvent> events=exampleLogger.getLoggingEvents();
        List<LoggingEvent> expectedEvents=Arrays.asList(
            LoggingEvent.info("Connecting to jdbc:mock:h2:mem:"),
            LoggingEvent.debug("Checking connection"),
            LoggingEvent.error(disconnect,"Connection broken, closing"),
            LoggingEvent.debug(closeFail,"close failed"),
            LoggingEvent.info("Connecting to jdbc:mock:h2:mem:")
        );
        assertEquals(expectedEvents,events);
    }
}
