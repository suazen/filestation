package me.daylight.filestation.model.dao;

import me.daylight.filestation.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Daylight
 * @date 2018/12/30 21:06
 */
public interface FolderRepository extends JpaRepository<Folder,Long> {
    boolean existsByTitleAndParent(String title,Folder parent);

    @Modifying
    @Transactional
    @Query(value = "delete from folder_files where files_id=?1 and folder_id=?2",nativeQuery = true)
    void deleteFileFromFolder(Long fileId,Long folderId);
}
