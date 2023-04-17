package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExceptionStepTest {
    
    @Test
    @DisplayName("Exception is thrown on supplier")
    public void testSupplier()
        throws SQLException
    {
        SQLException ex=new SQLException();
        ExceptionStep st=new ExceptionStep(ex);
        st.toString();
        final Integer i1=Integer.valueOf(1);
        SQLException e=assertThrows(SQLException.class,()->st.apply(()->i1));
        assertSame(ex,e);
    }
    
    @Test
    @DisplayName("Exception is thrown on runnable")
    public void testRunnable()
        throws SQLException
    {
        SQLException ex=new SQLException();
        ExceptionStep st=new ExceptionStep(ex);
        st.toString();
        SQLException e=assertThrows(SQLException.class,()->st.apply(()->{}));
        assertSame(ex,e);
    }
    
    @Test
    @DisplayName("RuntimeException is thrown on supplier")
    public void testSupplierRuntime()
        throws SQLException
    {
        RuntimeException ex=new RuntimeException();
        ExceptionStep st=new ExceptionStep(ex);
        st.toString();
        final Integer i1=Integer.valueOf(1);
        RuntimeException e=assertThrows(RuntimeException.class,()->st.apply(()->i1));
        assertSame(ex,e);
    }
    
    @Test
    @DisplayName("RuntimeException is thrown on runnable")
    public void testRunnableRuntime()
        throws SQLException
    {
        RuntimeException ex=new RuntimeException();
        ExceptionStep st=new ExceptionStep(ex);
        st.toString();
        RuntimeException e=assertThrows(RuntimeException.class,()->st.apply(()->{}));
        assertSame(ex,e);
    }
}
