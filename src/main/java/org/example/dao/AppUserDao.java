package org.example.dao;

import java.util.List;
import java.util.Optional;
import org.example.model.AppUser;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterBeanMapper(AppUser.class)
public interface AppUserDao {
    @SqlUpdate(
            """
                                INSERT INTO app_user(username, email, password_hash, first_name, last_name, role, is_active)
                                VALUES(:username, :email, :passwordHash, :firstName, :lastName, :role, :active)
                              """)
    @GetGeneratedKeys("id")
    long insertReturnId(@BindBean AppUser user);

    @SqlQuery(
            """
                                SELECT id, username, email, password_hash, first_name, last_name, role,
                                       is_active AS active, created_at, updated_at
                                  FROM app_user
                                 WHERE username = :username
                              """)
    Optional<AppUser> findByUsername(@Bind("username") String username);

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM app_user WHERE username = :username)")
    boolean existsByUsername(@Bind("username") String username);

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM app_user WHERE email = :email)")
    boolean existsByEmail(@Bind("email") String email);

    @SqlQuery("SELECT * FROM app_user ORDER BY id")
    List<AppUser> findAll();

    @SqlQuery("SELECT id FROM app_user WHERE username = :username")
    Optional<Long> findIdByUsername(@Bind("username") String username);

    @SqlUpdate("UPDATE app_user SET role=:role WHERE id=:id")
    int updateRole(@Bind("id") Long id, @Bind("role") String role);

    @SqlUpdate(
            """
                                UPDATE app_user
                                   SET is_active=:active
                                 WHERE id=:id
                              """)
    int updateActive(@Bind("id") Long id, @Bind("active") boolean active);
}
