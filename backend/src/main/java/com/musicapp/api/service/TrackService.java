package com.musicapp.api.service;

import com.musicapp.api.exception.ResourceNotFoundException;
import com.musicapp.api.model.PlayHistory;
import com.musicapp.api.model.Track;
import com.musicapp.api.model.User;
import com.musicapp.api.repository.PlayHistoryRepository;
import com.musicapp.api.repository.TrackRepository;
import com.musicapp.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "tracks", key = "'page-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<Track> getAllTracks(Pageable pageable) {
        return trackRepository.findAll(pageable);
    }

    @Cacheable(value = "search", key = "#query")
    @Transactional(readOnly = true)
    public List<Track> searchTracks(String query) {
        List<Track> results = trackRepository.fullTextSearch(query);
        if (results.isEmpty()) {
            // Fallback to LIKE search for short/partial queries
            results = trackRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(query, query);
        }
        return results;
    }

    public Track findById(Long id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found: " + id));
    }

    @CacheEvict(value = "tracks", allEntries = true)
    @Transactional
    public Track save(Track track) {
        return trackRepository.save(track);
    }

    public Page<Track> getAllTracksWithLiked(Pageable pageable, Set<Long> likedIds) {
        Page<Track> page = getAllTracks(pageable);
        page.getContent().forEach(t -> {
            if (likedIds.contains(t.getId())) {
                t.setLiked(true);
            }
        });
        return page;
    }

    public List<Track> searchTracksWithLiked(String query, Set<Long> likedIds) {
        List<Track> results = searchTracks(query);
        results.forEach(t -> t.setLiked(likedIds.contains(t.getId())));
        return results;
    }

    /**
     * Keyset (cursor-based) pagination for large libraries.
     * More efficient than offset pagination for deep pages.
     */
    @Transactional(readOnly = true)
    public List<Track> getTracksCursor(Long cursor, int size) {
        Pageable limit = PageRequest.of(0, size);
        if (cursor == null) {
            return trackRepository.findAllByOrderByIdAsc(limit);
        }
        return trackRepository.findByIdGreaterThanOrderByIdAsc(cursor, limit);
    }

    @Transactional
    public void recordPlay(Long trackId, String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            trackRepository.findById(trackId).ifPresent(track -> {
                PlayHistory history = PlayHistory.builder()
                        .user(user)
                        .track(track)
                        .playedAt(Instant.now())
                        .build();
                playHistoryRepository.save(history);
            });
        });
    }

    public List<Track> getRecentlyPlayed(String username, Set<Long> likedIds) {
        List<Track> tracks = playHistoryRepository.findRecentTracksByUsername(username);
        tracks.forEach(t -> t.setLiked(likedIds.contains(t.getId())));
        return tracks;
    }
}
