package com.github.karstenspang.mockjdbc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        throws IOException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertEquals(42,driver.getMajorVersion());
        assertEquals(17,driver.getMinorVersion());
    }
    
    @Test
    @DisplayName("pom.properties missing throws IOException")
    public void testNoPomProperties()
        throws IOException
    {
        assertThrows(IOException.class,()->new MockDriver("nosuchfile"));
    }
    
    @Test
    @DisplayName("If version is not present in pom.properties major and minor versions are 0")
    public void testNoVersion()
        throws IOException
    {
        MockDriver driver=new MockDriver("noversion.pom.properties");
        assertEquals(0,driver.getMajorVersion());
        assertEquals(0,driver.getMinorVersion());
    }
    
    @Test
    @DisplayName("If version is not numeric in pom.properties major and minor versions are 0")
    public void testNonNumericVersion()
        throws IOException
    {
        MockDriver driver=new MockDriver("develop.pom.properties");
        assertEquals(0,driver.getMajorVersion());
        assertEquals(0,driver.getMinorVersion());
    }
    
    @Test
    @DisplayName("The driver accepts URL's starting with jdbc:mock:")
    public void testAcceptsMock()
        throws IOException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertTrue(driver.acceptsURL("jdbc:mock:somedriver"));
    }
    
    @Test
    @DisplayName("The driver does not accepts URL's not starting with jdbc:mock:")
    public void testRejectsNoMock()
        throws IOException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertFalse(driver.acceptsURL("mock:somedriver"));
    }
    
    @Test
    @DisplayName("connect returns null for URL's not starting with jdbc:mock:")
    public void testReturnsNullNoMock()
        throws IOException,SQLException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertNull(driver.connect("mock:somedriver",new Properties()));
    }
    
    @Test
    @DisplayName("Parent logger has the expected name")
    public void testParentLogger()
        throws IOException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertEquals("com.github.karstenspang.mockjdbc",driver.getParentLogger().getName());
    }
    
    @Test
    @DisplayName("Parent claims to be JDBC compliant")
    public void testJdbcCompliant()
        throws IOException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertTrue(driver.jdbcCompliant());
    }
    
    @Test
    @DisplayName("Driver has no properties")
    public void testDriverProperties()
        throws IOException
    {
        MockDriver driver=new MockDriver("test1.pom.properties");
        assertArrayEquals(new DriverPropertyInfo[0],driver.getPropertyInfo("url",new Properties()));
    }
    
    @Test
    @DisplayName("Program with one exception throws exception at first call and succeeds at second")
    public void testOneException()
        throws IOException,SQLException
    {
        SQLException myex=new SQLException("my exception");
        ExceptionStep<Connection> step=new ExceptionStep<>(myex);
        Program<Connection> program=new Program<>(Arrays.<Step<Connection>>asList(step));
        MockDriver.setProgram(program);
        SQLException ex=assertThrows(SQLException.class,()->DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties()));
        assertEquals(myex,ex);
        try(Connection conn=DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties())){}
    }
    
    @Test
    @DisplayName("Empty program succeeds")
    public void testEmptyProgram()
        throws IOException,SQLException
    {
        MockDriver.setProgram(null);
        try(Connection conn=DriverManager.getConnection("jdbc:mock:h2:mem:",new Properties())){}
    }
}
