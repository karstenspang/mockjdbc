package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link Step} used when the program is to call a
 * method in the real driver.
 */
public class PassThruStep implements Step {
    private static PassThruStep instance=new PassThruStep();
    
    /**
     * Get (the only) instance.
     * @return the instance
     */
    @SuppressWarnings("unchecked")
    public static PassThruStep instance(){
        return instance;
    }
    
    /**
     * Call the method and return the result.
     * @param <T> The type of data returned by the step.
     * @param method Called to get the result
     * @return the result from {@code method}
     * @throws SQLException if {@code method} does.
     */
    public <T> T apply(SQLSupplier<? extends T> method)
        throws SQLException
    {
        return method.get();
    }
    
    /**
     * Call the method.
     * @param method Method to call.
     * @throws SQLException if {@code method} does.
     */
    public void apply(SQLRunnable method)
        throws SQLException
    {
        method.run();
    }
    
    @Override
    public String toString(){
        return "PassThruStep";
    }
    
    protected PassThruStep(){}
}
