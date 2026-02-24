package com.musicapp.api.controller;

import com.musicapp.api.model.Track;
import com.musicapp.api.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For MVP simplicity
public class TrackController {

    private final TrackRepository trackRepository;

    @GetMapping
    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    @GetMapping("/search")
    public List<Track> searchTracks(@RequestParam String query) {
        return trackRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(query, query);
    }
}
