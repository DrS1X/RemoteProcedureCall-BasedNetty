package com.zyy.rpc.consumer.controller;

import com.zyy.rpc.api.IUserService;
import com.zyy.rpc.consumer.anno.RpcReference;
import com.zyy.rpc.pojo.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @RpcReference
    IUserService userService;

    @RequestMapping("/getUserById")
    public User getUserById(int id){
        return userService.getById(id);
    }
}
