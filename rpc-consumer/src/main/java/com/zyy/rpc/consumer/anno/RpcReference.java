package com.zyy.rpc.consumer.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 引用代理类
 */
@Target(ElementType.FIELD) //作用于字段
@Retention(RetentionPolicy.RUNTIME) //在运行时可以获取得到
public @interface RpcReference {
}
