package me.daylight.filestation.service.impl;

import me.daylight.filestation.model.dao.UserRepository;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;
import me.daylight.filestation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Daylight
 * @date 2018/12/30 16:22
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String account, String password) {
        return userRepository.existsUserByAccountAndPassword(account, password);
    }

    @Override
    public User register(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public boolean isUserExisted(String account) {
        return userRepository.existsUserByAccount(account);
    }

    @Override
    public User findUserByAccount(String account) {
        return userRepository.findUserByAccount(account);
    }
}
