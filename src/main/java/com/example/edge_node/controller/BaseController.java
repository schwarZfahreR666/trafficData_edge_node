package com.example.edge_node.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * Create by zhangran
 */
@Controller
public class BaseController {
    @RequestMapping({"/index","/"})
    public String toIndex(Model model){
        return "index";
    }

    @RequestMapping({"/list"})
    public String toList(Model model){
        return "list";
    }


    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }
    @PostMapping("/login")
    public Object login(HttpServletRequest req, Model model){
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        //封装用户的登录数据
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
        //执行登录方法
        try{
            subject.login(token);
            RedirectView redirectTarget = new RedirectView();
            redirectTarget.setContextRelative(true);
            redirectTarget.setUrl("/");
            return redirectTarget;
        }catch(UnknownAccountException e){//用户名不存在
            model.addAttribute("msg","用户名错误");
            return "login";
        }catch(IncorrectCredentialsException e){//密码错误
            model.addAttribute("msg","密码错误");
            return "login";
        }

    }
    @RequestMapping("/noauth")
    @ResponseBody
    public String unAuthorized(){
        return "未经授权无法访问此页面！";
    }
}
