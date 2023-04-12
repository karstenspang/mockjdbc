package io.github.karstenspang.mockjdbc;

import java.util.Iterator;

/**
 * Base class for wraps around interfaces in {@link java.sql}.
 */
public class Wrap {
    /** The wrapped object */
    protected final Object wrapped;
    /** Steps to apply */
    protected final Iterator<Step> steps;
    /**
     * Wrap an object
     * @param wrapped Object to wrap
     * @param program Program to wrap the object with
     */
    protected Wrap(Object wrapped,Program program){
        this.wrapped=wrapped;
        this.steps=program.iterator();
    }
}
