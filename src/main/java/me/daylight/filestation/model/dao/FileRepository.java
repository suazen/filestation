package me.daylight.filestation.model.dao;

import me.daylight.filestation.model.entity.File;
import me.daylight.filestation.model.entity.Folder;
import me.daylight.filestation.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Daylight
 * @date 2018/12/30 16:19
 */
public interface FileRepository extends JpaRepository<File,Long> {
    @Query("update File file set file.shared=?2 where file.id=?1")
    @Modifying
    @Transactional
    void setFileShared(long id,boolean shared);

    List<File> findFilesBySharedTrue();

    List<File> findFilesByUploaderAndFolder(User uploader, Folder folder);

    List<File> findFilesByUploaderAndSharedTrue(User uploader);

    void deleteFilesByFolder(Folder folder);

    boolean existsByMd5(String md5);

    File findFirstByMd5(String md5);

    boolean existsByFileNameAndFolder(String fileName,Folder folder);
}
