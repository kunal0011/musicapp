package com.musicapp.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tracks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    private String artist;
    private String album;

    @Column(name = "cover_art_url", length = 1000)
    private String coverArtUrl;

    @Column(name = "stream_url", length = 1000, nullable = false)
    private String streamUrl;

    @Column(name = "duration_ms")
    @Builder.Default
    private Long durationMs = 0L;

    @Column(name = "hls_url", length = 1000)
    private String hlsUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id")
    @JsonIgnoreProperties({ "tracks", "followers" })
    private Artist artistEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "album_id")
    @JsonIgnoreProperties({ "tracks" })
    private Album albumEntity;

    @ManyToMany(mappedBy = "tracks")
    @JsonIgnore
    @Builder.Default
    private Set<Playlist> playlists = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "track_genres", joinColumns = @JoinColumn(name = "track_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    // Set by service layer per authenticated user; never persisted
    @Transient
    @Builder.Default
    private boolean liked = false;
}
