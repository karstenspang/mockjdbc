package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;
import java.util.Objects;

/**
 * {@link Step} that runs a different {@link SQLRunnable} than the one passed by the wrap.
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
     * Always throws {@link UnsupportedOperationException}, as there is no value to return.
     * @param <T> The type returned by this method.
     * @param method Not used.
     * @throws UnsupportedOperationException always
     * @return nothing
     */
    @Override
    public <T> T apply(SQLSupplier<? extends T> method)
    {
        throw new UnsupportedOperationException("no value to return");
    }
    
    /**
     * Run the runnable stored by the constructor.
     * @param method not used
     * @throws SQLException if the stored runnable does.
     */
    @Override
    public void apply(SQLRunnable method)
        throws SQLException
    {
        runnable.run();
    }
    
    @Override
    public String toString(){
        return "RunnableStep";
    }
}
