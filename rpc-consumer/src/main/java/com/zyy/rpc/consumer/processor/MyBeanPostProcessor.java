package com.zyy.rpc.consumer.processor;

import com.zyy.rpc.consumer.anno.RpcReference;
import com.zyy.rpc.consumer.proxy.RpcClientProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * bean的后置增强
 * 生成响应的代理并注入Controller的service属性
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    RpcClientProxy rpcClientProxy;

    /**
     * 自定义注解的注入，每个Bean都会执行该方法
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //1.查看Bean的字段中有没有对应的注解
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            //2.查找字段中是否包含注解
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if(annotation != null){
                //3.获取代理对象
                Object proxy = rpcClientProxy.getProxy(field.getType());

                try {
                    //4.属性注入
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return bean;
    }
}
