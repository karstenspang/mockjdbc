package com.github.karstenspang.mockjdbc;

import java.util.function.Consumer;
import java.util.Iterator;
import java.util.Spliterator;

/**
 * A program is an {@link Iterable} returning {@link Step}s.
 * If the defined list is exhausted, {@link PassThruStep}s
 * are returned by {@link Iterator#next} of the {@link Iterator}
 * returned by {@link #iterator}.
 * <p>
 * Note: Since indefinitely many steps are returned by
 * {@link Iterator#next}, the methods
 * {@link #forEach}, {@link #spliterator}, and
 * {@link Iterator#forEachRemaining} and {@link Iterator#hasNext}
 * of the returned {@link Iterator}
 * only take the supplied steps into account.
 */
public class Program<T> implements Iterable<Step<T>> {
    private Iterable<Step<T>> rawProgram;
    public Program(Iterable<Step<T>> rawProgram){
        this.rawProgram=rawProgram;
    }
    
    @Override
    public Iterator<Step<T>> iterator(){
        return new IteratorExtender<Step<T>>(rawProgram.iterator(),PassThruStep.<T>instance());
    }
    
    private static class IteratorExtender<U> implements Iterator<U>
    {
        private final Iterator<U> rawIterator;
        private final U tailValue;
        public IteratorExtender(Iterator<U> rawIterator,U tailValue){
            this.rawIterator=rawIterator;
            this.tailValue=tailValue;
        }
        @Override
        public U next(){
            if (rawIterator.hasNext()) return rawIterator.next();
            return tailValue;
        }
        @Override
        public boolean hasNext(){return rawIterator.hasNext();}
    
        @Override
        public void forEachRemaining​(Consumer<? super U> action){
            rawIterator.forEachRemaining(action);
        }
    }
    
    @Override
    public void forEach​(Consumer<? super Step<T>> action){
        rawProgram.forEach(action);
    }
    
    @Override
    public Spliterator<Step<T>> spliterator(){
        return rawProgram.spliterator();
    }
    
    @Override
    public String toString(){
        return super.toString()+
        "{rawProgram:"+String.valueOf(rawProgram)+"}";
    }
}
