#!/bin/bash

# Exit on error
set -e

echo "Setting up MinIO client in container..."
docker exec musicapp_storage mc alias set myminio http://127.0.0.1:9000 minioadmin minioadmin

echo "Creating 'musicapp' bucket if it doesn't exist..."
docker exec musicapp_storage mc mb myminio/musicapp || true

echo "Setting download policy on 'musicapp' bucket..."
docker exec musicapp_storage mc anonymous set download myminio/musicapp

echo "Downloading seed files..."
mkdir -p /tmp/musicapp_seeds

# Download 5 English audio tracks
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" -o /tmp/musicapp_seeds/en1.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3" -o /tmp/musicapp_seeds/en2.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3" -o /tmp/musicapp_seeds/en3.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3" -o /tmp/musicapp_seeds/en4.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3" -o /tmp/musicapp_seeds/en5.mp3

# Download 5 Hindi / Indian styled audio tracks
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3" -o /tmp/musicapp_seeds/hi1.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3" -o /tmp/musicapp_seeds/hi2.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3" -o /tmp/musicapp_seeds/hi3.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3" -o /tmp/musicapp_seeds/hi4.mp3
curl -sL "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3" -o /tmp/musicapp_seeds/hi5.mp3

# Download 10 cover images (using Lorem Picsum to guarantee no 404s)
curl -sL "https://picsum.photos/seed/music1/500/500" -o /tmp/musicapp_seeds/img1.jpg
curl -sL "https://picsum.photos/seed/music2/500/500" -o /tmp/musicapp_seeds/img2.jpg
curl -sL "https://picsum.photos/seed/music3/500/500" -o /tmp/musicapp_seeds/img3.jpg
curl -sL "https://picsum.photos/seed/music4/500/500" -o /tmp/musicapp_seeds/img4.jpg
curl -sL "https://picsum.photos/seed/music5/500/500" -o /tmp/musicapp_seeds/img5.jpg
curl -sL "https://picsum.photos/seed/music6/500/500" -o /tmp/musicapp_seeds/img6.jpg
curl -sL "https://picsum.photos/seed/music7/500/500" -o /tmp/musicapp_seeds/img7.jpg
curl -sL "https://picsum.photos/seed/music8/500/500" -o /tmp/musicapp_seeds/img8.jpg
curl -sL "https://picsum.photos/seed/music9/500/500" -o /tmp/musicapp_seeds/img9.jpg
curl -sL "https://picsum.photos/seed/music10/500/500" -o /tmp/musicapp_seeds/img10.jpg

echo "Copying to MinIO container..."
docker cp /tmp/musicapp_seeds/. musicapp_storage:/tmp/seeds

echo "Uploading files to MinIO bucket..."
docker exec musicapp_storage mc cp -r /tmp/seeds/ myminio/musicapp/

echo "Cleaning up..."
rm -rf /tmp/musicapp_seeds
docker exec musicapp_storage rm -rf /tmp/seeds

echo "MinIO Seeding Complete!"
