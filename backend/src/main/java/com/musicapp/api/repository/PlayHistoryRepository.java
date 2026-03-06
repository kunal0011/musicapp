package com.musicapp.api.repository;

import com.musicapp.api.model.PlayHistory;
import com.musicapp.api.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    @Query("""
        SELECT ph.track FROM PlayHistory ph
        WHERE ph.user.username = :username
        GROUP BY ph.track
        ORDER BY MAX(ph.playedAt) DESC
        """)
    List<Track> findRecentTracksByUsername(@Param("username") String username);

    @Query("""
        SELECT ph FROM PlayHistory ph
        WHERE ph.user.username = :username
        ORDER BY ph.playedAt DESC
        """)
    List<PlayHistory> findByUsernameOrderByPlayedAtDesc(@Param("username") String username);
}
