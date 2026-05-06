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

async function compressAndUploadImage(file, type) {
    return new Promise((resolve) => {
        let reader = new FileReader();
        reader.onload = e => {
            let img = new Image();
            img.onload = async () => {
                let canvas = document.createElement('canvas');
                let w = img.width, h = img.height;
                let maxW = 400;
                if (w > maxW) { h *= maxW / w; w = maxW; }
                canvas.width = w;
                canvas.height = h;
                canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                let blob = await new Promise(r => canvas.toBlob(r, 'image/jpeg', 0.7));
                let url = await uploadToCloudinary(blob, type === 'guide' ? 'guides' : (type === 'bus' ? 'vehicles' : 'avatars'));
                if (type === 'guide') {
                    currentGuidePhoto = url;
                    document.getElementById('guidePhotoImg').src = url;
                    document.getElementById('guidePhotoPreview').style.display = 'flex';
                } else if (type === 'bus') {
                    currentBusPhoto = url;
                    document.getElementById('busPhotoImg').src = url;
                    document.getElementById('busPhotoPreview').style.display = 'flex';
                } else if (type === 'avatar') {
                    userAvatarUrl = url;
                }
                showToast("✓ تم رفع الصورة");
                resolve(url);
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    });
}
