package com.musicapp.api.controller;

import com.musicapp.api.model.Artist;
import com.musicapp.api.model.Track;
import com.musicapp.api.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public Page<Artist> getAllArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return artistService.getAllArtists(PageRequest.of(page, size, Sort.by("name")));
    }

    @GetMapping("/{id}")
    public Artist getById(@PathVariable Long id) {
        return artistService.findById(id);
    }

    @GetMapping("/{id}/tracks")
    public List<Track> getArtistTracks(@PathVariable Long id) {
        return artistService.getArtistTracks(id);
    }

    @PostMapping("/{id}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable Long id, Principal principal) {
        artistService.followArtist(principal.getName(), id);
    }

    @DeleteMapping("/{id}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable Long id, Principal principal) {
        artistService.unfollowArtist(principal.getName(), id);
    }
}
