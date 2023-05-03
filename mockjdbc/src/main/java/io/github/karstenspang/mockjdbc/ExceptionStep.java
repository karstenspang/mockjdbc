package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link Step} used when the program needs to throw an exception.
 * {@link SQLException} and {@link RuntimeException} are supported.
 */
public class ExceptionStep implements Step {
    private SQLException exception;
    private RuntimeException runtimeException;
    
    /**
     * Construct the step
     * @param exception Exception to be thrown by the step
     */
    public ExceptionStep(SQLException exception)
    {
        this.exception=Objects.requireNonNull(exception,"exception is null");
        this.runtimeException=null;
    }
    
    /**
     * Construct the step
     * @param exception Exception to be thrown by the step
     */
    public ExceptionStep(RuntimeException exception)
    {
        this.exception=null;
        this.runtimeException=Objects.requireNonNull(exception,"exception is null");
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
        if (exception!=null) throw exception;
        throw runtimeException;
    }
    
    /**
     * Throw the exception supplied to the constructor.
     * @param action Not called.
     * @throws SQLException as requested.
     */
    public void apply(SQLRunnable action)
        throws SQLException
    {
        if (exception!=null) throw exception;
        throw runtimeException;
    }
    
    /**
     * The string represetation of the step
     * @return the string represetation
     */
    public String toString(){
        return "ExceptionStep: "+(exception!=null?String.valueOf(exception):String.valueOf(runtimeException));
    }
}
