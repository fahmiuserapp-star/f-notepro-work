// Utility Functions
function showToast(msg, isErr = false) {
    let t = document.createElement('div');
    t.className = 'toast';
    t.innerHTML = msg;
    t.style.background = isErr ? '#dc2626' : '#1e293b';
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 2500);
}

function escapeHtml(s) {
    if (!s) return '';
    return s.replace(/[&<>]/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' })[m]);
}

function validatePhone(p) {
    return /^[\+]?[0-9]{8,15}$/.test(p.replace(/\s/g, ''));
}

function playBeep(freq = 880) {
    try {
        let ctx = new (AudioContext || webkitAudioContext)();
        let osc = ctx.createOscillator();
        let gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.frequency.value = freq;
        gain.gain.value = 0.2;
        osc.start();
        gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.4);
        osc.stop(ctx.currentTime + 0.4);
    } catch (e) { }
}

async function uploadToCloudinary(file, folder = "general") {
    let fd = new FormData();
    fd.append("file", file);
    fd.append("upload_preset", CLOUDINARY_UPLOAD_PRESET);
    fd.append("folder", folder);
    let res = await axios.post(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/auto/upload`, fd);
    return res.data.secure_url;
}