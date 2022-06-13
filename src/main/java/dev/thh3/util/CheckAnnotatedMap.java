package dev.thh3.util;

import cn.hutool.json.JSONUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CheckAnnotatedMap {

    public static void main(String[] args) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        map.put("field", "abc");
        map.put("foo", "bar");
        System.out.println(JSONUtil.toJsonStr(map));
        AnnotatedMap<String, Object> map1 = new AnnotatedMap<>(map);
        Field declaredField = map1.getClass().getDeclaredFields()[0];
        declaredField.setAccessible(true);
        declaredField.set(map1, "hellomix");
        map = map1;

        System.out.println("key=field,value=" + map.get("field"));
        System.out.println("key=foo,value=" + map.get("foo"));
    }
}
