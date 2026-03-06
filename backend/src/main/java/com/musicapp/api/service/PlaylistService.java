package com.musicapp.api.service;

import com.musicapp.api.exception.ResourceNotFoundException;
import com.musicapp.api.model.Playlist;
import com.musicapp.api.model.Track;
import com.musicapp.api.repository.PlaylistRepository;
import com.musicapp.api.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    @Cacheable("playlists")
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    public Playlist findById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
    }

    @CacheEvict(value = "playlists", allEntries = true)
    @Transactional
    public Playlist create(String name, String coverUrl) {
        return playlistRepository.save(new Playlist(name, coverUrl != null ? coverUrl : ""));
    }

    @CacheEvict(value = "playlists", allEntries = true)
    @Transactional
    public Playlist addTrack(Long playlistId, Long trackId) {
        Playlist playlist = findById(playlistId);
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found: " + trackId));
        playlist.addTrack(track);
        return playlistRepository.save(playlist);
    }

    @CacheEvict(value = "playlists", allEntries = true)
    @Transactional
    public Playlist removeTrack(Long playlistId, Long trackId) {
        Playlist playlist = findById(playlistId);
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found: " + trackId));
        playlist.removeTrack(track);
        return playlistRepository.save(playlist);
    }

    @CacheEvict(value = "playlists", allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Playlist not found: " + id);
        }
        playlistRepository.deleteById(id);
    }
}
