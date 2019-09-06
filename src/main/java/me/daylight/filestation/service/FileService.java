package me.daylight.filestation.service;

import me.daylight.filestation.model.entity.File;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;

import java.util.List;


/**
 * @author Daylight
 * @date 2018/12/30 16:29
 */
public interface FileService {
    File save(File file);

    boolean isFileExist(long id);

    File getFile(long id);

    void deleteFile(long id);

    void shareFile(long id);

    void unsharedFile(long id);

    List<File> findAllSharedFiles();

    List<File> findMyFilesByFolder(User user,Folder folder);

    List<File> findMySharedFiles(User user);

    Folder createOrUpdateFolder(Folder folder);

    Folder findFolderById(long id);

    boolean isFolderExist(Long id);

    void delFolderById(long id);

    boolean checkMd5(String md5);

    Long getIdByMd5(String md5);

    boolean checkFileName(String fileName,Folder folder);

    boolean checkFolderName(String title,Folder parentFolder);

    void deleteFileFromFolder(Long fileId,Long folderId);
}
