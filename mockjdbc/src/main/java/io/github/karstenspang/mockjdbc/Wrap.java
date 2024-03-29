package io.github.karstenspang.mockjdbc;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Base class for wraps.
 * Implements an interface by passing method calls onto another
 * object implementing the same interface. A {@link Step} from a
 * {@link Supplier} is applied to the method call.
 */
public class Wrap {
    /** The wrapped object */
    protected final Object wrapped;
    /** Steps to apply */
    protected final Supplier<Step> stepSupplier;
    /**
     * Wrap an object
     * @param wrapped Object to wrap
     * @param stepSupplier Supplier of steps to wrap the object with.
     * @throws NullPointerException if {@code wrapped} or {@code stepSupplier} is {@code null}.
     */
    public Wrap(Object wrapped,Supplier<Step> stepSupplier){
        this(Wrap.class.getName(),wrapped,stepSupplier);
    }
    
    /**
     * Wrap an object
     * @param wrapped Object to wrap
     * @param steps Steps to wrap the object with.
     * @throws NullPointerException if {@code wrapped} or {@code steps} is {@code null}.
     */
    public Wrap(Object wrapped,Iterable<Step> steps){
        this(wrapped,new Program(steps));
    }
    
    /**
     * Wrap an object
     * @param className Class of the wrap, for logging purposes.
     * @param wrapped Object to wrap
     * @param stepSupplier Supplier of steps to wrap the object with.
     * @throws NullPointerException if {@code wrapped} or {@code stepSupplier} is {@code null}.
     */
    protected Wrap(String className,Object wrapped,Supplier<Step> stepSupplier){
        this.wrapped=Objects.requireNonNull(wrapped,"wrapped is null");
        this.stepSupplier=Objects.requireNonNull(stepSupplier,"stepSupplier is null");
        Logger logger=Logger.getLogger(className);
        logger.fine("Wrapping "+String.valueOf(wrapped)+" in "+className+ " with step supplier "+String.valueOf(stepSupplier));
    }
    
    /**
     * Check if the wrapped object is equal to another object.
     * @param other object to check. If an instance of {@link Wrap},
     *        recursively, then its wrapped value is checked.
     * @return {@code true} if matched, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other){
        while (other instanceof Wrap) other=((Wrap)other).wrapped;
        return wrapped.equals(other);
    }
    
    /**
     * Hash code
     * @return The hash code of the wrapped object.
     */
    @Override
    public int hashCode(){return wrapped.hashCode();}
    
    /**
     * Get the string representation
     * @return The representation of the actual wrap class,
     *         of the wrapped object, and of the step supplier.
     */
    @Override
    public String toString(){
        return getClass().getName()+
            ":{wrapped:"+wrapped.toString()+
            ",stepSupplier:"+stepSupplier.toString()+"}";
    }
}
