package me.daylight.filestation.service.impl;

import me.daylight.filestation.model.dao.FileRepository;
import me.daylight.filestation.model.dao.FolderRepository;
import me.daylight.filestation.model.entity.File;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;
import me.daylight.filestation.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Daylight
 * @date 2018/12/30 16:29
 */
@Service("fileService")
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Autowired
    public FileServiceImpl(FileRepository fileRepository, FolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    @Override
    public File save(File file) {
        return fileRepository.saveAndFlush(file);
    }

    @Override
    public boolean isFileExist(long id) {
        return fileRepository.existsById(id);
    }

    @Override
    public File getFile(long id) {
        return fileRepository.getOne(id);
    }

    @Override
    public void deleteFile(long id) {

        fileRepository.deleteById(id);
    }

    @Override
    public void shareFile(long id) {
        fileRepository.setFileShared(id,true);
    }

    @Override
    public void unsharedFile(long id) {
        fileRepository.setFileShared(id,false);
    }

    @Override
    public List<File> findAllSharedFiles() {
        return fileRepository.findFilesBySharedTrue();
    }

    @Override
    public List<File> findMyFilesByFolder(User user,Folder folder) {
        return fileRepository.findFilesByUploaderAndFolder(user,folder);
    }

    @Override
    public List<File> findMySharedFiles(User user) {
        return fileRepository.findFilesByUploaderAndSharedTrue(user);
    }

    @Override
    public Folder createOrUpdateFolder(Folder folder) {
        return folderRepository.saveAndFlush(folder);
    }

    @Override
    public Folder findFolderById(long id) {
        return folderRepository.getOne(id);
    }

    @Override
    public boolean isFolderExist(Long id) {
        return folderRepository.existsById(id);
    }

    @Override
    public void delFolderById(long id) {
        folderRepository.deleteById(id);
    }

    @Override
    public boolean checkMd5(String md5) {
        return fileRepository.existsByMd5(md5);
    }

    @Override
    public Long getIdByMd5(String md5) {
        return fileRepository.findFirstByMd5(md5).getId();
    }

    @Override
    public boolean checkFileName(String fileName,Folder folder) {
        return fileRepository.existsByFileNameAndFolder(fileName,folder);
    }

    @Override
    public boolean checkFolderName(String title, Folder parentFolder) {
        return folderRepository.existsByTitleAndParent(title, parentFolder);
    }

    @Override
    public void deleteFileFromFolder(Long fileId, Long folderId) {
        folderRepository.deleteFileFromFolder(fileId,folderId);
    }
}
