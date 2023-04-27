package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.util.Objects;

/**
 * A filter that wraps its input with a {@link Program} using a {@link Wrapper}.
 * @param <W> The wrapped type, e.g. {@link Connection}.
 * @see WrapperStep
 */
public class WrapperFilter<W> implements SQLFilter<W> {
    private final Wrapper<W> wrapper;
    private final Program program;
    
    /**
     * Construct the filter, storing the wrapper and the program for use in {@link #apply}.
     * @param wrapper Creates the wrap.
     * @param program The steps to pass to {@code wrapper}.
     * @throws NullPointerException if {@code wrapper} or {@code program} is {@code null}.
     */
    public WrapperFilter(Wrapper<W> wrapper,Iterable<Step> program){
        this.wrapper=Objects.requireNonNull(wrapper,"wrapper is null");
        this.program=new Program(Objects.requireNonNull(program,"program is null"));
    }
    
    /**
     * Wrap the input with the stored program using the stored wrapper.
     * @param input input to be wrapped.
     * @return the resulting wrap.
     */
    @Override
    public W apply(W input){
        return wrapper.wrap(input,program);
    }
}
