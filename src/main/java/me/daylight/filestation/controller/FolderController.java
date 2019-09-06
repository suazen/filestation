package me.daylight.filestation.controller;

import me.daylight.filestation.authority.SessionUtil;
import me.daylight.filestation.model.dto.BaseResponse;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;
import me.daylight.filestation.service.FileService;
import me.daylight.filestation.util.FileUtil;
import me.daylight.filestation.util.RetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daylight
 * @date 2018/12/30 23:38
 */
@Controller
@RequestMapping("/folder")
public class FolderController {
    private final FileService fileService;

    @Autowired
    public FolderController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping("/create")
    @ResponseBody
    public BaseResponse createFolder(long parentId,String title){
        User loginUser=(User) SessionUtil.getAttribute("user");

        Folder mFolder=fileService.findFolderById(parentId);

        Folder folder=new Folder();
        folder.setTitle(checkName(title,mFolder));
        folder.setParent(mFolder);
        folder.setLevel(mFolder.getLevel()+1);
        folder=fileService.createOrUpdateFolder(folder);
        mFolder.getChildren().add(folder);
        mFolder.setLeaf(false);
        fileService.createOrUpdateFolder(mFolder);

        StringBuilder path=new StringBuilder("/");
        while(folder.getLevel()!=0){
            path.insert(0, "/" + folder.getTitle());
            folder=folder.getParent();
        }
        path.insert(0,loginUser.getAccount());
        FileUtil.createDirIfNotExists(path.toString());
        return RetResponse.success();
    }

    @RequestMapping("/del")
    @ResponseBody
    public BaseResponse delFolder(long id){
        User loginUser=(User)SessionUtil.getAttribute("user");
        Folder folder=fileService.findFolderById(id);
        StringBuilder path= new StringBuilder("/");
        while(folder.getLevel()!=0){
            path.insert(0, "/" + folder.getTitle());
            folder=folder.getParent();
        }
        path.insert(0,loginUser.getAccount());
        FileUtil.delFolder(path.toString());
        fileService.delFolderById(id);
        return RetResponse.success();
    }

    @RequestMapping("/edit")
    @ResponseBody
    public BaseResponse editFolder(long id,String title){
        User loginUser=(User)SessionUtil.getAttribute("user");
        Folder folder=fileService.findFolderById(id);
        Folder mFolder=folder;
        if (fileService.checkFolderName(title,folder.getParent()))
            return RetResponse.error("该文件夹名称已存在");
        StringBuilder path= new StringBuilder("/");
        folder=folder.getParent();
        while(folder.getLevel()!=0){
            path.insert(0, "/" + folder.getTitle());
            folder=folder.getParent();
        }
        path.insert(0,loginUser.getAccount());
        FileUtil.renameFile(path.toString(),mFolder.getTitle(),title);
        mFolder.setTitle(title);
        fileService.createOrUpdateFolder(mFolder);
        return RetResponse.success();
    }

    @RequestMapping("/findMyFolder")
    @ResponseBody
    public BaseResponse getAllFolder(){
        User loginUser=(User)SessionUtil.getAttribute("user");
        Folder folder=fileService.findFolderById(loginUser.getFolderId());
        List<Folder> folders=new ArrayList<>();
        folders.add(folder);
        return RetResponse.success(folders);
    }

    @RequestMapping("/{id}")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id) throws IOException {
        if (fileService.isFolderExist(id)) {
            User loginUser=(User)SessionUtil.getAttribute("user");

            Folder folder = fileService.findFolderById(id);
            String folderName = folder.getTitle()+".zip";

            StringBuilder path = new StringBuilder("/");
            while (folder.getLevel() != 0) {
                path.insert(0, "/" + folder.getTitle());
                folder = folder.getParent();
            }
            path.insert(0, loginUser.getAccount());

            FileUtil.compress(path.toString(), "compress/"+folderName);
            File file = new File(FileUtil.absolutePath, FileUtil.staticDir + "compress/" + folderName);
            if (file.exists()) {
                return ResponseEntity
                        .ok()
                        .header("Content-Disposition", "attachment;fileName=" + new String((folderName).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1))
                        .contentLength(file.length())
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(new FileSystemResource(file));
            }
        }
        return ResponseEntity.notFound().build();
    }

    private String checkName(String folderName,Folder mFolder){
        int count=1;
        while (fileService.checkFolderName(folderName,mFolder)){
            if (folderName.matches(".*\\([1-9]+[0-9]*\\)")) {
                count = Integer.parseInt(folderName.substring(folderName.lastIndexOf("(")+1,folderName.length()-1))+1;
                folderName=folderName.substring(0,folderName.length()-3)+"("+count+")";
            }else{
                folderName+="("+count+")";
            }
            count++;
        }
        return folderName;
    }
}
