package com.musicapp.api.repository;

import com.musicapp.api.model.Track;
import com.musicapp.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT t FROM User u JOIN u.likedTracks t WHERE u.username = :username")
    List<Track> findLikedTracksByUsername(@Param("username") String username);

    @Query("SELECT t.id FROM User u JOIN u.likedTracks t WHERE u.username = :username")
    Set<Long> findLikedTrackIdsByUsername(@Param("username") String username);
}
