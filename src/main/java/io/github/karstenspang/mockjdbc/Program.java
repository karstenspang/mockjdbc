package io.github.karstenspang.mockjdbc;

import java.sql.Connection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

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
 * only take the steps supplied to the constructor into account.
 */
public class Program implements Iterable<Step> {
    private Iterable<Step> rawProgram;
    
    /**
     * Construct the program from steps.
     * @param rawProgram the steps
     */
    public Program(Iterable<Step> rawProgram){
        this.rawProgram=rawProgram;
    }
    
    /**
     * Iterator over the program. The iterator has the twist
     * that {@link Iterator#next} keeps on returning
     * {@link PassThruStep}s, even if {@link Iterator#hasNext}
     * returns {@code false}, and thus never throws
     * {@link NoSuchElementException}.
     * @return The iterator
     */
    @Override
    public Iterator<Step> iterator(){
        return new IteratorExtender<Step>(rawProgram.iterator(),PassThruStep.instance());
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
    
    /**
     * Performs the given action for each element of the
     * raw program supplied to the constructor.
     * @param action The action to be performed for each element
     */
    @Override
    public void forEach​(Consumer<? super Step> action){
        rawProgram.forEach(action);
    }
    
    /**
     * Creates a {@link Spliterator} over the elements described by
     * the raw program supplied to the constructor.
     * @return the spliterator
     */
    @Override
    public Spliterator<Step> spliterator(){
        return rawProgram.spliterator();
    }
    
    /**
     * The string represetation of the program.
     * @return the string represetation
     */
    @Override
    public String toString(){
        return super.toString()+
        "{rawProgram:"+String.valueOf(rawProgram)+"}";
    }
}
