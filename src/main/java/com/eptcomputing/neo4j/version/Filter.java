package com.eptcomputing.neo4j.version;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * General-purpose filter for Iterables. Create subclass and implement the <tt>condition</tt>
 * method to create a new Iterable containing only those elements for which the condition
 * is true. Like the <tt>filter</tt> function in proper programming languages!
 */
abstract class Filter<T> implements Iterable<T> {

    private final Iterable<T> input;

    public Filter(Iterable<T> input) {
        this.input = input;
    }

    abstract boolean condition(T element);

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private Iterator<T> source = input.iterator();
            private T next = null;

            public boolean hasNext() {
                if (next != null) return true;
                while (source.hasNext()) {
                    next = source.next();
                    if (condition(next)) return true;
                }
                next = null;
                return false;
            }

            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T result = next;
                next = null;
                return result;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
