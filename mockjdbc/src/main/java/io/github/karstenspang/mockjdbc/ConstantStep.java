package io.github.karstenspang.mockjdbc;

/**
 * {@link Step} that returns a constant value.
 */
public class ConstantStep implements Step {
    private Object value;
    
    /**
     * Construct the step.
     * @param value to be returned by {@link #apply(SQLSupplier)}
     */
    public ConstantStep(Object value){
        this.value=value;
    }
    
    /**
     * Return the value supplied to the constructor.
     * @param <T> The type returned by this method.
     * @param supplier Not used.
     * @return The value supplied to the constructor.
     * @throws ClassCastException if the value supplied to the 
     *         constructor can not be cast to <code>&lt;T&gt;</code>.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(SQLSupplier<? extends T> supplier)
    {
        return (T)value;
    }
    
    /**
     * Does nothing.
     * @param action not used
     */
    @Override
    public void apply(SQLRunnable action)
    {
    }
    
    @Override
    public String toString(){
        return "ConstantStep: "+String.valueOf(value);
    }
}
