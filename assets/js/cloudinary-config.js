window.CLOUDINARY_CLOUD_NAME = "dmhtx93nj";
window.CLOUDINARY_UPLOAD_PRESET = "fcloud_preset";

window.uploadToCloudinary = async function(file, folder = "general") {
    let fd = new FormData();
    fd.append("file", file);
    fd.append("upload_preset", window.CLOUDINARY_UPLOAD_PRESET);
    if(folder) fd.append("folder", folder);
    let res = await axios.post(`https://api.cloudinary.com/v1_1/${window.CLOUDINARY_CLOUD_NAME}/auto/upload`, fd, {
        headers: { "Content-Type": "multipart/form-data" },
        maxContentLength: Infinity, maxBodyLength: Infinity
    });
    return res.data.secure_url;
};

window.uploadAudioToCloudinary = async function(blob) {
    let fd = new FormData();
    fd.append("file", blob, "voice.webm");
    fd.append("upload_preset", window.CLOUDINARY_UPLOAD_PRESET);
    fd.append("folder", "voice_messages");
    fd.append("resource_type", "video");
    let res = await axios.post(`https://api.cloudinary.com/v1_1/${window.CLOUDINARY_CLOUD_NAME}/video/upload`, fd);
    return res.data.secure_url;
};

window.compressAndUploadImage = function(file, type) {
    return new Promise((resolve) => {
        let reader = new FileReader();
        reader.onload = e => {
            let img = new Image();
            img.onload = async () => {
                let canvas = document.createElement('canvas');
                let w = img.width, h = img.height;
                let maxW = 500;
                if(w > maxW) { h *= maxW / w; w = maxW; }
                canvas.width = w; canvas.height = h;
                canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                let blob = await new Promise(r => canvas.toBlob(r, 'image/jpeg', 0.7));
                let url = await window.uploadToCloudinary(blob, type === 'guide' ? 'guides' : (type === 'bus' ? 'vehicles' : 'avatars'));
                if(type === 'guide') { window.currentGuidePhoto = url; document.getElementById('guidePhotoImg').src = url; document.getElementById('guidePhotoPreview').style.display = 'flex'; }
                else if(type === 'bus') { window.currentBusPhoto = url; document.getElementById('busPhotoImg').src = url; document.getElementById('busPhotoPreview').style.display = 'flex'; }
                else if(type === 'avatar') { window.userAvatarUrl = url; }
                window.showToast("✓ تم رفع الصورة");
                resolve(url);
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    });
};

window.triggerImageUpload = function(type) {
    let inp = document.createElement('input');
    inp.type = 'file';
    inp.accept = 'image/*';
    inp.onchange = async e => { if(e.target.files[0]) await window.compressAndUploadImage(e.target.files[0], type); };
    inp.click();
};
