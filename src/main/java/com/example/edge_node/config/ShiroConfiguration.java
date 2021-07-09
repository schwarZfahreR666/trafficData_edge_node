package com.example.edge_node.config;


import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Create by zhangran
 */
@Configuration
public class ShiroConfiguration {
    //ShiroFilterFactoryBean
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") DefaultWebSecurityManager defaultWebSecurityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);
        //添加shiro内置过滤器
        /*
        * anon:无需认证即可访问
        * authc:必须认证才能访问
        * user:必须拥有记住我功能才能用
        * perms:拥有对某个资源的权限才能访问
        * role：拥有某个角色权限才能访问
        * */
        Map<String, String> filterMap = new LinkedHashMap<>();
//        filterMap.put("/user/add","perms[user:add]");
        filterMap.put("/login","anon");
        filterMap.put("/css/*","anon");
        filterMap.put("/js/*","anon");
        filterMap.put("/images/*","anon");
        filterMap.put("/favicon.ico","anon");
        filterMap.put("/**","authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);

        //设置登录页面
        shiroFilterFactoryBean.setLoginUrl("/toLogin");
        //设置未授权页面
//        shiroFilterFactoryBean.setUnauthorizedUrl("/noauth");


        return shiroFilterFactoryBean;
    }

    //DefaultWebSecurityManager
    @Bean(name="securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(@Qualifier("userRealm") UserRealm userRealm){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关联realm
        securityManager.setRealm(userRealm);
        return securityManager;
    }

    //第一步：创建 realm 对象，需要自定义
    @Bean(name="userRealm")
    public UserRealm userRealm(){
        return new UserRealm();
    }

    //整合shiroDialect:用来整合shiro和thymeleaf
    @Bean
    public ShiroDialect getShiroDialect(){
        return new ShiroDialect();
    }


}
