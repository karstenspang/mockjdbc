package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link Step} used when the {@link Program} is to call a
 * method in the real driver.
 * @param <T> The type returned.
 *            For example, the {@link MockDriver}
 *            uses steps that return a {@link Connection}.
 */
public class PassThruStep<T> implements Step<T> {
    private static PassThruStep<?> instance=new PassThruStep<>();
    
    /**
     * Get (the only) instance.
     * @param <U> The type to be returned
     * @return the instance
     */
    @SuppressWarnings("unchecked")
    public static <U> PassThruStep<U> instance(){
        return (PassThruStep<U>)instance;
    }
    
    /**
     * Call the supplier and return the result.
     * @param supplier Called to get the result
     * @return the result from {@code supplier}
     * @throws SQLException if {@code supplier} does.
     */
    public T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        return supplier.get();
    }
    
    private PassThruStep(){}
}
