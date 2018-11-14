package com.liugh.core.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liugh.annotation.MyController;
import com.liugh.annotation.MyRequestMapping;
import com.liugh.annotation.MyRequestParam;
import com.liugh.annotation.MyResponsebody;
import com.liugh.core.pojo.User;

@MyController
@MyRequestMapping("/test")
public class TestController {

     @MyRequestMapping("/doTest")
    public void test1(HttpServletRequest request, HttpServletResponse response,
            @MyRequestParam("param") String param){
        System.out.println(param);
        try {
            response.getWriter().write( "doTest method success! param:"+param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @MyRequestMapping("/doTest2")
    public void test2(HttpServletRequest request, HttpServletResponse response){
        try {
            response.getWriter().println("doTest2 method success!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     
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
