package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import java.sql.Connection;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link Step} that wraps the supplied value.
 * The step is a {@link FilterStep} with a {@link WrapperFilter}.
 * @param <W> The wrapped type, e.g. {@link Connection}.
 */
public class WrapperStep<W> extends FilterStep<W> {
    private final Supplier<Step> stepSupplier;
    
    /**
     * Construct the step from a {@link Supplier}&lt;{@link Step}&gt;.
     * @param wrapper The wrapper passed to the {@link WrapperFilter}.
     *        Usually a function reference to the constructor of
     *        a wrap class, e.g. {@link ConnectionWrap}{@code ::new}.
     * @param stepSupplier The supplier of steps passed to the {@link WrapperFilter}
     *        to use in {@code wrapper}. Could be, for example,
     *        an object that intelligently generates steps on the fly.
     * @throws NullPointerException if {@code wrapper} or {@code stepSupplier} is {@code null}.
     */
    public WrapperStep(Wrapper<W> wrapper,Supplier<Step> stepSupplier){
        super(new WrapperFilter<>(wrapper,stepSupplier));
        this.stepSupplier=stepSupplier;
    }
    
    /**
     * Construct the step from a {@link Iterable}&lt;{@link Step}&gt;.
     * @param wrapper The wrapper passed to the {@link WrapperFilter}.
     *        Usually a function reference to the constructor of
     *        a wrap class, e.g. {@link ConnectionWrap}{@code ::new}.
     * @param steps The steps passed to the {@link WrapperFilter}
     *        to use in {@code wrapper}. Usually a 
     *        {@link List}&lt;{@link Step}&gt;.
     * @throws NullPointerException if {@code wrapper} or {@code steps} is {@code null}.
     */
    public WrapperStep(Wrapper<W> wrapper,Iterable<Step> steps){
        this(wrapper,new Program(steps));
    }
    
    @Override
    public String toString(){
        return "WrapperStep: "+String.valueOf(stepSupplier);
    }
}
