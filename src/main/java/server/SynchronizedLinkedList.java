package server;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * A threadsafe list implementation. Normal Lists are not threadsafe.
 * Its just a wrapper, nothing special.
 * @author cotix
 *
 * @param <T>
 */
public class SynchronizedLinkedList<T> implements Iterable<T> {
    private final Lock lock;
    private LinkedList<T> list;

    public SynchronizedLinkedList() {
        list = new LinkedList<T>();
        lock = new ReentrantLock();
    }

    public boolean add(T e) {
        lock.lock();
        boolean res = list.add(e);
        lock.unlock();
        return res;
    }

    public Iterator<T> iterator() {
        lock.lock();
        Iterator<T> it = list.iterator();
        lock.unlock();
        return it;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void removeAll(Collection<T> c) {
        lock.lock();
        list.removeAll(c);
        lock.unlock();
    }

    public String toString() {
        lock.lock();
        String res = list.toString();
        lock.unlock();
        return res;
    }
}
