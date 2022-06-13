package dev.thh3.util;
/**
 * 缘起MyBatis拦截器 setParameter对于Map参数不容易增强处理
 * 扩展Map，模仿DTO形式补充注解，桥接get/put等重要方法以达成DTO加注入的效果
 * 注意需要给扩展Map补充构造函数
 */