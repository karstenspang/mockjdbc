package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * {@link Step} used when the program needs to throw an exception.
 */
public class ExceptionStep implements Step {
    SQLException exception;
    
    /**
     * Construct the step
     * @param exception Exception to be thrown by the step
     */
    public ExceptionStep(SQLException exception)
    {
        this.exception=exception;
    }
    
    /**
     * Throw the exception supplied to the constructor.
     * @param <T> The type of data returned by the step.
     * @param supplier Not called
     * @return None
     * @throws SQLException as requested.
     */
    public <T> T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        throw exception;
    }
    
    /**
     * Throw the exception supplied to the constructor.
     * @param action Not called.
     * @throws SQLException as requested.
     */
    public void apply(SQLRunnable action)
        throws SQLException
    {
        throw exception;
    }
    
    /**
     * The string represetation of the step
     * @return the string represetation
     */
    public String toString(){
        return super.toString()+
            "{exception:"+String.valueOf(exception)+"}";
    }
}
