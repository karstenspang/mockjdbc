package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link Step} used when the {@link Program} is to call a
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
     * Call the supplier and return the result.
     * @param <T> The type of data returned by the step.
     * @param supplier Called to get the result
     * @return the result from {@code supplier}
     * @throws SQLException if {@code supplier} does.
     */
    public <T> T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        return supplier.get();
    }
    
    private PassThruStep(){}
}
