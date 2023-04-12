package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
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

public class MockDriverTest {
    
    // For some reason, the H2 driver is not loaded automatically by DriverManager
    @BeforeAll
    static void init()
        throws ClassNotFoundException
    {
        Class.forName("org.h2.Driver");
        TestLogging.setup();
    }
    
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
        SQLException ex=assertThrows(SQLException.class,()->DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties()));
        assertEquals(myex,ex);
        Connection conn=DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties());
        conn.close();
    }
    
    @Test
    @DisplayName("Empty program succeeds")
    public void testEmptyProgram()
        throws SQLException
    {
        MockDriver.setProgram(null);
        Connection conn=DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties());
        conn.close();
    }
    
    @Test
    @DisplayName("Self-referencing URL throws IllegalArgumentException")
    public void testSelfRefence()
        throws SQLException
    {
        MockDriver.setProgram(null);
        assertThrows(IllegalArgumentException.class,()->DriverManager.getConnection("jdbc:mock:mock:",new Properties()));
    }
}
