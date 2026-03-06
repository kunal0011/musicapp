package com.musicapp.api.controller;

import com.musicapp.api.model.Track;
import com.musicapp.api.service.HlsService;
import com.musicapp.api.service.StorageService;
import com.musicapp.api.service.TrackService;
import com.musicapp.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;
    private final UserService userService;
    private final StorageService storageService;
    private final HlsService hlsService;

    @GetMapping
    public Page<Track> getAllTracks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort,
            Principal principal) {
        Set<Long> liked = principal != null ? userService.getLikedTrackIds(principal.getName()) : Set.of();
        return trackService.getAllTracksWithLiked(
                PageRequest.of(page, size, Sort.by(sort)), liked);
    }

    /**
     * Keyset (cursor-based) pagination – more efficient for large libraries.
     * Pass ?cursor=lastSeenId&size=20 to get the next page.
     */
    @GetMapping("/cursor")
    public List<Track> getTracksCursor(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        Set<Long> liked = principal != null ? userService.getLikedTrackIds(principal.getName()) : Set.of();
        List<Track> tracks = trackService.getTracksCursor(cursor, size);
        tracks.forEach(t -> t.setLiked(liked.contains(t.getId())));
        return tracks;
    }

    @GetMapping("/search")
    public List<Track> search(@RequestParam String q, Principal principal) {
        Set<Long> liked = principal != null ? userService.getLikedTrackIds(principal.getName()) : Set.of();
        return trackService.searchTracksWithLiked(q, liked);
    }

    @GetMapping("/{id}")
    public Track getById(@PathVariable Long id, Principal principal) {
        Track track = trackService.findById(id);
        if (principal != null) {
            Set<Long> liked = userService.getLikedTrackIds(principal.getName());
            track.setLiked(liked.contains(id));
        }
        return track;
    }

    /**
     * Returns a pre-signed HLS streaming URL for the given track.
     * Falls back to the raw stream URL if HLS is not available.
     */
    @GetMapping("/{id}/stream")
    public Map<String, String> getStreamUrl(@PathVariable Long id) {
        Track track = trackService.findById(id);
        String url;
        if (track.getHlsUrl() != null && !track.getHlsUrl().isEmpty()) {
            url = storageService.getCachedPresignedUrl(track.getHlsUrl());
        } else {
            url = storageService.getCachedPresignedUrl(track.getStreamUrl());
        }
        return Map.of("url", url);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Track upload(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "album", defaultValue = "") String album,
            @RequestParam(value = "durationMs", defaultValue = "0") Long durationMs) throws Exception {
        String audioUrl = storageService.uploadFile(audio, "audio");
        String coverUrl = (cover != null && !cover.isEmpty()) ? storageService.uploadFile(cover, "covers") : null;

        Track track = Track.builder()
                .title(title)
                .artist(artist)
                .album(album)
                .streamUrl(audioUrl)
                .coverArtUrl(coverUrl)
                .durationMs(durationMs)
                .build();
        Track saved = trackService.save(track);

        // Kick off async HLS transcoding
        hlsService.transcodeToHls(saved.getId(), audioUrl);

        return saved;
    }

    @PostMapping("/{id}/play")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordPlay(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            trackService.recordPlay(id, principal.getName());
        }
    }

    @GetMapping("/recently-played")
    public List<Track> recentlyPlayed(Principal principal) {
        Set<Long> liked = userService.getLikedTrackIds(principal.getName());
        return trackService.getRecentlyPlayed(principal.getName(), liked);
    }
}
