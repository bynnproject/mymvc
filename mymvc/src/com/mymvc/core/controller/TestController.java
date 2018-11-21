package com.mymvc.core.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mymvc.annotation.MyController;
import com.mymvc.annotation.MyRequestMapping;
import com.mymvc.annotation.MyRequestParam;
import com.mymvc.annotation.MyResponsebody;
import com.mymvc.core.pojo.User;

@MyController
@MyRequestMapping("/test")
public class TestController {

     
     
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
     
     
}
