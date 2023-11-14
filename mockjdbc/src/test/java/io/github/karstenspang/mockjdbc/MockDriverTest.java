package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
public class MockDriverTest {
    @Test
    @DisplayName("Major and minor version can be extracted from pom.properties")
    public void testVersion()
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertEquals(42,driver.getMajorVersion());
        assertEquals(17,driver.getMinorVersion());
    }
    
    @Test
    @DisplayName("If pom.properties is missing, major and minor versions are 0")
    public void testNoPomProperties()
    {
        TestLogger drvLogger=TestLoggerFactory.getTestLogger(MockDriver.class);
        drvLogger.clear();
        MockDriver driver=new MockDriver("nosuchfile");
        assertEquals(0,driver.getMajorVersion(),"major");
        assertEquals(0,driver.getMinorVersion(),"minor");
        List<LoggingEvent> events=drvLogger.getLoggingEvents();
        List<LoggingEvent> expected=Arrays.asList(
            LoggingEvent.warn("Could not load property file nosuchfile"),
            LoggingEvent.info("MockDriver version null")
        );
        assertEquals(expected,events,"log");
    }
    
    @Test
    @DisplayName("If version is not present in pom.properties, major and minor versions are 0")
    public void testNoVersion()
    {
        MockDriver driver=new MockDriver("noversion.pom.properties");
        assertEquals(0,driver.getMajorVersion());
        assertEquals(0,driver.getMinorVersion());
    }
    
    @Test
    @DisplayName("If version is not numeric in pom.properties, major and minor versions are 0")
    public void testNonNumericVersion()
    {
        MockDriver driver=new MockDriver("develop.pom.properties");
        assertEquals(0,driver.getMajorVersion());
        assertEquals(0,driver.getMinorVersion());
    }
    
    @Test
    @DisplayName("The driver accepts URL's starting with jdbc:mock:")
    public void testAcceptsMock()
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertTrue(driver.acceptsURL("jdbc:mock:somedriver"));
    }
    
    @Test
    @DisplayName("The driver does not accepts URL's not starting with jdbc:mock:")
    public void testRejectsNoMock()
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertFalse(driver.acceptsURL("mock:somedriver"));
    }
    
    @Test
    @DisplayName("connect returns null for URL's not starting with jdbc:mock:")
    public void testReturnsNullNoMock()
        throws SQLException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertNull(driver.connect("mock:somedriver",new Properties()));
    }
    
    @Test
    @DisplayName("Parent logger has the expected name")
    public void testParentLogger()
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertEquals("io.github.karstenspang.mockjdbc",driver.getParentLogger().getName());
    }
    
    @Test
    @DisplayName("Parent claims to be JDBC compliant")
    public void testJdbcCompliant()
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertTrue(driver.jdbcCompliant());
    }
    
    @Test
    @DisplayName("Driver has no properties")
    public void testDriverProperties()
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertArrayEquals(new DriverPropertyInfo[0],driver.getPropertyInfo("url",new Properties()));
    }
    
    @Test
    @DisplayName("Program with one exception throws exception at first call and succeeds at second")
    public void testOneException()
        throws SQLException
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep step=new ExceptionStep(myex);
        List<Step> program=Arrays.asList(step);
        MockDriver.setProgram(program);
        SQLException ex=assertThrows(SQLException.class,()->DriverManager.getConnection("jdbc:mock:noop:",new Properties()));
        assertEquals(myex,ex);
        Connection conn=DriverManager.getConnection("jdbc:mock:noop:",new Properties());
        conn.close();
    }
    
    @Test
    @DisplayName("Empty program succeeds")
    public void testEmptyProgram()
        throws SQLException
    {
        MockDriver.setProgram(null);
        Connection conn=DriverManager.getConnection("jdbc:mock:noop:",new Properties());
        conn.close();
    }
    
    @Test
    @DisplayName("The driver program set in the parent thread before thread creation will be used by a child thread")
    public void testProgramInteritance()
        throws Exception
    {
        final SQLException ex=new SQLException("test");
        MockDriver.setProgram(Arrays.asList(new ExceptionStep(ex)));
        TestThread t=new TestThread(()->{
            SQLException thrown=assertThrows(SQLException.class,()->{
                Connection conn=DriverManager.getConnection("jdbc:mock:noop:",new Properties());
                conn.close();
            });
            assertSame(ex,thrown);
        });
        MockDriver.setProgram(null);
        t.start();
        t.joinAndCheck();
    }
    
    @Test
    @DisplayName("Password is hidden by default")
    void testLogPasswordDefault()
        throws Exception
    {
        TestLogger drvLogger=TestLoggerFactory.getTestLogger(MockDriver.class);
        MockDriver driver=new MockDriver("test1.pom.properties");
        Properties props=new Properties();
        props.setProperty("password","secret");
        drvLogger.clear();
        driver.connect("jdbc:mock:noop:",props);
        List<LoggingEvent> events=drvLogger.getLoggingEvents();
        List<String> messages=new ArrayList<>();
        for (LoggingEvent event:events){
            messages.add(event.getMessage());
        }
        
        List<String> expected=Arrays.asList(
            "Apply PassThruStep to DriverManager.getConnection(jdbc:noop:,{password=[HIDDEN]})",
            "Result: NoopConnection"
        );
        assertEquals(expected,messages);
    }
    
    @Test
    @DisplayName("Password is shown if requested")
    void testLogPasswordOn()
        throws Exception
    {
        TestLogger drvLogger=TestLoggerFactory.getTestLogger(MockDriver.class);
        MockDriver driver=new MockDriver("test1.pom.properties");
        driver.logPasswordInstance(true);
        Properties props=new Properties();
        props.setProperty("password","secret");
        drvLogger.clear();
        driver.connect("jdbc:mock:noop:",props);
        List<LoggingEvent> events=drvLogger.getLoggingEvents();
        List<String> messages=new ArrayList<>();
        for (LoggingEvent event:events){
            messages.add(event.getMessage());
        }
        
        List<String> expected=Arrays.asList(
            "Apply PassThruStep to DriverManager.getConnection(jdbc:noop:,{password=secret})",
            "Result: NoopConnection"
        );
        assertEquals(expected,messages);
    }
    
    @Test
    @DisplayName("Password is not replaced if it is not there")
    void testLogNoPassword()
        throws Exception
    {
        TestLogger drvLogger=TestLoggerFactory.getTestLogger(MockDriver.class);
        MockDriver driver=new MockDriver("test1.pom.properties");
        Properties props=new Properties();
        drvLogger.clear();
        driver.connect("jdbc:mock:noop:",props);
        List<LoggingEvent> events=drvLogger.getLoggingEvents();
        List<String> messages=new ArrayList<>();
        for (LoggingEvent event:events){
            messages.add(event.getMessage());
        }
        
        List<String> expected=Arrays.asList(
            "Apply PassThruStep to DriverManager.getConnection(jdbc:noop:,{})",
            "Result: NoopConnection"
        );
        assertEquals(expected,messages);
    }
    
    @Test
    @DisplayName("Password is not replaced if info is not supplied")
    void testLogNoProps()
        throws Exception
    {
        TestLogger drvLogger=TestLoggerFactory.getTestLogger(MockDriver.class);
        MockDriver driver=new MockDriver("test1.pom.properties");
        drvLogger.clear();
        driver.connect("jdbc:mock:noop:",null);
        List<LoggingEvent> events=drvLogger.getLoggingEvents();
        List<String> messages=new ArrayList<>();
        for (LoggingEvent event:events){
            messages.add(event.getMessage());
        }
        
        List<String> expected=Arrays.asList(
            "Apply PassThruStep to DriverManager.getConnection(jdbc:noop:,null)",
            "Result: NoopConnection"
        );
        assertEquals(expected,messages);
    }
    
    @Test
    @DisplayName("The logPassword method calls the logPasswordInstance method in the instance")
    void testStaticLogPassword(){
        boolean savedLogPassword=MockDriver.logPassword();
        try{
            MockDriver.logPassword(true);
            assertTrue(MockDriver.logPassword());
            MockDriver.logPassword(false);
            assertFalse(MockDriver.logPassword());
        }
        finally{
            MockDriver.logPassword(savedLogPassword);
        }
    }
}
