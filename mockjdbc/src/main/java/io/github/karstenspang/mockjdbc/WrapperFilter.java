package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A filter that wraps its input with a {@link Supplier}{@code <}{@link Step}{@code >}; using a {@link Wrapper}.
 * @param <W> The wrapped type, e.g. {@link Connection}.
 * @see WrapperStep
 */
public class WrapperFilter<W> implements SQLFilter<W> {
    private final Wrapper<W> wrapper;
    private final Supplier<Step> stepSupplier;
    
    /**
     * Construct the filter, storing the wrapper and the supplier of steps for use in {@link #apply}.
     * @param wrapper Creates the wrap.
     * @param stepSupplier The supplier of steps to pass to {@code wrapper}.
     * @throws NullPointerException if {@code wrapper} or {@code stepSupplier} is {@code null}.
     */
    public WrapperFilter(Wrapper<W> wrapper,Supplier<Step> stepSupplier){
        this.wrapper=Objects.requireNonNull(wrapper,"wrapper is null");
        this.stepSupplier=Objects.requireNonNull(stepSupplier,"stepSupplier is null");
    }
    
    /**
     * Wrap the input with the stored program using the stored wrapper.
     * @param input input to be wrapped.
     * @return the resulting wrap.
     */
    @Override
    public W apply(W input){
        return wrapper.wrap(input,stepSupplier);
    }
}
