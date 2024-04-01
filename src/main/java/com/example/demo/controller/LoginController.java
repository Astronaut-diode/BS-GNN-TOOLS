package com.example.demo.controller;

import com.example.demo.bean.User;
import com.example.demo.dao.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private LoginMapper loginMapper;

    /**
     * 根据请求地址{localhost:8080/index}访问项目的首页。
     *
     * @return
     */
    @GetMapping("index")
    public String index() {
        return "login";
    }

    /**
     * 根据请求地址{localhost:8080/login}访问项目的首页。
     *
     * @return
     */
    @GetMapping("login")
    public String login() {
        return "login";
    }

    /**
     * 根据请求地址{localhost:8080/register}访问项目的首页。
     *
     * @return
     */
    @GetMapping("register")
    public String register() {
        return "register";
    }

    @PostMapping("login")
    @ResponseBody
    public Map<String, Object> login(@RequestParam(name = "username") String username,
                        @RequestParam(name = "password") String password) {
        User user = loginMapper.selectUser(username, password);
        Map<String, Object> response = new HashMap<>();
        if(user == null) {
            response.put("success", false);
            response.put("message", "登陆失败");
        } else {
            response.put("success", true);
            response.put("message", "登陆成功");
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
        }
        return response;
    }

    @PostMapping("register")
    @ResponseBody
    public Map<String, Object> register(@RequestParam(name = "username") String username,
                                        @RequestParam(name = "password") String password) {
        User user = new User(username, password);
        Map<String, Object> response = new HashMap<>();
        if(loginMapper.selectUserByUsername(username)) {  // 代表已经被使用过了。
            response.put("success", false);
            response.put("message", "注册失败");
        } else {
            loginMapper.insertUser(user);
            response.put("success", true);
            response.put("message", "注册成功");
        }
        return response;
    }
}
