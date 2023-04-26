package io.github.karstenspang.mockjdbc;

import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConstantStepTest{
    static private final String someString="some string";
    static private final String emptyString="";
    static private final SQLSupplier<String> stringSupplier=new SQLSupplier<String>(){public String get(){return emptyString;}};
    static private final Integer zero=Integer.valueOf(0);
    static private final SQLSupplier<Integer> integerSupplier=new SQLSupplier<Integer>(){public Integer get(){return zero;}};
    
    @DisplayName("A different value is returned")
    @Test
    public void testSuccess()
    {
        ConstantStep step=new ConstantStep(someString);
        assertEquals("ConstantStep: some string",step.toString(),"toString");
        String result=step.apply(stringSupplier);
        assertSame(someString,result,"result");
    }
    
    @DisplayName("The action is not called")
    @Test
    public void testNoAction(){
        final AtomicInteger testValue=new AtomicInteger(0);
        final SQLRunnable updateRunnable=new SQLRunnable(){public void run(){testValue.incrementAndGet();}};
        ConstantStep step=new ConstantStep(someString);
        step.apply(updateRunnable);
        assertEquals(0,testValue.get());
    }
    
    @DisplayName("ClassCastException is thrown on type mismatch")
    @Test
    public void testMismatch(){
        ConstantStep step=new ConstantStep(someString);
        assertThrows(ClassCastException.class,()->{Integer x=step.apply(integerSupplier);});
    }
    
    @DisplayName("A null value is returned")
    @Test
    public void testNull()
    {
        ConstantStep step=new ConstantStep(null);
        assertEquals("ConstantStep: null",step.toString(),"toString");
        String result=step.apply(stringSupplier);
        assertNull(result,"result");
    }
}
