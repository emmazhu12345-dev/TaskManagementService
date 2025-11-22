package org.example.service;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.model.AppUser;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;
    private final Cache<String, AppUser> userByUsernameCache;

    /** Cache-Aside read with "find" semantics. Return Optional to keep it flexible. */
    public Optional<AppUser> findByUsernameCached(String username) {
        AppUser user = userByUsernameCache.get(
                username, key -> userRepository.findByUsername(key).orElse(null));
        return Optional.ofNullable(user);
    }

    /** For places that expect "throw if not found", like Spring Security. */
    public AppUser getByUsernameOrThrow(String username) {
        return findByUsernameCached(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /** Invalidate cache entry when user data is changed (password, role, active flag, etc.). */
    // TODO: homework, update role , update user operaiton, should call this.
    public void evictByUsername(String username) {
        userByUsernameCache.invalidate(username);
    }

    /** Optional: if you sometimes need to bypass cache (e.g., for admin tools). */
    public Optional<AppUser> findByUsernameDirect(String username) {
        return userRepository.findByUsername(username);
    }
}
