package com.musicapp.api.config;

import com.musicapp.api.model.Track;
import com.musicapp.api.repository.TrackRepository;
import com.musicapp.api.model.Playlist;
import com.musicapp.api.repository.PlaylistRepository;
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

        @Override
        public void run(String... args) throws Exception {
                if (trackRepository.count() == 0) {
                        trackRepository.saveAll(Arrays.asList(
                                        Track.builder()
                                                        .title("Uplifting Electronic")
                                                        .artist("Pixabay Music")
                                                        .album("Electronic Vibes")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img1.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/en1.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Pop Rock Anthem")
                                                        .artist("Pixabay Music")
                                                        .album("Rock Collection")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img2.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/en2.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Acoustic Guitars")
                                                        .artist("Creative Commons")
                                                        .album("Acoustic Collection")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img3.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/en3.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Lofi Chill")
                                                        .artist("FASSounds")
                                                        .album("Study Beats")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img4.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/en4.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Night City")
                                                        .artist("Pixabay Music")
                                                        .album("Retro Vibes")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img5.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/en5.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Indian Traditional")
                                                        .artist("Music Of India")
                                                        .album("Traditional Rhythms")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img6.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/hi1.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Bollywood Dance")
                                                        .artist("Pritam Style")
                                                        .album("Desi Hits")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img7.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/hi2.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Indian Classical")
                                                        .artist("Pandit Sounds")
                                                        .album("Raag Collection")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img8.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/hi3.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Punjabi Beat")
                                                        .artist("Bhangra Beats")
                                                        .album("Punjabi Hits")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img9.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/hi4.mp3")
                                                        .build(),
                                        Track.builder()
                                                        .title("Tabla Rhythm")
                                                        .artist("Indian Percussion")
                                                        .album("Classical India")
                                                        .coverArtUrl("http://10.0.2.2:9000/musicapp/img10.jpg")
                                                        .streamUrl("http://10.0.2.2:9000/musicapp/hi5.mp3")
                                                        .build()));
                        System.out.println("Initialized mock tracks in Database.");
                }

                if (playlistRepository.count() == 0) {
                        List<Track> allTracks = trackRepository.findAll();

                        Playlist playlist1 = new Playlist("Focus & Study", "http://10.0.2.2:9000/musicapp/cover1.jpg");
                        if (allTracks.size() >= 2) {
                                playlist1.addTrack(allTracks.get(0));
                                playlist1.addTrack(allTracks.get(3));
                        }

                        Playlist playlist2 = new Playlist("Relaxing Vibes", "http://10.0.2.2:9000/musicapp/cover2.jpg");
                        if (allTracks.size() >= 3) {
                                playlist2.addTrack(allTracks.get(1));
                                playlist2.addTrack(allTracks.get(2));
                        }

                        playlistRepository.saveAll(Arrays.asList(playlist1, playlist2));
                        System.out.println("Initialized mock playlists in Database.");
                }
        }
}
