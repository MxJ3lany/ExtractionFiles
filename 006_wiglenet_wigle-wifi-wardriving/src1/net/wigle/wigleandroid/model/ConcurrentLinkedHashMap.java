package net.wigle.wigleandroid.model;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * try to be all things to all people. a remove-eldest feature, concurrent except during put,
 * which is usually single-threaded in this app anyway.
 * @param <K> key
 * @param <V> value
 */
public final class ConcurrentLinkedHashMap<K,V> implements Map<K,V> {
    private final ConcurrentHashMap<K,V> map;
    private final LinkedBlockingQueue<K> queue;
    private int count = 0;

    private final int maxSize;
    private final Object WRITE_LOCK = new Object();

    public ConcurrentLinkedHashMap() {
        this( Integer.MAX_VALUE );
    }

    public ConcurrentLinkedHashMap( final int maxSize ) {
        map = new ConcurrentHashMap<>();
        queue = new LinkedBlockingQueue<>();
        this.maxSize = maxSize;
    }

    public V put( final K key, final V value ) {
        V previous;
        synchronized( WRITE_LOCK ) {
            previous = map.put(key, value);
            if ( previous == null ) {
                // new key! add to queue
                queue.add( key );
                // check if this puts us over
                if ( count + 1 > maxSize ) {
                    // remove eldest
                    K eldest = queue.remove();
                    map.remove( eldest );
                }
                else {
                    // this doesn't put us over, just add to the count
                    count++;
                }
            }
        }
        return previous;
    }

    public void putAll(Map<? extends K,? extends V> map) {
    }

    public V remove( Object key ) {
        synchronized( WRITE_LOCK ) {
            return map.remove(key);
        }
    }

    public V get( Object key ) {
        return map.get( key );
    }

    public void clear() {
        map.clear();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return null;
    }

    /**
     * make sure this is only used for reading (we only use it for reading currently.) the map is concurrent safe, but it will bugger
     * our internal accounting for size() if the set is mutated
     */
    public Set<Map.Entry<K,V>> entrySet() {
        return map.entrySet();
    }

    public Collection<V> values() {
        return map.values();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
      return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    public int size() {
        return count;
    }

    public boolean isFull() {
        return count >= maxSize;
    }

    public int maxSize() {
        return maxSize;
    }

}
