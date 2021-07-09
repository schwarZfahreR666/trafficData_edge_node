package com.example.edge_node.config;


import com.example.edge_node.pojo.User;
import com.example.edge_node.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by zhangran
 */
//自定义realm，extends AuthorizingRealm
public class UserRealm extends AuthorizingRealm {

    @Autowired
    UserService userService;

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行了授权方法");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        //拿到当前登录对象
        Subject subject = SecurityUtils.getSubject();
        User currentUser = (User)subject.getPrincipal();

        info.addStringPermission(currentUser.getPerms());
        return info;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        //用户名，密码  数据库中取
        User user = userService.queryUserByName(token.getUsername());


        //用户认证
        if(user == null){
            return null; //抛出未知用户异常
        }
        //密码认证，shiro做
        //传user
        return new SimpleAuthenticationInfo(user,user.getPassword(),"");
    }
}
