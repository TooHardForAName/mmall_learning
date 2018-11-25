package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMaper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        //先在数据库中查询该用户名是否存在
        int resultCount = userMaper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //在数据库中验证账号密码是否正确，注意，验证的时候要使用md5加密后的密码进行验证，因为注册的时候就使用的是加密后的密码
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMaper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //到这一步就说明账号密码验证正确，为了安全不能将密码封装到user对象中，所以要将user对象中的password属性置为null。
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> validResponse = checkValid(user.getUsername(), Const.USERNAME);
        //先检查该用户名是否已经被注册
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        //检查邮箱是否已经被注册。注意，因为可以用用户名登录也可以用邮箱登录
        validResponse = checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        //设置用户的权限为普通用户。Role中有（管理员和普通用户），这是一个内部内部接口类
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //为了安全，我们在数据库中不能直接存储用户的明文密码，需要进行加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //将user在数据库中插入，此时插入的user对象的password已经是经过md5加密过后的密码了
        int resultCount = userMaper.insert(user);

        //insert语句返回的是成功的条数，如果为0，则插入未成功。
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        } else {
            return ServerResponse.createBySuccessMessage("注册成功");
        }

    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            //开始判断。如果type是用户名类型，则进行用户名判断
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMaper.checkUsername(str);
                if (resultCount != 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            //如果type是邮箱类型，则进行邮箱验证
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMaper.checkEmail(str);
                if (resultCount != 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }

        } else {
            //输入的类型为空
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }
}
