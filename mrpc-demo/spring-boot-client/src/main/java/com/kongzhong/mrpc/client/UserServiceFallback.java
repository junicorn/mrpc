package com.kongzhong.mrpc.client;

import org.springframework.stereotype.Component;

/**
 * @author biezhi
 * @date 2017/7/26
 */
@Component("UserServiceFallback")
public class UserServiceFallback {

    public String testHystrix(int errorNum) {
        return "默认返回22";
    }
}
