package com.musicapp.api.service;

import com.musicapp.api.dto.RegisterRequest;
import com.musicapp.api.exception.ApiException;
import com.musicapp.api.exception.ResourceNotFoundException;
import com.musicapp.api.model.Track;
import com.musicapp.api.model.User;
import com.musicapp.api.repository.TrackRepository;
import com.musicapp.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username already taken", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public List<Track> getLikedTracks(String username) {
        return userRepository.findLikedTracksByUsername(username);
    }

    @Transactional
    public void likeTrack(String username, Long trackId) {
        User user = findByUsername(username);
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found: " + trackId));
        user.getLikedTracks().add(track);
        userRepository.save(user);
    }

    @Transactional
    public void unlikeTrack(String username, Long trackId) {
        User user = findByUsername(username);
        user.getLikedTracks().removeIf(t -> t.getId().equals(trackId));
        userRepository.save(user);
    }

    public Set<Long> getLikedTrackIds(String username) {
        return userRepository.findLikedTrackIdsByUsername(username);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
