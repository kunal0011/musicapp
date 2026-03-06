package com.musicapp.api.controller;

import com.musicapp.api.model.Album;
import com.musicapp.api.model.Track;
import com.musicapp.api.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public Page<Album> getAllAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return albumService.getAllAlbums(PageRequest.of(page, size, Sort.by("title")));
    }

    @GetMapping("/{id}")
    public Album getById(@PathVariable Long id) {
        return albumService.findById(id);
    }

    @GetMapping("/{id}/tracks")
    public List<Track> getAlbumTracks(@PathVariable Long id) {
        return albumService.getAlbumTracks(id);
    }
}
