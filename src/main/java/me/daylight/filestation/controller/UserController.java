package me.daylight.filestation.controller;

import me.daylight.filestation.authority.ReturnType;
import me.daylight.filestation.authority.Limit;
import me.daylight.filestation.authority.SessionUtil;
import me.daylight.filestation.model.dto.BaseResponse;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;
import me.daylight.filestation.service.FileService;
import me.daylight.filestation.service.UserService;
import me.daylight.filestation.util.RetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daylight
 * @date 2018/12/30 16:12
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    private final FileService fileService;

    @Autowired
    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @RequestMapping("/login")
    public BaseResponse login(String account, String password){
        if (!userService.isUserExisted(account))
            return RetResponse.error("用户不存在！");
        if (!userService.login(account, password))
            return RetResponse.error("密码错误！");
        SessionUtil.newSession();
        SessionUtil.setAttribute("user",userService.findUserByAccount(account));
        return RetResponse.success();
    }

    @RequestMapping("/register")
    public BaseResponse register(User user){
        if (userService.isUserExisted(user.getAccount()))
            return RetResponse.error("账号已存在！");
        Folder folder=new Folder();
        folder.setTitle(user.getName());
        folder.setLevel(0);
        user.setFolderId(fileService.createOrUpdateFolder(folder).getId());
        return RetResponse.success(userService.register(user).getId());
    }

    @Limit(returnType = ReturnType.Json)
    @RequestMapping("/getSelfInfo")
    public BaseResponse getSelfInfo(){
        return RetResponse.success(SessionUtil.getAttribute("user"));
    }
}
