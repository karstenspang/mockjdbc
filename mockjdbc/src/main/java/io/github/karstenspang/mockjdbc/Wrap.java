package io.github.karstenspang.mockjdbc;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Base class for wraps.
 * Implements an interface by passing method calls onto another
 * object implementing the same interface. A step from a
 * program is applied to the method call.
 */
public class Wrap {
    static Logger logger=Logger.getLogger(Wrap.class.getName());
    /** The wrapped object */
    protected final Object wrapped;
    /** Steps to apply */
    protected final Iterator<Step> steps;
    /**
     * Wrap an object
     * @param wrapped Object to wrap
     * @param program Program to wrap the object with
     */
    public Wrap(Object wrapped,Program program){
        this(Wrap.class,wrapped,program);
    }
    
    /**
     * Wrap an object
     * @param clazz Class of the wrap, for logging purposes.
     * @param wrapped Object to wrap
     * @param program Program to wrap the object with
     */
    protected Wrap(Class<? extends Wrap> clazz,Object wrapped,Program program){
        Logger logger=Logger.getLogger(clazz.getName());
        logger.fine("Wrapping "+String.valueOf(wrapped)+" in "+clazz.getName()+ "with program "+String.valueOf(program));
        this.wrapped=wrapped;
        this.steps=program.iterator();
    }
}
