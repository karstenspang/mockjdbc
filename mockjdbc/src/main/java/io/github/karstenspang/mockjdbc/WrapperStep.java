package io.github.karstenspang.mockjdbc;

import io.github.karstenspang.mockjdbc.wrap.ConnectionWrap;
import java.sql.Connection;
import java.util.List;

/**
 * {@link Step} that wraps the supplied value.
 * @param <W> The wrapped type, e.g. {@link Connection}.
 */
public class WrapperStep<W> extends FilterStep<W> {
    private final Iterable<Step> program;
    
    /**
     * Construct the step. The step is a {@link FilterStep} with a
     * {@link WrapperFilter}.
     * @param wrapper The wrapper passed to the {@link WrapperFilter}.
     *        Usually a function reference to the constructor of
     *        a wrap class, e.g. {@link ConnectionWrap}{@code ::new}.
     * @param program The steps passed to the {@link WrapperFilter}
     *        to use in {@code wrapper}. Usually a 
     *        {@link List}&lt;{@link Step}&gt;, but it could also be, for example,
     *        an object that intelligently generates steps on the fly.
     * @throws NullPointerException if {@code wrapper} or {@code program} is {@code null}.
     */
    public WrapperStep(Wrapper<W> wrapper,Iterable<Step> program){
        super(new WrapperFilter<>(wrapper,program));
        this.program=program;
    }
    
    @Override
    public String toString(){
        return "WrapperStep: "+String.valueOf(program);
    }
}
