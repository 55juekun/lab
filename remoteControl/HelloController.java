package com.firstspringboot.spring.web;

import com.firstspringboot.spring.socket.MySocket;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/** @RestController 注解相当于 @ResponseBody ＋ @Controller
 * @ResponseBody 会导致强制返回预定义的数据类型，从而无法跳转
 * 如下方的 time()方法会返回 "time"字符，而不是跳转到 time.jsp，
 * 适用于固定返回json或者其他数据类型的场景
 */
@RestController
@RequestMapping(value = "/hello")
public class HelloController {
    @RequestMapping(value = "/hello/{id}/{str}")
    public String hello(@PathVariable("id") int id,@PathVariable("str") String str) throws IOException {
        MySocket.sendMsgToClient(id,str);
        return "good";
    }
    @RequestMapping(value = "/a")
    public String stady() throws Exception {
        throw new Exception("some Exception");
    }

}
