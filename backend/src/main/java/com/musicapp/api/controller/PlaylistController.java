package com.musicapp.api.controller;

import com.musicapp.api.model.Playlist;
import com.musicapp.api.model.Track;
import com.musicapp.api.repository.PlaylistRepository;
import com.musicapp.api.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private TrackRepository trackRepository;

    @GetMapping
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    @PostMapping
    public Playlist createPlaylist(@RequestBody Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    @PostMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<Playlist> addTrackToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long trackId) {

        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        Optional<Track> optionalTrack = trackRepository.findById(trackId);

        if (optionalPlaylist.isPresent() && optionalTrack.isPresent()) {
            Playlist playlist = optionalPlaylist.get();
            Track track = optionalTrack.get();

            playlist.addTrack(track);
            Playlist updatedPlaylist = playlistRepository.save(playlist);
            return ResponseEntity.ok(updatedPlaylist);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
