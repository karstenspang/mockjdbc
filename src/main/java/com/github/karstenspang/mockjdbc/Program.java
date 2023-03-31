package com.github.karstenspang.mockjdbc;

import java.util.function.Consumer;
import java.util.Iterator;
import java.util.Spliterator;

/**
 * A program is an {@link Iterable} returning {@link Step}s.
 * If the defined list is exhausted, {@link PassThruStep}s
 * are returned.
 * <p>
 * Note: Do not iterate over all elements. This will run forever.
 * Consequently, {@link #forEach}, {@link #spliterator}, and
 * {@link Iterator#forEachRemaining} of the returned {@link Iterator}
 * all throw {@link UnsupportedOperationException}.
 */
public class Program<T> implements Iterable<Step<? extends T>> {
    private Iterable<Step<? extends T>> rawProgram;
    public Program(Iterable<Step<? extends T>> rawProgram){
        this.rawProgram=rawProgram;
    }
    
    public Iterator<Step<? extends T>> iterator(){
        return new IteratorExtender<Step<? extends T>>(rawProgram.iterator(),PassThruStep.<T>instance());
    }
    
    private static class IteratorExtender<U> implements Iterator<U>
    {
        private Iterator<? extends U> rawIterator;
        private U tailValue;
        public IteratorExtender(Iterator<? extends U> rawIterator,U tailvalue){
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
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void forEach​(Consumer<? super Step<? extends T>> action){
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Spliterator<Step<? extends T>> spliterator(){
        throw new UnsupportedOperationException();
    }
}
