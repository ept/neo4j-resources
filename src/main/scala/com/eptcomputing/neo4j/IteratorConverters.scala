package com.eptcomputing.neo4j

/**
 * Extend your class with this trait to get implicit conversion between Java and Scala
 * Iterators/Iterables.
 */
trait IteratorConverters {
  /** Implicitly convert a Java iterable to a Scala iterator. */
  protected implicit def java2scala[T](iter: java.lang.Iterable[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter.iterator)
  
  /** Implicitly convert a Java iterator to a Scala iterator. */
  protected implicit def java2scala[T](iter: java.util.Iterator[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter)
}
