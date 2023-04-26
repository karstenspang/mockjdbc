package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SupplierStepTest{
    static private final String someString="some string";
    static private final SQLSupplier<String> someStringSupplier=new SQLSupplier<String>(){public String get(){return someString;}};
    static private final String emptyString="";
    static private final SQLSupplier<String> emptyStringSupplier=new SQLSupplier<String>(){public String get(){return emptyString;}};
    static private final Integer zero=Integer.valueOf(0);
    static private final SQLSupplier<Integer> integerSupplier=new SQLSupplier<Integer>(){public Integer get(){return zero;}};
    
    @DisplayName("A different value is returned")
    @Test
    public void testSuccess()
        throws SQLException
    {
        SupplierStep step=new SupplierStep(someStringSupplier);
        assertEquals("SupplierStep",step.toString(),"toString");
        String result=step.apply(emptyStringSupplier);
        assertSame(someString,result,"result");
    }
    
    @DisplayName("The action is not called")
    @Test
    public void testNoAction()
        throws SQLException
    {
        final AtomicInteger testValue=new AtomicInteger(0);
        final SQLSupplier<String> updateSupplier=new SQLSupplier<String>(){public String get(){testValue.incrementAndGet();return emptyString;}};
        SupplierStep step=new SupplierStep(someStringSupplier);
        step.apply(updateSupplier);
        assertEquals(0,testValue.get());
    }
    
    @DisplayName("apply runnable throws IllegalArgumentException")
    @Test
    public void testRunnableThrows(){
        final AtomicInteger testValue=new AtomicInteger(0);
        final SQLRunnable updateRunnable=new SQLRunnable(){public void run(){testValue.incrementAndGet();}};
        SupplierStep step=new SupplierStep(someStringSupplier);
        assertThrows(IllegalArgumentException.class,()->{step.apply(updateRunnable);});
    }
    
    @DisplayName("ClassCastException is thrown on type mismatch")
    @Test
    public void testMismatch(){
        SupplierStep step=new SupplierStep(someStringSupplier);
        assertThrows(ClassCastException.class,()->{Integer x=step.apply(integerSupplier);});
    }
    
    @DisplayName("SQLException is thrown if done by stored supplier")
    @Test
    public void testException()
    {
        final SQLException ex=new SQLException();
        final SQLSupplier<String> failingStringSupplier=new SQLSupplier<String>(){public String get() throws SQLException{throw ex;}};
        SupplierStep step=new SupplierStep(failingStringSupplier);
        SQLException e=assertThrows(SQLException.class,()->{String x=step.apply(emptyStringSupplier);});
        assertSame(ex,e);
    }
    
    @DisplayName("null supplier to constructor throws NullPointerException")
    @Test
    public void testNull()
    {
        assertThrows(NullPointerException.class,()->new SupplierStep(null));
    }
    
}
