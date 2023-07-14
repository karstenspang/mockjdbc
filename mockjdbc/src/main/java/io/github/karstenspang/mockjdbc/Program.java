package io.github.karstenspang.mockjdbc;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@link Supplier} returning {@link Step}s
 * in order from a list passed to the constructor.
 * If the list is exhausted, {@link PassThruStep}s
 * are returned by {@link #get}.
 */
public class Program implements Supplier<Step> {
    private final Iterable<Step> steps;
    private final Iterator<Step> it;
    
    /**
     * Construct the program from steps.
     * @param steps the steps
     * @throws NullPointerException if {@code steps} is {@code null}.
     */
    public Program(Iterable<Step> steps){
        this.steps=Objects.requireNonNull(steps,"steps is null");
        it=steps.iterator();
    }
    
    /**
     * Get the next step
     * @return the next step in the list supplied to the constructor.
     *         If the list is exhausted, return a {@link PassThruStep}.
     */
    @Override
    public Step get(){
        if (it.hasNext()) return it.next();
        return PassThruStep.instance();
    }
    
    /**
     * The string represetation of the steps.
     * @return the string represetation
     */
    @Override
    public String toString(){
        return String.valueOf(steps);
    }
}
