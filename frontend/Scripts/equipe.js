document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-scratch]").forEach((canvas) => setupScratch(canvas));
});

function setupScratch(canvas) {
    const wrap = canvas.parentElement;
    const ctx = canvas.getContext("2d");
    let isDrawing = false;
    let revealed = false;

    function resize() {
        const rect = wrap.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
        paintScratchLayer();
    }

    function paintScratchLayer() {
        ctx.globalCompositeOperation = "source-over";
        const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
        gradient.addColorStop(0, "#a8d5c4");
        gradient.addColorStop(1, "#7fb8a0");
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        ctx.fillStyle = "#0e4c3c";
        ctx.font = `bold ${Math.max(14, canvas.width * 0.09)}px Lato, sans-serif`;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText("Raspe e conheça", canvas.width / 2, canvas.height / 2);
    }

    function getPos(e) {
        const rect = canvas.getBoundingClientRect();
        const clientX = e.touches ? e.touches[0].clientX : e.clientX;
        const clientY = e.touches ? e.touches[0].clientY : e.clientY;
        return { x: clientX - rect.left, y: clientY - rect.top };
    }

    function scratch(e) {
        if (revealed) return;
        const { x, y } = getPos(e);
        ctx.globalCompositeOperation = "destination-out";
        ctx.beginPath();
        ctx.arc(x, y, Math.max(18, canvas.width * 0.08), 0, Math.PI * 2);
        ctx.fill();
        checkReveal();
    }

    function checkReveal() {
        const { data } = ctx.getImageData(0, 0, canvas.width, canvas.height);
        let transparent = 0;
        for (let i = 3; i < data.length; i += 4) {
            if (data[i] === 0) transparent++;
        }
        const percent = transparent / (data.length / 4);
        if (percent > 0.55 && !revealed) {
            revealed = true;
            canvas.classList.add("revealed");
        }
    }

    function start(e) { isDrawing = true; scratch(e); }
    function move(e) { if (isDrawing) scratch(e); }
    function end() { isDrawing = false; }

    canvas.addEventListener("mousedown", start);
    canvas.addEventListener("mousemove", move);
    canvas.addEventListener("mouseup", end);
    canvas.addEventListener("mouseleave", end);
    canvas.addEventListener("touchstart", start, { passive: true });
    canvas.addEventListener("touchmove", move, { passive: true });
    canvas.addEventListener("touchend", end);

    window.addEventListener("resize", resize);
    resize();
}