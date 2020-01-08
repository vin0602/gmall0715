package com.atguigu.gmall.passpost.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passpost.config.JwtUtil;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PasspostController {

    @Value("${token.key}")
    private String key;
    @Reference
    private UserService userService;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){

        if (userInfo!=null){
            UserInfo loginUser = userService.login(userInfo);
            if (loginUser == null){
                return "fail";
            }else {
                //生成token
                Map map = new HashMap();
                map.put("userId",loginUser.getId());
                map.put("nickName",loginUser.getNickName());
                // String salt = 192.168.182.132
                String salt = request.getHeader("X-forwarded-for");
                String token = JwtUtil.encode(key,map,salt);
                return token;
            }
        }

        return "fail";
    }

    //验证功能方法
    // 直接将token ，salt 以参数的形式传入到控制器
    // http://passport.atguigu.com/verify?token=xxxx&salt=xxx
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        // 从token 中获取userId  --- {解密token}  Map<String, Object> map1 = JwtUtil.decode(token, key, salt);
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");


        Map <String, Object> map = JwtUtil.decode(token, key, salt);
        //判断
        if (map!=null && map.size()>0){
            //token中解密出来的userId
            String userId = (String) map.get("userId");
            //调用服务层
            UserInfo userInfo = userService.verify(userId);

            if (userInfo!=null){
                return "success";
            }
        }

        return "fail";
    }



}
