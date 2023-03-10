package com.example.edge_node.controller;

import com.example.edge_node.pojo.SysMonitor;
import com.example.edge_node.service.ContainerService;
import com.example.edge_node.service.ImageService;
import com.example.edge_node.service.MonitorService;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.net.SocketException;
import java.util.List;

/**
 * Create by zhangran
 */
@Controller
public class BaseController {
    @Autowired
    MonitorService monitorService;
    @Autowired
    ImageService imageService;
    @Autowired
    ContainerService containerService;

    @RequestMapping({"/index","/"})
    public String toIndex(Model model){
        try {
            SysMonitor sysInfo = monitorService.SystemMonitor();
            model.addAttribute("sysInfo",sysInfo);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "nodewatch";
    }

    @RequestMapping({"/imageslist"})
    public String toImagesList(Model model){
        List<Image> images = imageService.list();
        model.addAttribute("images",images);
        return "Imageslist";
    }

    @RequestMapping({"/ctnlist"})
    public String toCtnList(Model model){
        List<Container> containers = containerService.list();
        model.addAttribute("containers",containers);
        return "Containerslist";
    }




    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }
    @PostMapping("/login")
    public Object login(HttpServletRequest req, Model model){
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        //??????????????????
        Subject subject = SecurityUtils.getSubject();
        //???????????????????????????
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
        //??????????????????
        try{
            subject.login(token);
            RedirectView redirectTarget = new RedirectView();
            redirectTarget.setContextRelative(true);
            redirectTarget.setUrl("/");
            return redirectTarget;
        }catch(UnknownAccountException e){//??????????????????
            model.addAttribute("msg","???????????????");
            return "login";
        }catch(IncorrectCredentialsException e){//????????????
            model.addAttribute("msg","????????????");
            return "login";
        }

    }
    @RequestMapping("/noauth")
    @ResponseBody
    public String unAuthorized(){
        return "????????????????????????????????????";
    }
}
