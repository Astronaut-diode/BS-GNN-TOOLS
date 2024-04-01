package com.example.demo.dao;

import com.example.demo.bean.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface LoginMapper {
    /**
     * 动态语句插入
     *
     * @param user
     * @return
     */
    void insertUser(User user);

    /**
     * 登录用户
     *
     * @param username
     * @param password
     * @return
     */
    User selectUser(String username, String password);

    /**
     * 查询用户名是否出现了重复
     * @param username
     * @return
     */
    boolean selectUserByUsername(String username);
}