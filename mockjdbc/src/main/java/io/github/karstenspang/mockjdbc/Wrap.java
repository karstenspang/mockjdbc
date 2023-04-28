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
    static Logger logger=Logger.getLogger(Wrap.class.getName());
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
        this(Wrap.class,wrapped,stepSupplier);
    }
    
    /**
     * Wrap an object
     * @param clazz Class of the wrap, for logging purposes.
     * @param wrapped Object to wrap
     * @param stepSupplier Supplier of steps to wrap the object with.
     * @throws NullPointerException if {@code wrapped} or {@code stepSupplier} is {@code null}.
     */
    protected Wrap(Class<? extends Wrap> clazz,Object wrapped,Supplier<Step> stepSupplier){
        this.wrapped=Objects.requireNonNull(wrapped,"wrapped is null");
        this.stepSupplier=Objects.requireNonNull(stepSupplier,"stepSupplier is null");
        Logger logger=Logger.getLogger(clazz.getName());
        logger.fine("Wrapping "+String.valueOf(wrapped)+" in "+clazz.getName()+ " with step supplier "+String.valueOf(stepSupplier));
    }
    
    /**
     * Check if the wrapped value is equal to another object.
     * @param other object to check. If an instance of {@link Wrap},
     *        then its wrapped value is checked.
     * @return {@code true} if matched, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other){
        if (other instanceof Wrap) other=((Wrap)other).wrapped;
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
