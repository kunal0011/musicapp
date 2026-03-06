package com.musicapp.api.service;

import io.minio.PutObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HlsService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    /**
     * Transcodes a raw audio file (MP3/WAV/FLAC) into multi-bitrate HLS segments.
     * Produces 128k and 256k AAC variants + a master playlist.
     *
     * @param trackId  unique track identifier used as the HLS folder name
     * @param audioUrl full MinIO URL to the raw audio file
     * @return the URL of the master .m3u8 playlist, or null on failure
     */
    @Async
    public String transcodeToHls(Long trackId, String audioUrl) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("hls-" + trackId);
            Path inputFile = workDir.resolve("input");

            // Download the raw audio from MinIO URL
            downloadFile(audioUrl, inputFile);

            // Create variant directories
            Path low = workDir.resolve("128k");
            Path high = workDir.resolve("256k");
            Files.createDirectories(low);
            Files.createDirectories(high);

            // Transcode to 128k variant
            runFfmpeg(inputFile, low.resolve("playlist.m3u8"), "128k", low);

            // Transcode to 256k variant
            runFfmpeg(inputFile, high.resolve("playlist.m3u8"), "256k", high);

            // Write master playlist
            String masterContent = """
                    #EXTM3U
                    #EXT-X-STREAM-INF:BANDWIDTH=128000,CODECS="mp4a.40.2"
                    128k/playlist.m3u8
                    #EXT-X-STREAM-INF:BANDWIDTH=256000,CODECS="mp4a.40.2"
                    256k/playlist.m3u8
                    """;
            Path masterPlaylist = workDir.resolve("master.m3u8");
            Files.writeString(masterPlaylist, masterContent);

            // Upload all HLS files to MinIO
            String hlsPrefix = "hls/" + trackId;
            uploadDirectory(workDir, hlsPrefix);

            return minioUrl + "/" + bucketName + "/" + hlsPrefix + "/master.m3u8";

        } catch (Exception e) {
            log.error("HLS transcoding failed for track {}", trackId, e);
            return null;
        } finally {
            // Clean up temp directory
            if (workDir != null) {
                try {
                    deleteDirectory(workDir);
                } catch (IOException e) {
                    log.warn("Failed to clean up temp dir: {}", workDir, e);
                }
            }
        }
    }

    private void downloadFile(String url, Path dest) throws Exception {
        try (InputStream in = new java.net.URL(url).openStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void runFfmpeg(Path input, Path outputPlaylist, String bitrate, Path segmentDir) throws Exception {
        String segmentPattern = segmentDir.resolve("segment_%03d.ts").toString();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", input.toString(),
                "-c:a", "aac", "-b:a", bitrate,
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-hls_segment_filename", segmentPattern,
                "-f", "hls",
                outputPlaylist.toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Consume output to prevent blocking
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                /* consume */ }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg exited with code " + exitCode + " for bitrate " + bitrate);
        }
    }

    private void uploadDirectory(Path dir, String prefix) throws Exception {
        try (var stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().equals("input"))
                    .forEach(file -> {
                        try {
                            String relativePath = dir.relativize(file).toString();
                            String objectName = prefix + "/" + relativePath;
                            String contentType = relativePath.endsWith(".m3u8")
                                    ? "application/vnd.apple.mpegurl"
                                    : "video/MP2T";

                            minioClient.putObject(
                                    PutObjectArgs.builder()
                                            .bucket(bucketName)
                                            .object(objectName)
                                            .stream(Files.newInputStream(file), Files.size(file), -1)
                                            .contentType(contentType)
                                            .build());
                        } catch (Exception e) {
                            log.error("Failed to upload HLS file: {}", file, e);
                        }
                    });
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        try (var stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            /* best effort */ }
                    });
        }
    }
}
