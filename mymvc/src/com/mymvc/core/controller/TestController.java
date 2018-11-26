package com.mymvc.core.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.mymvc.annotation.MyAutowired;
import com.mymvc.annotation.MyController;
import com.mymvc.annotation.MyRequestMapping;
import com.mymvc.annotation.MyRequestParam;
import com.mymvc.annotation.MyResponsebody;
import com.mymvc.core.pojo.User;
import com.mymvc.core.serviceImpl.UserServiceImpl;

@MyController
@MyRequestMapping("/test")
public class TestController {
	
	@MyAutowired("user")
	public UserServiceImpl userServiceImpl;

   

    
     
     @MyRequestMapping("/string")
     //@MyResponsebody
     public String stringTest(HttpServletRequest request, HttpServletResponse response){
         Map<String, String> map = new HashMap<String, String>();
         map.put("123", "454");

    	 return "MyHtml";
     }
     @MyRequestMapping("/json")
     @MyResponsebody
     public  Map<String, Object> jsonTest(HttpServletRequest request, HttpServletResponse response){
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("123", "454");
         User user = new User();
         map.put("user", user);

        
    	 return map;
     }
     
     @MyRequestMapping("/userservice")
     @MyResponsebody
     public  User userservice(HttpServletRequest request, HttpServletResponse response){
    	 User user = userServiceImpl.getUser();
        

        
        return user;
     }
     
     @MyRequestMapping("param")
     @MyResponsebody
     public Map paramtest(@MyRequestParam("d")String b, Integer a, Integer c,HttpServletRequest request){
    	 Map<String ,Object> map = new HashMap<String, Object>();
    	 map.put("a", a);
    	 map.put("b", b);
    	 map.put("c", c);
    	 
    	 return map;
     }
     
     
}
