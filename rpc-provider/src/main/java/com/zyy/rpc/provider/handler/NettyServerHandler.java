package com.zyy.rpc.provider.handler;

import com.alibaba.fastjson.JSON;
import com.zyy.rpc.common.RpcRequest;
import com.zyy.rpc.common.RpcResponse;
import com.zyy.rpc.provider.anno.RpcService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


/**
 * 自定义业务处理类
 * 1.通过ApplicationContextAware接口查找标有@RpcService注解的Bean并缓存
 * 2.接收客户端的请求
 * 3.根据传递过来的BeanName从缓存中查找
 * 4.通过cglib反射调用Bean的方法
 * 5.响应客户端
 */
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {

    static  Map<String, Object> SERVICE_INSTANCE_MAP = new HashMap<>();
    /**
     * 1.将标有@RpcService注解的Bean进行缓存
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //1.1 通过注解获取bean的集合
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        //1.2 循环遍历
        for (Object serviceBean : serviceMap.values()){
            if(serviceBean.getClass().getInterfaces().length == 0){
                throw new RuntimeException("对外暴露的服务必须实现接口");
            }
            //默认处理第一个接口作为缓存Bean的名字
            String serviceName = serviceBean.getClass().getInterfaces()[0].getName();
            SERVICE_INSTANCE_MAP.put(serviceName, serviceBean);
        }
        System.out.println(SERVICE_INSTANCE_MAP);

    }

    /**
     * 通道读取就绪事件--读取客户端的消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 2.接收客户端的请求
        RpcRequest rpcRequest = JSON.parseObject(msg, RpcRequest.class);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        //业务处理
        try {
            rpcResponse.setResult(handler(rpcRequest));
        }catch (Exception e){
            e.printStackTrace();
            rpcResponse.setError(e.getMessage());
        }

        //5.响应客户端
        ctx.writeAndFlush(JSON.toJSONString(rpcResponse));
    }

    private Object handler(RpcRequest rpcRequest) throws InvocationTargetException {
        //3.根据传递过来的BeanName从缓存中查找
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());
        if(serviceBean == null){
            throw new RuntimeException("服务端没有找到服务");
        }

        //4.通过cglib反射调用Bean的方法
        FastClass proxyClass = FastClass.create(serviceBean.getClass());
        FastMethod method = proxyClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        return method.invoke(serviceBean, rpcRequest.getParameters());
    }

}
