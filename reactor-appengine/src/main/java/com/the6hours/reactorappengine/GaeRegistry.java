package com.the6hours.reactorappengine;

import reactor.bus.registry.CachableRegistration;
import reactor.bus.registry.Registration;
import reactor.bus.registry.Registry;
import reactor.bus.selector.Selector;
import reactor.fn.Consumer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Based on {@link reactor.bus.registry.CachingRegistry}. Key difference that HashMap implementation replaces from
 * [unsupported by gae] ConcurrentHashMapV8 with standard {@link ConcurrentHashMap}.
 *
 *
 * @author Igor Artamonov
 */
public class GaeRegistry<K, V> implements Registry<K, V> {

    private final ConcurrentHashMap<Object, List<Registration<K, ? extends V>>> cache = new
            ConcurrentHashMap<>();
    private final ConcurrentHashMap<Selector<K>, List<Registration<K, ? extends V>>> registrations = new
            ConcurrentHashMap<>();

    private final boolean     useCache;
    private final boolean     cacheNotFound;
    private final Consumer<K> onNotFound;

    public GaeRegistry(boolean useCache, boolean cacheNotFound, Consumer<K> onNotFound) {
        this.useCache = useCache;
        this.cacheNotFound = cacheNotFound;
        this.onNotFound = onNotFound;
    }

    @Override
    public synchronized Registration<K, V> register(final Selector<K> sel, V obj) {
        List<Registration<K, ? extends V>> regs;
        if ((regs = registrations.get(sel)) == null) {
            registrations.putIfAbsent(sel, new ArrayList<Registration<K, ? extends V>>());
            regs = registrations.get(sel);
        }

        Registration<K, V> reg = new CachableRegistration<>(sel, obj, new Runnable() {
            @Override
            public void run() {
                registrations.remove(sel);
                cache.clear();
            }
        });
        regs.add(reg);

        return reg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean unregister(Object key) {
        boolean found = false;
        for (Selector sel : registrations.keySet()) {
            if (!sel.matches(key)) {
                continue;
            }
            if (null != registrations.remove(sel) && !found) {
                found = true;
            }
        }
        if (useCache)
            cache.remove(key);
        return found;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Registration<K, ? extends V>> select(final K key) {
        List<Registration<K, ? extends V>> selectedRegs;
        if (null != (selectedRegs = cache.get(key))) {
            return selectedRegs;
        }

        final List<Registration<K, ? extends V>> regs = new ArrayList<Registration<K, ? extends V>>();
        for (Map.Entry<Selector<K>, List<Registration<K, ? extends V>>> entry: registrations.entrySet()) {
            Selector<K> selector = entry.getKey();
            List<Registration<K, ? extends V>> registrations = entry.getValue();
            if (selector.matches(key)) {
                regs.addAll(registrations);
            }
        }

        if (regs.isEmpty() && null != onNotFound) {
            onNotFound.accept(key);
        }
        if (useCache && (!regs.isEmpty() || cacheNotFound)) {
            cache.put(key, regs);
        }

        return regs;
    }

    @Override
    public synchronized void clear() {
        cache.clear();
        registrations.clear();
    }

    @Override
    public synchronized Iterator<Registration<K, ? extends V>> iterator() {
        final List<Registration<K, ? extends V>> regs = new ArrayList<Registration<K, ? extends V>>();
        for (List<Registration<K, ? extends V>> curr: registrations.values()) {
            regs.addAll(curr);
        }
        return regs.iterator();
    }

}
