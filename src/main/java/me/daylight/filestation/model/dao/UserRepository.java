package me.daylight.filestation.model.dao;

import me.daylight.filestation.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Daylight
 * @date 2018/12/30 16:19
 */
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsUserByAccountAndPassword(String account, String password);

    boolean existsUserByAccount(String account);

    User findUserByAccount(String account);

    @Query("update User u set u.password=?2 where u.account=?1")
    @Modifying
    @Transactional
    void changePassword(String account,String password);
}
