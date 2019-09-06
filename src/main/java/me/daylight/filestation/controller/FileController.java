package me.daylight.filestation.controller;

import me.daylight.filestation.authority.ReturnType;
import me.daylight.filestation.authority.Limit;
import me.daylight.filestation.authority.SessionUtil;
import me.daylight.filestation.model.dto.BaseResponse;
import me.daylight.filestation.model.dto.TableDto;
import me.daylight.filestation.model.entity.File;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daylight
 * @date 2018/12/30 16:12
 */
@Controller
@Limit(returnType = ReturnType.Json)
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    private ConcurrentHashMap<String,String> md5Map=new ConcurrentHashMap<>();

    @RequestMapping("/opera")
    @ResponseBody
    public BaseResponse operaFile(String action,Long fileId, String fileName, Long fileSize, Long folderId) {
        User loginUser=(User) SessionUtil.getAttribute("user");

        if (fileService.isFileExist(fileId)){
            File file=fileService.getFile(fileId);

            if (fileName==null||"".equals(fileName))
                fileName=file.getFileName();

            StringBuilder path= new StringBuilder("/");
            Folder folder=fileService.findFolderById(folderId);
            Folder mFolder=folder;
            while(folder.getLevel()!=0){
                path.insert(0, "/" + folder.getTitle());
                folder=folder.getParent();
            }
            path.insert(0,loginUser.getAccount());

            fileName=checkName(fileName,mFolder);

            if ("copy".equals(action)) {
                File myFile = new File(path.toString(), fileName, fileSize == null ? file.getFileSize() : fileSize, mFolder, loginUser, file.getMd5());
                FileUtil.copyFile(file.getPath() + file.getFileName(), path.toString(), fileName);

                myFile = fileService.save(myFile);
                mFolder.getFiles().add(myFile);
                fileService.createOrUpdateFolder(mFolder);
                return RetResponse.success(myFile.getId());
            }else if ("move".equals(action)){
                FileUtil.moveFile(file.getPath()+file.getFileName(),path.toString(),fileName);
                file.setPath(path.toString());
                file.setFileName(fileName);
                fileService.deleteFileFromFolder(fileId,file.getFolder().getId());
                file.setFolder(mFolder);
                fileService.save(file);
                mFolder.getFiles().add(file);
                fileService.createOrUpdateFolder(mFolder);
                return RetResponse.success(file.getId());
            }
            return RetResponse.error("no such action");

        }
        return RetResponse.error("文件不存在！");
    }

    @RequestMapping("/upload/{folderId:[0-9]*}")
    @ResponseBody
    public BaseResponse upload(String id,@PathVariable Long folderId,@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty())
            return RetResponse.error();

        User loginUser=(User)SessionUtil.getAttribute("user");

        String md5=md5Map.get(id);
        md5Map.remove(id);

        StringBuilder path= new StringBuilder("/");
        Folder folder=fileService.findFolderById(folderId==null?loginUser.getFolderId():folderId);
        Folder mFolder=folder;
        while(folder.getLevel()!=0){
            path.insert(0, "/" + folder.getTitle());
            folder=folder.getParent();
        }
        path.insert(0,loginUser.getAccount());

        String fileName=checkName(file.getOriginalFilename(),mFolder);

        File myFile=new File(path.toString(),fileName,file.getSize(),mFolder,loginUser,md5);
        FileUtil.upload(file,myFile.getPath(), fileName);

        myFile=fileService.save(myFile);
        myFile.getFolder().getFiles().add(myFile);
        fileService.createOrUpdateFolder(myFile.getFolder());
        return RetResponse.success(myFile.getId());
    }

    private String checkName(String fileName,Folder mFolder){
        int count=1;
        while (fileService.checkFileName(fileName,mFolder)){
            String name=fileName.substring(0,fileName.length()-FileUtil.getSuffix(fileName).length());
            if (name.matches(".*\\([1-9]+[0-9]*\\)")) {
                count = Integer.parseInt(name.substring(name.lastIndexOf("(")+1,name.length()-1))+1;
                name=name.substring(0,name.length()-3)+"("+count+")";
            }else{
                name+="("+count+")";
            }
            count++;
            fileName=name+FileUtil.getSuffix(fileName);
        }
        return fileName;
    }

    @RequestMapping("/md5/{folderId:[0-9]*}")
    @ResponseBody
    public BaseResponse md5Check(String id,String fileName,Long fileSize,String md5,@PathVariable Long folderId){
        if (fileService.checkMd5(md5)) {
            Long fileId=fileService.getIdByMd5(md5);
            operaFile("copy",fileId,fileName,fileSize,folderId);
            return RetResponse.success();
        }
        md5Map.put(id,md5);
        return RetResponse.error();
    }

    @RequestMapping("/rename")
    @ResponseBody
    public BaseResponse renameFile(long id,String name){
        if (fileService.isFileExist(id)){
            File file=fileService.getFile(id);
            if (fileService.checkFileName(name+FileUtil.getSuffix(file.getFileName()),file.getFolder()))
                return RetResponse.error("该文件名称已存在");
            FileUtil.renameFile(file.getPath(),file.getFileName(),name+FileUtil.getSuffix(file.getFileName()));
            file.setFileName(name+FileUtil.getSuffix(file.getFileName()));
            fileService.save(file);
            return RetResponse.success();
        }
        return RetResponse.error();
    }

    @RequestMapping("/delete")
    @ResponseBody
    public BaseResponse deleteFile(long id){
        if (fileService.isFileExist(id)) {
            File file = fileService.getFile(id);
            if (FileUtil.delete(file.getPath()+ file.getFileName())) {
                fileService.deleteFileFromFolder(id,file.getFolder().getId());
                fileService.deleteFile(id);
                return RetResponse.success();
            }
        }
        return RetResponse.error("文件不存在！");
    }

    @Limit(loginRequired = false)
    @RequestMapping("/{id:.+}/{name:.+}")
    public ResponseEntity<FileSystemResource> download(@PathVariable long id,@PathVariable String name,boolean preview) throws IOException {
        if (fileService.isFileExist(id)) {
            File myFile = fileService.getFile(id);

            User loginUser=(User)SessionUtil.getAttribute("user");
            if (!myFile.isShared()&& !loginUser.getId().equals(myFile.getUploader().getId()))
                return ResponseEntity.badRequest().build();

            String filename = myFile.getFileName();
            String path=myFile.getPath();
            FileUtil.createDirIfNotExists(path);
            java.io.File file = new java.io.File(FileUtil.absolutePath, FileUtil.staticDir+path+ filename);
            if (file.exists()) {
                if (preview){
                    if (!FileUtil.getSuffix(filename).toLowerCase().equals(".pdf"))
                        file=FileUtil.previewFile(file,filename+".pdf");
                    return ResponseEntity
                            .ok()
                            .contentLength(file.length())
                            .contentType(MediaType.parseMediaType("application/pdf"))
                            .body(new FileSystemResource(file));
                }
                return ResponseEntity
                        .ok()
                        .header("Content-Disposition", "attachment;fileName=" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1))
                        .contentLength(file.length())
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(new FileSystemResource(file));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping("/share")
    @ResponseBody
    public BaseResponse share(long id){
        if (!fileService.isFileExist(id))
            return RetResponse.error("文件不存在！");
        fileService.shareFile(id);
        return RetResponse.success();
    }

    @RequestMapping("/unshared")
    @ResponseBody
    public BaseResponse unshared(long id){
        if (!fileService.isFileExist(id))
            return RetResponse.error("文件不存在！");
        fileService.unsharedFile(id);
        return RetResponse.success();
    }

    @Limit(loginRequired = false)
    @RequestMapping("/sharedList")
    @ResponseBody
    public BaseResponse sharedFileList(){
        List<File> files=fileService.findAllSharedFiles();
        Map<String,Object> map=new HashMap<>();
        map.put("data",files);
        map.put("count",files.size());
        return RetResponse.success(map);
    }

    @RequestMapping("/list")
    @ResponseBody
    public BaseResponse fileList(Long folderId){
        User loginUser=(User)SessionUtil.getAttribute("user");
        Folder folder;
        if (folderId==null){
            folder=fileService.findFolderById(loginUser.getFolderId());
        }else
            folder=fileService.findFolderById(folderId);
        List<TableDto.FileTable> tableList=new ArrayList<>();
        List<Folder> folders=folder.getChildren();
        for (Folder mFolder:folders)
            tableList.add(new TableDto.FileTable(mFolder.getId(),0,mFolder.getTitle(),mFolder.getCreateTime(),null,null));
        List<File> files=folder.getFiles();
        for (File file:files)
            tableList.add(new TableDto.FileTable(file.getId(),1,file.getFileName(),file.getUploadTime(),file.getFileSize(),file.isShared()));
        return RetResponse.success(new TableDto(tableList,tableList.size()));
    }

    @RequestMapping("/mySharedList")
    @ResponseBody
    public BaseResponse mySharedFileList(){
        User loginUser=(User)SessionUtil.getAttribute("user");
        List<File> files=fileService.findMySharedFiles(loginUser);
        return RetResponse.success(new TableDto(files,files.size()));
    }
}
