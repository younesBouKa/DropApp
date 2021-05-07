package server.tools;

import java.util.*;
import java.util.function.Predicate;

public class Cache<Key,Resource> {

    private final static Object lock = new Object();
    private final static Map<String,Cache> caches = new HashMap<>();

    private final int MAX_CACHE_KEYS = 50;
    private final int MAX_CACHE_RES = 100;
    private final Map<Key, Set<Resource>> cache = new HashMap<>();

    private Cache(){}

    public static Cache getInstance(String name){
        synchronized (lock){
            Cache cache = caches.getOrDefault(name, new Cache());
            return cache;
        }
    }

    public Resource get(Key key, Resource resource){
        return get(key, resource::equals);
    }

    public Resource get(Key key, Predicate<Resource> searchPredicate){
        Set<Resource> resourceSet = cache.getOrDefault(key, new HashSet<>());
        return resourceSet
                .stream()
                .filter(Objects::nonNull)
                .filter(searchPredicate)
                .findFirst()
                .orElse(null);
    }

    public void add(Key key, Resource resource){
        Set<Resource> resourceSet = cache.getOrDefault(key, new HashSet<>());
        // limit resources
        if(resourceSet.size()>MAX_CACHE_RES){
            Resource resToDelete = resourceSet
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(resource1 -> !resource1.equals(resource))
                    .findFirst()
                    .orElse(null);
            resourceSet.remove(resToDelete);
        }
        resourceSet.add(resource);
        if(cache.keySet().size()>MAX_CACHE_KEYS){
            Key keyToDelete = cache.keySet()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(key1 -> !key1.equals(key) || cache.get(key).isEmpty())
                    .findFirst()
                    .orElse(null);
            cache.remove(keyToDelete);
        }
        cache.put(key, resourceSet);
    }

    public boolean delete(Key key, Resource resource){
        return delete(key, resource::equals);
    }

    public boolean delete(Key key, Predicate<Resource> deletePredicate){
        Set<Resource> resourceSet = cache.getOrDefault(key, new HashSet<>());
        return resourceSet.removeIf(deletePredicate);
    }

    public Resource update(Key key, Resource resource){
        return update(key, resource, resource::equals);
    }

    public Resource update(Key key, Resource resource, Predicate<Resource> searchPredicate){
        // TODO to see later
        Set<Resource> resourceSet = cache.getOrDefault(key, new HashSet<>());
        resourceSet.removeIf(searchPredicate);
        add(key, resource);
        return get(key, searchPredicate);
    }

    public boolean clearAll(Key key){
        return cache.remove(key)!=null;
    }

    public boolean clear(Key key, Predicate<Resource> deletePredicate){
        return cache.getOrDefault(key, new HashSet<>()).removeIf(deletePredicate);
    }

    public int clear(Predicate<Resource> deletePredicate){
        int deleted = 0;
        for (Key key : cache.keySet()){
            deleted += clear(key, deletePredicate)? 1 : 0;
        }
        return deleted;
    }

}
