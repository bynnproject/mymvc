package com.mymvc.core.serviceImpl;

import com.mymvc.annotation.MyService;
import com.mymvc.core.pojo.User;
import com.mymvc.core.service.UserService;

@MyService("user")
public class UserServiceImpl implements UserService{
	

	@Override
	public User getUser(){
		User user = new User();
		user.setAge("11");
		user.setName("11");
		return user;
	}
	
}
