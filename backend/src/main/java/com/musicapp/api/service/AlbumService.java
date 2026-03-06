package com.musicapp.api.service;

import com.musicapp.api.exception.ResourceNotFoundException;
import com.musicapp.api.model.Album;
import com.musicapp.api.model.Track;
import com.musicapp.api.repository.AlbumRepository;
import com.musicapp.api.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    public Page<Album> getAllAlbums(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    public Album findById(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + id));
    }

    public List<Album> getAlbumsByArtist(Long artistId) {
        return albumRepository.findByArtistId(artistId);
    }

    public List<Track> getAlbumTracks(Long albumId) {
        findById(albumId); // ensure album exists
        return trackRepository.findByAlbumEntityId(albumId);
    }

    @Transactional
    public Album save(Album album) {
        return albumRepository.save(album);
    }
}
