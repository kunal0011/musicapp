package com.musicapp.api.service;

import com.musicapp.api.exception.ResourceNotFoundException;
import com.musicapp.api.model.Artist;
import com.musicapp.api.model.Track;
import com.musicapp.api.model.User;
import com.musicapp.api.repository.ArtistRepository;
import com.musicapp.api.repository.TrackRepository;
import com.musicapp.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    public Page<Artist> getAllArtists(Pageable pageable) {
        return artistRepository.findAll(pageable);
    }

    public Artist findById(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + id));
    }

    public List<Track> getArtistTracks(Long artistId) {
        findById(artistId); // ensure artist exists
        return trackRepository.findByArtistEntityId(artistId);
    }

    @Transactional
    public Artist save(Artist artist) {
        return artistRepository.save(artist);
    }

    @Transactional
    public void followArtist(String username, Long artistId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Artist artist = findById(artistId);
        artist.getFollowers().add(user);
        artist.setFollowerCount(artist.getFollowerCount() + 1);
        artistRepository.save(artist);
    }

    @Transactional
    public void unfollowArtist(String username, Long artistId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Artist artist = findById(artistId);
        artist.getFollowers().remove(user);
        artist.setFollowerCount(Math.max(0, artist.getFollowerCount() - 1));
        artistRepository.save(artist);
    }
}
