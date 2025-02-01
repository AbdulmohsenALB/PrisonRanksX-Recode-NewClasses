package me.prisonranksx.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class InitHashMaps<K, V> {

    private Map<Object, Object> map;
    private Object lastKey;
    private Function<V, V> vFunction;
    private Function<K, K> kFunction;

    public InitHashMaps() {
        map = new HashMap<>();
    }

    public InitHashMaps<K, V> preprocessKey(Function<K, K> kFunction) {
        this.kFunction = kFunction;
        return this;
    }

    public InitHashMaps<K, V> key(K object) {
        map.put(kFunction != null ? kFunction.apply(object) : object, null);
        lastKey = object;
        return this;
    }

    public InitHashMaps<K, V> preprocessValue(Function<V, V> vFunction) {
        this.vFunction = vFunction;
        return this;
    }


    public InitHashMaps<K, V> value(V object) {
        map.put(lastKey, vFunction != null ? vFunction.apply(object) : object);
        return this;
    }

    public InitHashMaps<K, V> put(K k, V v) {
        map.put(kFunction != null ? kFunction.apply(k) : k, vFunction != null ? vFunction.apply(v) : v);
        lastKey = k;
        return this;
    }

    public Map<K, V> putLast(K k, V v) {
        map.put(kFunction != null ? kFunction.apply(k) : k, vFunction != null ? vFunction.apply(v) : v);
        lastKey = k;
        return finish();
    }

    public <K, V> Map<K, V> finish() {
        return (Map<K, V>) map;
    }


    public static <K, V> InitHashMaps<K, V> init() {
        return new InitHashMaps<>();
    }

    public static <K, V> InitHashMaps<K, V> init(Class<K> keyType, Class<V> valueType) {
        return new InitHashMaps<>();
    }

    public static <K, V> InitHashMaps<K, V> withKeyPreProcess(Function<K, K> keyPreAdditionFunction) {
        return new InitHashMaps().preprocessKey(keyPreAdditionFunction);
    }

    public static <K, V> InitHashMaps<K, V> withValuePreProcess(Function<V, V> valuePreAdditionFunction) {
        return new InitHashMaps<K, V>().preprocessValue(valuePreAdditionFunction);
    }

    public static <K, V> InitHashMaps<K, V> withKeyValuePreProcess(Function<K, K> keyPreAdditionFunction, Function<V, V> valuePreAdditionFunction) {
        return new InitHashMaps<K, V>().preprocessKey(keyPreAdditionFunction).preprocessValue(valuePreAdditionFunction);
    }

    public static <K, V> InitHashMaps<K, V> putFirst(Function<K, K> keyPreAdditionFunction, K k, V v) {
        return new InitHashMaps<K, V>().preprocessKey(keyPreAdditionFunction).put(k, v);
    }

    public static <K, V> InitHashMaps<K, V> putFirst(Function<K, K> keyPreAdditionFunction, Function<V, V> valuePreAdditionFunction, K k, V v) {
        return new InitHashMaps<K, V>().preprocessKey(keyPreAdditionFunction).preprocessValue(valuePreAdditionFunction).put(k, v);
    }

    public static <K, V> InitHashMaps<K, V> putFirst(K k, V v) {
        return new InitHashMaps<K, V>().put(k, v);
    }

    public static <K, V> HashMap<K, V> newMap(Function<V, V> vFunction, Object... obj) {
        InitHashMaps<K, V> initHashMaps = new InitHashMaps<>();
        for (int i = 0; i < obj.length; i += 2) {
            initHashMaps.map.put(obj[i], vFunction.apply((V) obj[i + 1]));
        }
        return (HashMap<K, V>) initHashMaps.map;
    }

    public static <K, V> HashMap<K, V> newMap(Function<K, K> kFunction, Function<V, V> vFunction, Object... obj) {
        InitHashMaps<K, V> initHashMaps = new InitHashMaps<>();
        for (int i = 0; i < obj.length; i += 2) {
            initHashMaps.map.put(
                    kFunction != null ? kFunction.apply((K) obj[i]) : obj[i], vFunction != null ? vFunction.apply((V) obj[i + 1]) : obj[i + 1]);
        }
        return (HashMap<K, V>) initHashMaps.map;
    }

    public static <K, V> Map<K, List<V>> newKeyToListMap(Predicate<Object> keyPredicate, Object... obj) {
        K currentKey = null;
        List<V> currentValueList = null;
        Map<K, List<V>> result = new HashMap<>();

        for (Object item : obj) {
            if (keyPredicate.test(item)) {
                if (currentValueList != null) {
                    result.put(currentKey, currentValueList);
                }
                currentKey = (K) item;
                currentValueList = new ArrayList<>();
            } else {
                currentValueList.add((V) item);
            }
        }

        if (currentValueList != null) {
            result.put(currentKey, currentValueList);
        }

        return result;
    }

    public static <K, V> Map<K, List<V>> newKeyToListMap(Predicate<Object> keyPredicate, Function<List<V>, List<V>> vFunction, Object... obj) {
        K currentKey = null;
        List<V> currentValueList = null;
        Map<K, List<V>> result = new HashMap<>();

        for (Object item : obj) {
            if (keyPredicate.test(item)) {
                if (currentValueList != null) {
                    result.put(currentKey, vFunction.apply(currentValueList));
                }
                currentKey = (K) item;
                currentValueList = new ArrayList<>();
            } else {
                currentValueList.add((V) item);
            }
        }

        if (currentValueList != null) {
            result.put(currentKey, vFunction.apply(currentValueList));
        }

        return result;
    }

    public static <K, V> Map<K, V> of(Object... obj) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < obj.length; i += 2) {
            map.put((K) obj[i], (V) obj[i + 1]);
        }
        return map;
    }

    public static <K, V> Map<K, V> ofIndexFunctions(int size, Function<Integer, K> kIndexFunction, Function<Integer, V> vIndexFunction) {
        return ofIndexFunctions(0, size, kIndexFunction, vIndexFunction);
    }

    public static <K, V> Map<K, V> ofIndexFunctions(int startIndex, int size, Function<Integer, K> kIndexFunction, Function<Integer, V> vIndexFunction) {
        Map<K, V> map = new HashMap<>();
        for (int i = startIndex; i < size + startIndex; i++) {
            map.put(kIndexFunction.apply(i), vIndexFunction.apply(i));
        }
        return map;
    }

    public static <K, V> Map<K, V> ofIndexFunctionsLinked(int startIndex, int size, Function<Integer, K> kIndexFunction, Function<Integer, V> vIndexFunction) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = startIndex; i < size + startIndex; i++) {
            map.put(kIndexFunction.apply(i), vIndexFunction.apply(i));
        }
        return map;
    }

}
