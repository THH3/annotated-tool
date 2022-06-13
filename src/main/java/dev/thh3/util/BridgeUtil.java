package dev.thh3.util;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.HexUtil;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BridgeUtil {
    static Map<Class<?>, Class<?>> clazzMap = new HashMap<>();

    public static Class<?> buildClass(Class<?> clazz) {
        MyTable annotation = AnnotationUtil.getAnnotation(clazz, MyTable.class);
        if (annotation == null) {
            return null;
        }
        Class<?> newMapClazz = null;
        synchronized (clazzMap) {
            newMapClazz = clazzMap.get(clazz);
            if (newMapClazz == null) {
                try {
                    ClassPool cp = ClassPool.getDefault();
                    String packageName = clazz.getPackage().getName();
                    String part = HexUtil.toHex(System.currentTimeMillis() / 1000);
                    CtClass ctClazz = cp.makeClass(packageName + ".Td" + part);
                    CtClass superClazz = cp.get(HashMap.class.getName());
                    ctClazz.setSuperclass(superClazz);
                    CtConstructor c1 = new CtConstructor(new CtClass[0], ctClazz);
                    c1.setBody("super();");
                    c1.setModifiers(Modifier.PUBLIC);
                    ctClazz.addConstructor(c1);

                    CtConstructor c2 = new CtConstructor(new CtClass[]{cp.get(Map.class.getName())}, ctClazz);
                    c2.setBody("super($1);");
                    c2.setModifiers(Modifier.PUBLIC);
                    ctClazz.addConstructor(c2);

                    ConstPool constPool = ctClazz.getClassFile().getConstPool();
                    // copy MyTable annotation
                    AnnotationsAttribute clzAnnotationsAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    Annotation tableAnnotation = new Annotation(MyTable.class.getName(), constPool);
                    tableAnnotation.addMemberValue("value", new StringMemberValue(annotation.value(), constPool));
                    clzAnnotationsAttr.addAnnotation(tableAnnotation);
                    ctClazz.getClassFile().addAttribute(clzAnnotationsAttr);
                    // copy MyField annotation
                    Field[] declaredFields = clazz.getDeclaredFields();
                    List<Field> cacheField = new ArrayList<>();
                    for (Field f : declaredFields) {
                        if (f.isAnnotationPresent(MyField.class)) {
                            String name = f.getName();
                            String typeName = f.getType().getTypeName();
                            CtField ctField = CtField.make("private " + typeName + " " + name + ";", ctClazz);
                            AnnotationsAttribute fieldAnnotationsAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                            Annotation fieldAnnotation = new Annotation(MyField.class.getName(), constPool);
                            MyField myField = AnnotationUtil.getAnnotation(f, MyField.class);
                            fieldAnnotation.addMemberValue("value", new StringMemberValue(myField.value(), constPool));
                            fieldAnnotationsAttr.addAnnotation(fieldAnnotation);
                            ctField.getFieldInfo().addAttribute(fieldAnnotationsAttr);
                            ctClazz.addField(ctField);

                            cacheField.add(f);
                        }
                    }
                    // bridge method
                    CtMethod putMethod = new CtMethod(cp.get(Object.class.getName()), "put", new CtClass[]{cp.get(Object.class.getName()),cp.get(Object.class.getName())}, ctClazz);
                    String bodyStr = "";
                    for (Field f : cacheField) {
                        String name = f.getName();
                        String type = f.getType().getTypeName();
                        String str = "else if (\"" + name + "\".equals($1)) {";
                        str += "this." + name + "=(" + type + ")$2;";
                        str += "}";
                        bodyStr += str;
                    }
                    bodyStr = bodyStr.replaceFirst("else ", "");
                    bodyStr += "return super.put($1, $2);";
                    putMethod.setBody( "{" + bodyStr + "}");
                    AnnotationsAttribute putMethodAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    Annotation overrideAnno = new Annotation(Override.class.getName(), constPool);
                    putMethodAttr.addAnnotation(overrideAnno);
                    putMethod.getMethodInfo().addAttribute(putMethodAttr);
                    ctClazz.addMethod(putMethod);

                    CtMethod getMethod = new CtMethod(cp.get(Object.class.getName()), "get", new CtClass[]{cp.get(Object.class.getName())}, ctClazz);
                    String bodyStr2 = "";
                    for (Field f : cacheField) {
                        String name = f.getName();
                        String str = "else if (\"" + name + "\".equals($1)) {";
                        str += "return this." + name + ";";
                        str += "}";
                        bodyStr2 += str;
                    }
                    bodyStr2 = bodyStr2.replaceFirst("else ", "");
                    bodyStr2 += "return super.get($1);";
                    getMethod.setBody( "{" + bodyStr2 + "}");
                    AnnotationsAttribute getMethodAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    //Annotation overrideAnno = new Annotation(Override.class.getName(), constPool);
                    getMethodAttr.addAnnotation(overrideAnno);
                    getMethod.getMethodInfo().addAttribute(getMethodAttr);
                    ctClazz.addMethod(getMethod);


                    newMapClazz = ctClazz.toClass();
                    clazzMap.put(clazz, newMapClazz);
                } catch (NotFoundException | CannotCompileException e) {
                    return null;
                }
            }
        }
        return newMapClazz;
    }
    public static Map<String, Object> getMap(Map<String, Object> oldMap, Class<?> clazz) {
        MyTable annotation = AnnotationUtil.getAnnotation(clazz, MyTable.class);
        if (annotation == null) {
            return oldMap;
        }
        Class<?> newMapClazz = null;
        synchronized (clazzMap) {
            newMapClazz = clazzMap.get(clazz);
            if (newMapClazz == null) {
                try {
                    ClassPool cp = ClassPool.getDefault();
                    String packageName = clazz.getPackage().getName();
                    String part = HexUtil.toHex(System.currentTimeMillis() / 1000);
                    CtClass ctClazz = cp.makeClass(packageName + ".Td" + part);
                    CtClass superClazz = cp.get(HashMap.class.getName());
                    ctClazz.setSuperclass(superClazz);
                    CtConstructor c1 = new CtConstructor(new CtClass[0], ctClazz);
                    c1.setBody("super();");
                    c1.setModifiers(Modifier.PUBLIC);
                    ctClazz.addConstructor(c1);

                    CtConstructor c2 = new CtConstructor(new CtClass[]{cp.get(Map.class.getName())}, ctClazz);
                    c2.setBody("super($1);");
                    c2.setModifiers(Modifier.PUBLIC);
                    ctClazz.addConstructor(c2);

                    ConstPool constPool = ctClazz.getClassFile().getConstPool();
                    // copy MyTable annotation
                    AnnotationsAttribute clzAnnotationsAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    Annotation tableAnnotation = new Annotation(MyTable.class.getName(), constPool);
                    tableAnnotation.addMemberValue("value", new StringMemberValue(annotation.value(), constPool));
                    clzAnnotationsAttr.addAnnotation(tableAnnotation);
                    ctClazz.getClassFile().addAttribute(clzAnnotationsAttr);
                    // copy MyField annotation
                    Field[] declaredFields = clazz.getDeclaredFields();
                    List<Field> cacheField = new ArrayList<>();
                    for (Field f : declaredFields) {
                        if (f.isAnnotationPresent(MyField.class)) {
                            String name = f.getName();
                            String typeName = f.getType().getTypeName();
                            CtField ctField = CtField.make("private " + typeName + " " + name + ";", ctClazz);
                            AnnotationsAttribute fieldAnnotationsAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                            Annotation fieldAnnotation = new Annotation(MyField.class.getName(), constPool);
                            MyField myField = AnnotationUtil.getAnnotation(f, MyField.class);
                            fieldAnnotation.addMemberValue("value", new StringMemberValue(myField.value(), constPool));
                            fieldAnnotationsAttr.addAnnotation(fieldAnnotation);
                            ctField.getFieldInfo().addAttribute(fieldAnnotationsAttr);
                            ctClazz.addField(ctField);

                            cacheField.add(f);
                        }
                    }
                    // bridge method
                    CtMethod putMethod = new CtMethod(cp.get(Object.class.getName()), "put", new CtClass[]{cp.get(Object.class.getName()),cp.get(Object.class.getName())}, ctClazz);
                    String bodyStr = "";
                    for (Field f : cacheField) {
                        String name = f.getName();
                        String type = f.getType().getTypeName();
                        String str = "else if (\"" + name + "\".equals($1)) {";
                        str += "this." + name + "=(" + type + ")$2;";
                        str += "}";
                        bodyStr += str;
                    }
                    bodyStr = bodyStr.replaceFirst("else ", "");
                    bodyStr += "return super.put($1, $2);";
                    putMethod.setBody( "{" + bodyStr + "}");
                    ctClazz.addMethod(putMethod);

                    CtMethod getMethod = new CtMethod(cp.get(Object.class.getName()), "get", new CtClass[]{cp.get(Object.class.getName())}, ctClazz);
                    String bodyStr2 = "";
                    for (Field f : cacheField) {
                        String name = f.getName();
                        String str = "else if (\"" + name + "\".equals($1)) {";
                        str += "return this." + name + ";";
                        str += "}";
                        bodyStr2 += str;
                    }
                    bodyStr2 = bodyStr2.replaceFirst("else ", "");
                    bodyStr2 += "return super.get($1);";
                    getMethod.setBody( "{" + bodyStr2 + "}");
                    ctClazz.addMethod(getMethod);


                    newMapClazz = ctClazz.toClass();
                    clazzMap.put(clazz, newMapClazz);
                } catch (NotFoundException | CannotCompileException e) {
                    return oldMap;
                }
            }
        }
        if (newMapClazz != null) {
            try {
                Constructor<?> constructor = newMapClazz.getConstructor(Map.class);
                Object object = constructor.newInstance(oldMap);
                return (Map<String, Object>) object;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return oldMap;
    }
}
