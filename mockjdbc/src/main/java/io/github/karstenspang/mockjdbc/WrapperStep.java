package io.github.karstenspang.mockjdbc;

import java.sql.SQLException;

/**
 * {@link Step} that wraps the suplied value.
 * @param <W> The wrapped type.
 */
public class WrapperStep<W> implements Step {
    private Wrapper<W> wrapper;
    private Program program;
    
    /**
     * Construct the step.
     * @param wrapper Creates the wrap
     * @param program The steps to pass to {@code wrapper}
     */
    public WrapperStep(Wrapper<W> wrapper,Iterable<Step> program){
        this.wrapper=wrapper;
        this.program=new Program(program);
    }
    
    /**
     * Call the supplier, wrap the result, and return the wrap.
     * @param <T> The type returned by {[@code supplier} and this method. Must match <code>&lt;W&gt;</code>.
     * @param supplier Called to get the result
     * @return wrapped result from {@code supplier}
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
        return super.toString()+
            "{wrapper:"+String.valueOf(wrapper)+
            ",program:"+String.valueOf(program)+
            "}";
    }
}
