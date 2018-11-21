package com.mymvc.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import com.alibaba.fastjson.JSONObject;
import com.mymvc.annotation.MyController;
import com.mymvc.annotation.MyRequestMapping;
import com.mymvc.annotation.MyResponsebody;

public class MyDispatcherServlet extends HttpServlet{
	
    //放类名
    private List<String> classNames = new ArrayList<String>();

    //spring的ioc容器
    private Map<String, Object> ioc = new HashMap<String, Object>();
    
    //将扫描带有特定注解的方法放在该map中
    private Map<String, Method> handlerMapping = new  HashMap<String, Method>();

    //将扫描带有contoller注解的类放在该map中
    private Map<String, Object> controllerMap  =new HashMap<String, Object>();

    //寻找该项目静态资源时，用户设定的前缀
    private String suffix ;
    
    //寻找该项目静态资源时，用户设定的后缀
    private String prefix ;
    
    //需要扫描的包名（controllr）
    private String packages ;
    
    @Override
    public void init(ServletConfig config) throws ServletException {

    	//1.初始化基本信息
    	getBasicConfig(config);
    	
        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        scanPackages(this.packages);

        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();

        //4.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //处理请求
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if(handlerMapping.isEmpty()){
            return;
        }

        String url =req.getRequestURI();
        String contextPath = req.getContextPath();

        url=url.replace(contextPath, "").replaceAll("/+", "/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }

        Method method =this.handlerMapping.get(url);

        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //保存参数值
        Object [] paramValues= new Object[parameterTypes.length];

        //方法的参数列表
        for (int i = 0; i<parameterTypes.length; i++){  
            //根据参数名称，做某些处理  
            String requestParam = parameterTypes[i].getSimpleName();  

            if (requestParam.equals("HttpServletRequest")){  
                //参数类型已明确，这边强转类型  
                paramValues[i]=req;
                continue;  
            }  
            if (requestParam.equals("HttpServletResponse")){  
                paramValues[i]=resp;
                continue;  
            }
            if(requestParam.equals("String")){
                for (Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i]=value;
                }
            }
        }  
        //利用反射机制来调用
        try {
           Object object = method.invoke(this.controllerMap.get(url), paramValues);//第一个参数是method所对应的实例 在ioc容器中
           if(method.isAnnotationPresent(MyResponsebody.class)){
        	   if(!(null == object)){
        		   
            	   resp.getWriter().write( JSONObject.toJSONString(object) );  
               }
           }else {
        	   Type type =  method.getGenericReturnType();
        	   if("class java.lang.String".equals(type.toString())){
        		   outputTheFile(req,resp,object.toString());        		   
        	   }
           
           }
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void getBasicConfig(ServletConfig config){
    	
    	this.packages = config.getInitParameter("packagesToScan");
    	this.suffix = config.getInitParameter("suffix");
    	this.prefix = config.getInitParameter("prefix");
    	
    }

  
    private void scanPackages(String packageName) {
        //把所有的.替换成/
        URL url  =this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
                //递归读取包
            	scanPackages(packageName+"."+file.getName());
            }else{
                String className =packageName +"." +file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }   
        for (String className : classNames) {
            try {
                //把类搞出来,反射来实例化(只有加@MyController需要实例化)
                Class<?> clazz =Class.forName(className);
               if(clazz.isAnnotationPresent(MyController.class)){
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                }else{
                    continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void initHandlerMapping(){
        if(ioc.isEmpty()){
            return;
        }
        try {
            for (Entry<String, Object> entry: ioc.entrySet()) {
                Class<? extends Object> clazz = entry.getValue().getClass();
                if(!clazz.isAnnotationPresent(MyController.class)){
                    continue;
                }

                //拼url时,是controller头的url拼上方法上的url
                String baseUrl ="";
                if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                    MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl=annotation.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if(!method.isAnnotationPresent(MyRequestMapping.class)){
                        continue;
                    }
                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = annotation.value();

                    url =(baseUrl+"/"+url).replaceAll("/+", "/");
                    handlerMapping.put(url,method);
                    controllerMap.put(url,clazz.newInstance());
                    System.out.println(url+","+method);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 把字符串的首字母小写
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
    
    /**
     * 将文件输出到前端
     */
    private void outputTheFile(HttpServletRequest request,HttpServletResponse response,
    		String filePath){
    	
    	String uri =request.getRequestURI();
    	String dir = request.getSession(true).getServletContext().getRealPath("/");
    	dir = dir + prefix + "/" +filePath + suffix;
    	response.setContentType("text/html; charset=UTF-8");
    	try {
			InputStream inputStream = new FileInputStream(dir);
			String html = IOUtils.toString(inputStream,"UTF-8");
			PrintWriter out = response.getWriter();
			out.println(html);
			out.flush();
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	
    	
    }
    

}
