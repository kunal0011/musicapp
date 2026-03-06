package com.musicapp.api.controller;

import com.musicapp.api.model.Playlist;
import com.musicapp.api.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public List<Playlist> getAll() {
        return playlistService.getAllPlaylists();
    }

    @GetMapping("/{id}")
    public Playlist getById(@PathVariable Long id) {
        return playlistService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Playlist create(@RequestBody Map<String, String> body) {
        return playlistService.create(body.get("name"), body.get("coverUrl"));
    }

    @PostMapping("/{playlistId}/tracks/{trackId}")
    public Playlist addTrack(@PathVariable Long playlistId, @PathVariable Long trackId) {
        return playlistService.addTrack(playlistId, trackId);
    }

    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public Playlist removeTrack(@PathVariable Long playlistId, @PathVariable Long trackId) {
        return playlistService.removeTrack(playlistId, trackId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        playlistService.delete(id);
    }
}
