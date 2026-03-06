package com.musicapp.api.service;

import com.musicapp.api.model.Track;
import com.musicapp.api.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Collaborative filtering recommendations.
 * Strategy: "users who liked the same tracks as you also liked these".
 * Falls back to genre-based recommendations.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TrackRepository trackRepository;

    @Transactional(readOnly = true)
    public List<Track> getRecommendations(String username, int limit) {
        // First try collaborative filtering
        List<Track> collaborative = trackRepository.findCollaborativeRecommendations(username, limit);
        if (!collaborative.isEmpty()) {
            return collaborative;
        }

        // Fallback: recommend tracks from genres the user listens to most
        List<Track> genreBased = trackRepository.findGenreBasedRecommendations(username, limit);
        if (!genreBased.isEmpty()) {
            return genreBased;
        }

        // Final fallback: popular tracks (most played)
        return trackRepository.findMostPlayedTracks(limit);
    }
}
