package com.zyy.rpc.common;

import lombok.Data;

@Data
public class RpcRequest {

    private String requestId;

    private String className;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;
}
