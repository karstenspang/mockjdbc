package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import io.github.karstenspang.mockjdbc.wrap.ResultSetWrap;
import io.github.karstenspang.mockjdbc.wrap.StatementWrap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class RecursiveWrapperStepSupplierTest {
    
    // For some reason, the H2 driver is not loaded automatically by DriverManager
    @BeforeAll
    static void init()
        throws ClassNotFoundException
    {
        Class.forName("org.h2.Driver");
        TestLogging.setup();
    }
    
    @Test
    @DisplayName("A Connection and all of its sub-objects are wrapped")
    public void testVersion()
        throws SQLException
    {
        TestLogger[] loggers={
            TestLoggerFactory.getTestLogger(MockDriver.class),
            TestLoggerFactory.getTestLogger(ConnectionWrap.class),
            TestLoggerFactory.getTestLogger(StatementWrap.class),
            TestLoggerFactory.getTestLogger(ResultSetWrap.class)
        };
        for (TestLogger logger:loggers){
            logger.clear();
        }
        MockDriver.setStepSupplier(RecursiveWrapperStepSupplier.instance());
        try(Connection conn=DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties())){
            assertInstanceOf(ConnectionWrap.class,conn,"Connection");
            Statement stmt=conn.createStatement();
            assertInstanceOf(StatementWrap.class,stmt,"Statement");
            ResultSet rs=stmt.executeQuery("select 1");
            assertInstanceOf(ResultSetWrap.class,rs,"Statement");
            rs.next();
            int i=rs.getInt(1);
            assertEquals(1,i);
        }
        List<LoggingEvent> events=new ArrayList<>();
        for (TestLogger logger:loggers){
            events.addAll(logger.getLoggingEvents());
        }
        // This won't work because we have no control over the strings returned by H2
        //assertEquals(
        //    Arrays.asList(...),
        //    events);
    }
}
