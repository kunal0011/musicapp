package com.musicapp.api.controller;

import com.musicapp.api.exception.ResourceNotFoundException;
import com.musicapp.api.model.Lyrics;
import com.musicapp.api.model.Track;
import com.musicapp.api.repository.LyricsRepository;
import com.musicapp.api.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tracks/{trackId}/lyrics")
@RequiredArgsConstructor
public class LyricsController {

    private final LyricsRepository lyricsRepository;
    private final TrackService trackService;

    @GetMapping
    public Lyrics getLyrics(@PathVariable Long trackId) {
        return lyricsRepository.findByTrackId(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Lyrics not found for track: " + trackId));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Lyrics upsertLyrics(
            @PathVariable Long trackId,
            @RequestBody Map<String, String> body) {
        Track track = trackService.findById(trackId);
        Lyrics lyrics = lyricsRepository.findByTrackId(trackId)
                .orElse(Lyrics.builder().track(track).build());

        if (body.containsKey("lrcContent")) {
            lyrics.setLrcContent(body.get("lrcContent"));
        }
        if (body.containsKey("plainText")) {
            lyrics.setPlainText(body.get("plainText"));
        }

        return lyricsRepository.save(lyrics);
    }
}
