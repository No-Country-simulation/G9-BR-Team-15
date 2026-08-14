    document.querySelectorAll('.player').forEach(player => {
        const audio = player.querySelector('.player__audio');
        const btn = player.querySelector('.player__play');
        const iconPlay = player.querySelector('.icon-play');
        const iconPause = player.querySelector('.icon-pause');
        const barra = player.querySelector('.player__barra');
        const progresso = player.querySelector('.player__progresso');
        const tempo = player.querySelector('.player__tempo');

        function formatarTempo(s) {
            const min = Math.floor(s / 60);
            const seg = Math.floor(s % 60).toString().padStart(2, '0');
            return `${min}:${seg}`;
        }

        btn.addEventListener('click', () => {
            document.querySelectorAll('audio').forEach(a => {
                if (a !== audio) a.pause();
            });
            document.querySelectorAll('.icon-play').forEach(i => i.style.display = 'block');
            document.querySelectorAll('.icon-pause').forEach(i => i.style.display = 'none');

            if (audio.paused) {
                audio.play();
                iconPlay.style.display = 'none';
                iconPause.style.display = 'block';
            } else {
                audio.pause();
                iconPlay.style.display = 'block';
                iconPause.style.display = 'none';
            }
        });

        audio.addEventListener('timeupdate', () => {
            const pct = (audio.currentTime / audio.duration) * 100 || 0;
            progresso.style.width = pct + '%';
            tempo.textContent = formatarTempo(audio.currentTime);
        });

        audio.addEventListener('ended', () => {
            iconPlay.style.display = 'block';
            iconPause.style.display = 'none';
            progresso.style.width = '0%';
            tempo.textContent = '0:00';
        });

        barra.addEventListener('click', (e) => {
            const rect = barra.getBoundingClientRect();
            const pct = (e.clientX - rect.left) / rect.width;
            audio.currentTime = pct * audio.duration;
        });
    });