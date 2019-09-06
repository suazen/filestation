package me.daylight.filestation.service;

import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;

/**
 * @author Daylight
 * @date 2018/12/30 16:20
 */
public interface UserService {
    boolean login(String account,String password);

    User register(User user);

    boolean isUserExisted(String account);

    User findUserByAccount(String account);
}
