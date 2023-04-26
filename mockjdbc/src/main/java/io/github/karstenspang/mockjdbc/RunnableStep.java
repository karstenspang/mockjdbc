package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link Step} that runs a different {@link SQLRunnable} than the one in the wrap.
 */
public class RunnableStep implements Step {
    private SQLRunnable runnable;
    
    /**
     * Construct the step.
     * @param runnable runnable to run instead of the one in the wrap.
     * @throws NullPointerException if {@code runnable} is {@code null}.
     */
    public RunnableStep(SQLRunnable runnable){
        this.runnable=Objects.requireNonNull(runnable,"runnable is null");
    }
    
    /**
     * Always throws {@link IllegalArgumentException}, as there is no value to return.
     * @param <T> The type returned by this method.
     * @param supplier Not used.
     * @throws IllegalArgumentException always
     * @return nothing
     */
    @Override
    public <T> T apply(SQLSupplier<? extends T> supplier)
    {
        throw new IllegalArgumentException("no value to return");
    }
    
    /**
     * Run the runnable stored by the constructor.
     * @param action not used
     * @throws SQLException if the stored runnable does.
     */
    @Override
    public void apply(SQLRunnable action)
        throws SQLException
    {
        runnable.run();
    }
    
    @Override
    public String toString(){
        return "RunnableStep";
    }
}
