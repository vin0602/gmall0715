package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    //进入控制器之前执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //登录成功的时候，https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6InR0IiwidXNlcklkIjoiMSJ9.uJ-PLHlCqkT3-55h3fiD2bKWB7GsACDDVG6RR9CJo-I
        String token = request.getParameter("newToken");
        if (token!=null){
            //将token放入cookie中
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        // 当用户登录成之后，那么用户是否可以继续访问其他业务？
        // 是 商品检索：http://list.gmall.com/list.html?catalog3Id=61
        if(token==null){
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        // 当token 真正不为空的时候，解密用户昵称
        if (token!=null){
            // 解密token 即可！
            // token = eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.3Z6Lwc4nZ3FDIOx-SEkd_hiImKqCrB-reiYFB6X8RBo
            Map map = getUserMapByToken(token);
            String  nickName = (String) map.get("nickName");
            //保存到作用域中
            request.setAttribute("nickName",nickName);
        }
        //获取用户访问的控制器上是否有注解  @LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //有注解
        if (methodAnnotation!=null){
            // 直接认证！用户是否登录！ http://passport.atguigu.com/verify?token=xxxx&salt=xxx
            String salt = request.getHeader("X-forwarded-for");
            // 远程调用！
            // http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.3Z6Lwc4nZ3FDIOx-SEkd_hiImKqCrB-reiYFB6X8RBo&salt=192.168.67.1
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if ("success".equals(result)){
                //用户已经登录状态
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                //保存到作用域
                request.setAttribute("userId",userId);
                //放行
                return true;
            }else {
                // 当LoginRequire的注解中的属性autoRedirect =true 时必须登录！
                if (methodAnnotation.autoRedirect()){
                    // 应该跳转到登录页面！http://item.gmall.com/37.html -----> http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F37.html
                    // 得到用户访问的url 路径
                    String requestURL = request.getRequestURL().toString();
                    System.out.println(requestURL);// http://item.gmall.com/37.html
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println(encodeURL);// http%3A%2F%2Fitem.gmall.com%2F37.html

                    //重定向
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    //拦截
                    return  false;
                }
            }
        }

        return true;
    }

    //解密
    private Map getUserMapByToken(String token) {
        // token = eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.3Z6Lwc4nZ3FDIOx-SEkd_hiImKqCrB-reiYFB6X8RBo
        // 解密 得到token 的中间部分
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        System.out.println(tokenUserInfo);
        // 通过   Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);
        // byte --- String --- Map  类型转换
        String tokenJson = new String(bytes);
        JSONObject jsonObject = JSON.parseObject(tokenJson);

        return jsonObject;
    }


    //进入控制器之后，返回视图之前执行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //视图渲染之后，执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }


}
