package me.daylight.filestation.controller;

import me.daylight.filestation.authority.Limit;
import me.daylight.filestation.authority.SessionUtil;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;
import me.daylight.filestation.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Daylight
 * @date 2018/12/30 17:01
 */
@Controller
public class PageController {

    private final FileService fileService;

    @Autowired
    public PageController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping("/")
    public String index(Model model){
        User loginUser=(User)SessionUtil.getAttribute("user");
        if (loginUser!=null) {
            model.addAttribute("user", loginUser);
        }
        model.addAttribute("isUserLogin",loginUser!=null);
        return "index";
    }

    @RequestMapping("/login")
    public String loginPage(){
        return "login";
    }

    @RequestMapping("/register")
    public String registerPage(){
        return "register";
    }

    @RequestMapping("/404Page")
    public String notFoundPage(){
        return "404";
    }

    @RequestMapping("/errorPage")
    public String errorPage(){
        return "error";
    }

    @Limit
    @RequestMapping("/myFiles")
    public String myFilePage(Long folderId,Model model){
        User loginUser=(User) SessionUtil.getAttribute("user");
        List<Folder> folders=new LinkedList<>();
        if (folderId!=null) {
            Folder folder=fileService.findFolderById(folderId);
            while (folder.getParent() != null) {
                folders.add(0, folder);
                folder=folder.getParent();
            }
        }
        folders.add(0,fileService.findFolderById(loginUser.getFolderId()));
        model.addAttribute("folders",folders);
        return "list";
    }

    @RequestMapping("/sharedFiles")
    public String sharedFilesPage(){
        return "sharedFileList";
    }

    @Limit
    @RequestMapping("/myFolderList")
    public String folderList(){
        return "folderList";
    }

}
