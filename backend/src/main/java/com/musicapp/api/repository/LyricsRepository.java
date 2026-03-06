package com.musicapp.api.repository;

import com.musicapp.api.model.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LyricsRepository extends JpaRepository<Lyrics, Long> {
    Optional<Lyrics> findByTrackId(Long trackId);
}
