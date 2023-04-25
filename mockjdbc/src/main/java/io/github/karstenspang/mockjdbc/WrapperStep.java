package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * {@link Step} that wraps the supplied value.
 * @param <W> The wrapped type, e.g. {@link Connection}.
 */
public class WrapperStep<W> implements Step {
    private Wrapper<W> wrapper;
    private Program program;
    
    /**
     * Construct the step.
     * @param wrapper Creates the wrap. Usually a function reference to
     *        the constructor of a wrap class, e.g. {@link ConnectionWrap}{@code ::new}.
     * @param program The steps to pass to {@code wrapper}. Usually a 
     *        {@link List}&lt;{@link Step}&gt;, but it could also be, for example,
     *        an object that intelligently generates steps on the fly.
     */
    public WrapperStep(Wrapper<W> wrapper,Iterable<Step> program){
        this.wrapper=wrapper;
        this.program=new Program(program);
    }
    
    /**
     * Call the supplier, wrap the result, and return the wrap.
     * @param <T> The type returned by {@code supplier} and this method. Must match <code>&lt;W&gt;</code>.
     * @param supplier Called to get the object to wrap.
     * @return wrapped result from {@code supplier}.
     * @throws SQLException if {@code supplier} does.
     * @throws ClassCastException if <code>&lt;W&gt;</code> and <code>&lt;T&gt;</code> do not match.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(SQLSupplier<? extends T> supplier)
        throws SQLException
    {
        return (T)wrapper.wrap((W)supplier.get(),program);
    }
    
    /**
     * Always throws {@link IllegalArgumentException}, as there is no way to wrap {@code void}.
     * @param action not used
     * @throws IllegalArgumentException always
     */
    @Override
    public void apply(SQLRunnable action)
    {
        throw new IllegalArgumentException("void cannot be wrapped");
    }
    
    @Override
    public String toString(){
        return "WrapperStep: "+String.valueOf(program);
    }
}
