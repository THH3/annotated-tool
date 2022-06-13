package dev.thh3.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.json.JSONUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CheckBridge {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        map.put("field", "abc");
        map.put("foo", "bar");
        System.out.println(JSONUtil.toJsonStr(map));
        //map = BridgeUtil.getMap(map, TdTable.class);
        Class<?> clazz = BridgeUtil.buildClass(TdTable.class);
        Constructor<?> constructor = clazz.getConstructor();
        Object instance = constructor.newInstance();
        Map<String,Object> map1 = (Map<String, Object>) instance;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            map1.put(entry.getKey(), entry.getValue());
        }
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object oldValue = field.get(instance);
            oldValue += HexUtil.toHex(System.currentTimeMillis() / 1000);
            field.set(instance, oldValue);
        }
        System.out.println("key=field,value=" + map1.get("field"));
        System.out.println("key=foo,value=" + map1.get("foo"));
    }
}
