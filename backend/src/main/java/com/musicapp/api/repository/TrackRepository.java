package com.musicapp.api.repository;

import com.musicapp.api.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);

    @Query(value = """
            SELECT * FROM tracks
            WHERE search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
            LIMIT 50
            """, nativeQuery = true)
    List<Track> fullTextSearch(@Param("query") String query);

    // Keyset pagination: forward
    List<Track> findByIdGreaterThanOrderByIdAsc(Long cursor, org.springframework.data.domain.Pageable pageable);

    // Keyset pagination: all from start
    List<Track> findAllByOrderByIdAsc(org.springframework.data.domain.Pageable pageable);

    // Artist/Album relationship queries
    List<Track> findByArtistEntityId(Long artistId);

    List<Track> findByAlbumEntityId(Long albumId);

    /**
     * Collaborative filtering: find tracks liked by users who also liked
     * the same tracks as the given user, excluding tracks already liked.
     */
    @Query(value = """
            SELECT DISTINCT t.* FROM tracks t
            JOIN user_liked_tracks ult ON ult.track_id = t.id
            WHERE ult.user_id IN (
                SELECT ult2.user_id FROM user_liked_tracks ult2
                WHERE ult2.track_id IN (
                    SELECT ult3.track_id FROM user_liked_tracks ult3
                    JOIN users u ON u.id = ult3.user_id
                    WHERE u.username = :username
                )
                AND ult2.user_id != (SELECT id FROM users WHERE username = :username)
            )
            AND t.id NOT IN (
                SELECT ult4.track_id FROM user_liked_tracks ult4
                JOIN users u2 ON u2.id = ult4.user_id
                WHERE u2.username = :username
            )
            LIMIT :lim
            """, nativeQuery = true)
    List<Track> findCollaborativeRecommendations(@Param("username") String username, @Param("lim") int limit);

    /**
     * Genre-based fallback: recommend tracks in genres the user listens to,
     * that the user hasn't liked yet.
     */
    @Query(value = """
            SELECT DISTINCT t.* FROM tracks t
            JOIN track_genres tg ON tg.track_id = t.id
            WHERE tg.genre_id IN (
                SELECT tg2.genre_id FROM track_genres tg2
                JOIN user_liked_tracks ult ON ult.track_id = tg2.track_id
                JOIN users u ON u.id = ult.user_id
                WHERE u.username = :username
            )
            AND t.id NOT IN (
                SELECT ult2.track_id FROM user_liked_tracks ult2
                JOIN users u2 ON u2.id = ult2.user_id
                WHERE u2.username = :username
            )
            ORDER BY RANDOM()
            LIMIT :lim
            """, nativeQuery = true)
    List<Track> findGenreBasedRecommendations(@Param("username") String username, @Param("lim") int limit);

    /**
     * Popularity fallback: most played tracks ever.
     */
    @Query(value = """
            SELECT t.* FROM tracks t
            JOIN play_history ph ON ph.track_id = t.id
            GROUP BY t.id
            ORDER BY COUNT(*) DESC
            LIMIT :lim
            """, nativeQuery = true)
    List<Track> findMostPlayedTracks(@Param("lim") int limit);
}
