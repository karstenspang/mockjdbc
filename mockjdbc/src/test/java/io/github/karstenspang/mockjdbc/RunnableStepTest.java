package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RunnableStepTest{
    static private final String emptyString="";
    static private final SQLSupplier<String> emptyStringSupplier=new SQLSupplier<String>(){public String get(){return emptyString;}};
    static private final SQLRunnable noopRunnable=new SQLRunnable(){public void run(){}};
    
    @DisplayName("A different value is returned")
    @Test
    public void testSuccess()
        throws SQLException
    {
        final AtomicInteger testValue=new AtomicInteger(0);
        final SQLRunnable incrementRunnable=new SQLRunnable(){public void run(){testValue.incrementAndGet();}};
        final SQLRunnable decrementRunnable=new SQLRunnable(){public void run(){testValue.decrementAndGet();}};
        RunnableStep step=new RunnableStep(incrementRunnable);
        assertEquals("RunnableStep",step.toString(),"toString");
        step.apply(decrementRunnable);
        assertEquals(1,testValue.get());
    }
    
    @DisplayName("apply supplier throws IllegalArgumentException")
    @Test
    public void testSupplierThrows(){
        final AtomicInteger testValue=new AtomicInteger(0);
        final SQLRunnable updateRunnable=new SQLRunnable(){public void run(){testValue.incrementAndGet();}};
        RunnableStep step=new RunnableStep(updateRunnable);
        assertThrows(IllegalArgumentException.class,()->{step.apply(emptyStringSupplier);});
    }
    
    @DisplayName("SQLException is thrown if done by stored runnable")
    @Test
    public void testException()
    {
        final SQLException ex=new SQLException();
        final SQLRunnable failingRunnable=new SQLRunnable(){public void run() throws SQLException{throw ex;}};
        RunnableStep step=new RunnableStep(failingRunnable);
        SQLException e=assertThrows(SQLException.class,()->{step.apply(noopRunnable);});
        assertSame(ex,e);
    }
    
    @DisplayName("null runnable to constructor throws NullPointerException")
    @Test
    public void testNull()
    {
        assertThrows(NullPointerException.class,()->new RunnableStep(null));
    }
    
}
