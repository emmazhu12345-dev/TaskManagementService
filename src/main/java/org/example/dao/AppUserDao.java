package org.example.dao;

import org.example.model.AppUser;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.*;
import java.util.*;

@RegisterBeanMapper(AppUser.class)
public interface AppUserDao {
    @SqlUpdate("""
        INSERT INTO app_user(username, email, password_hash)
        VALUES(:username, :email, :passwordHash)
    """)
    @GetGeneratedKeys("id")
    long insertReturnId(@BindBean AppUser user);

    @SqlQuery("SELECT * FROM app_user WHERE username = :username")
    Optional<AppUser> findByUsername(@Bind("username") String username);

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM app_user WHERE username = :username)")
    boolean existsByUsername(@Bind("username") String username);

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM app_user WHERE email = :email)")
    boolean existsByEmail(@Bind("email") String email);

    @SqlQuery("SELECT * FROM app_user ORDER BY id")
    List<AppUser> findAll();

    @SqlQuery("SELECT id FROM app_user WHERE username = :username")
    Optional<Long> findIdByUsername(@Bind("username") String username);
}
