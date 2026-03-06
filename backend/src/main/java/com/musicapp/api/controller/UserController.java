package com.musicapp.api.controller;

import com.musicapp.api.model.Track;
import com.musicapp.api.model.User;
import com.musicapp.api.service.StorageService;
import com.musicapp.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final StorageService storageService;

    // --- Liked songs ---

    @GetMapping("/liked")
    public List<Track> getLiked(Principal principal) {
        return userService.getLikedTracks(principal.getName());
    }

    @PostMapping("/liked/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable Long trackId, Principal principal) {
        userService.likeTrack(principal.getName(), trackId);
    }

    @DeleteMapping("/liked/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long trackId, Principal principal) {
        userService.unlikeTrack(principal.getName(), trackId);
    }

    // --- User profile ---

    @GetMapping("/profile")
    public Map<String, Object> getProfile(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                "bio", user.getBio() != null ? user.getBio() : "",
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> body, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        if (body.containsKey("displayName")) {
            user.setDisplayName(body.get("displayName"));
        }
        if (body.containsKey("bio")) {
            user.setBio(body.get("bio"));
        }
        userService.saveUser(user);
        return getProfile(principal);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) throws Exception {
        String url = storageService.uploadFile(file, "avatars");
        User user = userService.findByUsername(principal.getName());
        user.setAvatarUrl(url);
        userService.saveUser(user);
        return Map.of("avatarUrl", url);
    }
}
