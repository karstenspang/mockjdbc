package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import io.github.karstenspang.mockjdbc.wrap.ResultSetWrap;
import io.github.karstenspang.mockjdbc.wrap.StatementWrap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
public class RecursiveWrapperStepSupplierTest {
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
        try(Connection conn=DriverManager.getConnection("jdbc:mock:noop:",new Properties())){
            assertInstanceOf(ConnectionWrap.class,conn,"Connection");
            Statement stmt=conn.createStatement();
            assertInstanceOf(StatementWrap.class,stmt,"Statement");
            ResultSet rs=stmt.executeQuery("select 0");
            assertInstanceOf(ResultSetWrap.class,rs,"ResultSet 1");
            rs.next();
            int i=rs.getInt(1);
            assertEquals(0,i);
            rs=stmt.executeQuery("select null");
            assertInstanceOf(ResultSetWrap.class,rs,"ResultSet 2");
            rs.next();
            String s=rs.getString(1);
            assertNull(s);
        }
        List<LoggingEvent> events=new ArrayList<>();
        for (TestLogger logger:loggers){
            events.addAll(logger.getLoggingEvents());
        }
        assertEquals(
            Arrays.asList(
                LoggingEvent.debug("Setting step provider RecursiveWrapperStepSupplier"),
                LoggingEvent.trace("Apply RecursiveWrapperStep to DriverManager.getConnection(jdbc:noop:,{})"),
                LoggingEvent.trace("Result: io.github.karstenspang.mockjdbc.wrap.ConnectionWrap:{wrapped:NoopConnection,stepSupplier:RecursiveWrapperStepSupplier}"),
                LoggingEvent.debug("Wrapping NoopConnection in io.github.karstenspang.mockjdbc.wrap.ConnectionWrap with step supplier RecursiveWrapperStepSupplier"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to Connection.createStatement()"), 
                LoggingEvent.trace("Result: io.github.karstenspang.mockjdbc.wrap.StatementWrap:{wrapped:NoopStatement,stepSupplier:RecursiveWrapperStepSupplier}"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to Connection.close()"), 
                LoggingEvent.debug("Wrapping NoopStatement in io.github.karstenspang.mockjdbc.wrap.StatementWrap with step supplier RecursiveWrapperStepSupplier"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to Statement.executeQuery(select 0)"), 
                LoggingEvent.trace("Result: io.github.karstenspang.mockjdbc.wrap.ResultSetWrap:{wrapped:NoopResultSet,stepSupplier:RecursiveWrapperStepSupplier}"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to Statement.executeQuery(select null)"), 
                LoggingEvent.trace("Result: io.github.karstenspang.mockjdbc.wrap.ResultSetWrap:{wrapped:NoopResultSet,stepSupplier:RecursiveWrapperStepSupplier}"), 
                LoggingEvent.debug("Wrapping NoopResultSet in io.github.karstenspang.mockjdbc.wrap.ResultSetWrap with step supplier RecursiveWrapperStepSupplier"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to ResultSet.next()"), 
                LoggingEvent.trace("Result: false"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to ResultSet.getInt(1)"), 
                LoggingEvent.trace("Result: 0"), 
                LoggingEvent.debug("Wrapping NoopResultSet in io.github.karstenspang.mockjdbc.wrap.ResultSetWrap with step supplier RecursiveWrapperStepSupplier"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to ResultSet.next()"), 
                LoggingEvent.trace("Result: false"), 
                LoggingEvent.trace("Apply RecursiveWrapperStep to ResultSet.getString(1)"), 
                LoggingEvent.trace("Result: null")
            ),
            events);
    }
    
    @Test
    @DisplayName("Extended interfaces come before the interface they extend in the search list")
    void testSequence()
    {
        final Set<Class<?>> specialInterfaces=new HashSet<>(Arrays.asList(Wrapper.class,AutoCloseable.class));
        List<Class<?>> interfaces=RecursiveWrapperStepSupplier.wrappedInterfaces();
        for (int i=0;i<interfaces.size();i++){
            Class<?> ifClass=interfaces.get(i);
            Set<Class<?>> extendedInterfaces=new HashSet<>(Arrays.asList(ifClass.getInterfaces()));
            extendedInterfaces.removeAll(specialInterfaces);
            if (extendedInterfaces.isEmpty()) continue;
            assertEquals(1,extendedInterfaces.size(),"parents of "+ifClass.getSimpleName());
            Class<?> parent=extendedInterfaces.toArray(new Class<?>[1])[0];
            boolean found=false;
            for (int j=i+1;j<interfaces.size();j++){
                if (parent==interfaces.get(j)){
                    found=true;
                    break;
                }
            }
            assertTrue(found,ifClass.getSimpleName()+" before "+parent.getSimpleName());
        }
    }
}
