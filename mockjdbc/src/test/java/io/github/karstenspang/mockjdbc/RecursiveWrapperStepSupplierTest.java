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
    
    // For some reason, the H2 driver is not loaded automatically by DriverManager
    @BeforeAll
    static void init()
        throws ClassNotFoundException
    {
        Class.forName("org.h2.Driver");
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
            assertInstanceOf(ResultSetWrap.class,rs,"ResultSet 1");
            rs.next();
            int i=rs.getInt(1);
            assertEquals(1,i);
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
        // This won't work because we have no control over the strings returned by H2
        //assertEquals(
        //    Arrays.asList(...),
        //    events);
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
