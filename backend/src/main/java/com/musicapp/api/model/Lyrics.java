package com.musicapp.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lyrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lyrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", unique = true, nullable = false)
    private Track track;

    /** LRC-formatted synced lyrics */
    @Column(name = "lrc_content", columnDefinition = "TEXT")
    private String lrcContent;

    /** Plain text lyrics without timestamps */
    @Column(name = "plain_text", columnDefinition = "TEXT")
    private String plainText;
}
