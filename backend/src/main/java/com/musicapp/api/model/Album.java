package com.musicapp.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "albums")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "cover_art_url", length = 1000)
    private String coverArtUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @OneToMany(mappedBy = "albumEntity", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Track> tracks = new HashSet<>();
}
