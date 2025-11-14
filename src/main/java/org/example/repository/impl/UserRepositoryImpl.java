package org.example.repository.impl;

import org.example.dao.AppUserDao;
import org.example.model.AppUser;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Jdbi jdbi;

    public UserRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return jdbi.withExtension(AppUserDao.class, dao -> dao.findByUsername(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return jdbi.withExtension(AppUserDao.class, dao -> dao.existsByUsername(username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jdbi.withExtension(AppUserDao.class, dao -> dao.existsByEmail(email));
    }

    @Override
    public AppUser createUser(AppUser user) {
        // insert only (示例简单化；如需 upsert 可再扩展)
        Long id = jdbi.withExtension(AppUserDao.class, dao -> dao.insertReturnId(user));
        user.setId(id);
        return user;
    }

    @Override
    public List<AppUser> findAll() {
        return jdbi.withExtension(AppUserDao.class, AppUserDao::findAll);
    }

    @Override
    public Optional<Long> findIdByUsername(String username) {
        return jdbi.withExtension(AppUserDao.class, dao -> dao.findIdByUsername(username));
    }

    @Override
    public void setRole(Long userId, Role role) {
        int n = jdbi.withExtension(AppUserDao.class, dao -> dao.updateRole(userId, role.name()));
        if (n == 0) throw new IllegalArgumentException("User not found");
    }

    @Override
    public void setActive(Long userId, boolean active) {
        int n = jdbi.withExtension(AppUserDao.class, dao -> dao.updateActive(userId, active));
        if (n == 0) throw new IllegalArgumentException("User not found");
    }
}
