package com.musicapp.api.controller;

import com.musicapp.api.model.Track;
import com.musicapp.api.service.RecommendationService;
import com.musicapp.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @GetMapping
    public List<Track> getRecommendations(
            @RequestParam(defaultValue = "20") int limit,
            Principal principal) {
        List<Track> tracks = recommendationService.getRecommendations(principal.getName(), limit);
        Set<Long> liked = userService.getLikedTrackIds(principal.getName());
        tracks.forEach(t -> t.setLiked(liked.contains(t.getId())));
        return tracks;
    }
}
