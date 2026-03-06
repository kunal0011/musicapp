package com.musicapp.api.config;

import com.musicapp.api.model.Playlist;
import com.musicapp.api.model.Track;
import com.musicapp.api.repository.PlaylistRepository;
import com.musicapp.api.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;

    // MinIO URLs use 10.0.2.2 (Android emulator loopback to host machine)
    private static final String MINIO = "http://10.0.2.2:9000/musicapp";

    @Override
    public void run(String... args) {
        if (trackRepository.count() > 0) return;

        trackRepository.saveAll(Arrays.asList(
            Track.builder().title("Uplifting Electronic").artist("Pixabay Music").album("Electronic Vibes")
                .coverArtUrl(MINIO + "/img1.jpg").streamUrl(MINIO + "/en1.mp3").durationMs(180000L).build(),
            Track.builder().title("Pop Rock Anthem").artist("Pixabay Music").album("Rock Collection")
                .coverArtUrl(MINIO + "/img2.jpg").streamUrl(MINIO + "/en2.mp3").durationMs(210000L).build(),
            Track.builder().title("Acoustic Guitars").artist("Creative Commons").album("Acoustic Collection")
                .coverArtUrl(MINIO + "/img3.jpg").streamUrl(MINIO + "/en3.mp3").durationMs(195000L).build(),
            Track.builder().title("Lofi Chill").artist("FASSounds").album("Study Beats")
                .coverArtUrl(MINIO + "/img4.jpg").streamUrl(MINIO + "/en4.mp3").durationMs(240000L).build(),
            Track.builder().title("Night City").artist("Pixabay Music").album("Retro Vibes")
                .coverArtUrl(MINIO + "/img5.jpg").streamUrl(MINIO + "/en5.mp3").durationMs(165000L).build(),
            Track.builder().title("Indian Traditional").artist("Music Of India").album("Traditional Rhythms")
                .coverArtUrl(MINIO + "/img6.jpg").streamUrl(MINIO + "/hi1.mp3").durationMs(300000L).build(),
            Track.builder().title("Bollywood Dance").artist("Pritam Style").album("Desi Hits")
                .coverArtUrl(MINIO + "/img7.jpg").streamUrl(MINIO + "/hi2.mp3").durationMs(225000L).build(),
            Track.builder().title("Indian Classical").artist("Pandit Sounds").album("Raag Collection")
                .coverArtUrl(MINIO + "/img8.jpg").streamUrl(MINIO + "/hi3.mp3").durationMs(420000L).build(),
            Track.builder().title("Punjabi Beat").artist("Bhangra Beats").album("Punjabi Hits")
                .coverArtUrl(MINIO + "/img9.jpg").streamUrl(MINIO + "/hi4.mp3").durationMs(200000L).build(),
            Track.builder().title("Tabla Rhythm").artist("Indian Percussion").album("Classical India")
                .coverArtUrl(MINIO + "/img10.jpg").streamUrl(MINIO + "/hi5.mp3").durationMs(280000L).build()
        ));

        if (playlistRepository.count() > 0) return;

        List<Track> all = trackRepository.findAll();
        Playlist focus = new Playlist("Focus & Study", MINIO + "/cover1.jpg");
        Playlist relax = new Playlist("Relaxing Vibes", MINIO + "/cover2.jpg");

        if (all.size() >= 4) {
            focus.addTrack(all.get(0));
            focus.addTrack(all.get(3));
            relax.addTrack(all.get(1));
            relax.addTrack(all.get(2));
        }

        playlistRepository.saveAll(Arrays.asList(focus, relax));
    }
}
