package com.icecream.server.service;

import com.icecream.server.entity.User;

public interface UserService {
  void save(User user);

  boolean check(User user, String password);

  User findByPhoneNumber(String phoneNumber);
}