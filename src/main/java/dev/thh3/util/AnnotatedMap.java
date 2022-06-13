package dev.thh3.util;

import java.util.HashMap;
import java.util.Map;

@MyTable("t_table")
public class AnnotatedMap<K, V> extends HashMap<K,V> {

    public AnnotatedMap() {

    }

    public AnnotatedMap(Map<K, V> m) {
        for (Entry<K, V> kvEntry : m.entrySet()) {
            put(kvEntry.getKey(), kvEntry.getValue());
        }
        //super(m);
    }

    @MyField("t_field")
    private String field;

    @Override
    public V get(Object key) {
        // declared fields with MyField annotation should be cached
        // as define by bytecode tech
//        Field[] declaredFields = this.getClass().getDeclaredFields();
//        for (Field f : declaredFields) {
//            if (f.isAnnotationPresent(MyField.class)) {
//                if (f.getName().equals(key)) {
//                    f.setAccessible(true);
//                    try {
//                        return (V) f.get(this);
//                    } catch (IllegalAccessException e) {
//                        return super.get(key);
//                    }
//                }
//            }
//        }
        if ("field".equals(key)) {
            return (V)"abc1";
        }
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        System.out.println("put k-v");
        if ("field".equals(key)) {
            this.field = String.valueOf(value);
        }
        return super.put(key, value);
    }
}
