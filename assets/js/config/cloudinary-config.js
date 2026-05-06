// ========== إعدادات Cloudinary (آمنة) ==========
// هذه المفاتيح عامة وآمنة للاستخدام في التطبيقات العميلة
window.CLOUDINARY_CLOUD_NAME = "dmhtx93nj";
window.CLOUDINARY_UPLOAD_PRESET = "fcloud_preset";

// دوال الرفع مع معالجة آمنة للأخطاء
window.uploadToCloudinary = async function(file, folder = "general") {
    try {
        let fd = new FormData();
        fd.append("file", file);
        fd.append("upload_preset", window.CLOUDINARY_UPLOAD_PRESET);
        if(folder) fd.append("folder", folder);
        
        let res = await axios.post(
            `https://api.cloudinary.com/v1_1/${window.CLOUDINARY_CLOUD_NAME}/auto/upload`,
            fd,
            {
                headers: { "Content-Type": "multipart/form-data" },
                maxContentLength: Infinity,
                maxBodyLength: Infinity,
                timeout: 60000 // 60 ثانية كحد أقصى
            }
        );
        return res.data.secure_url;
    } catch(e) {
        console.error("[DEV] Upload error:", e);
        window.showToast("فشل رفع الملف. يرجى المحاولة لاحقاً.", true);
        throw new Error("Upload failed");
    }
};

window.uploadAudioToCloudinary = async function(blob) {
    try {
        let fd = new FormData();
        fd.append("file", blob, "voice.webm");
        fd.append("upload_preset", window.CLOUDINARY_UPLOAD_PRESET);
        fd.append("folder", "voice_messages");
        fd.append("resource_type", "video");
        
        let res = await axios.post(
            `https://api.cloudinary.com/v1_1/${window.CLOUDINARY_CLOUD_NAME}/video/upload`,
            fd,
            { timeout: 120000 }
        );
        return res.data.secure_url;
    } catch(e) {
        console.error("[DEV] Audio upload error:", e);
        window.showToast("فشل رفع الرسالة الصوتية", true);
        throw new Error("Audio upload failed");
    }
};

// ضغط الصور مع حدود آمنة
window.compressAndUploadImage = function(file, type) {
    return new Promise((resolve, reject) => {
        // التحقق من حجم الملف (حد أقصى 5 ميجابايت)
        if(file.size > 5 * 1024 * 1024) {
            window.showToast("الصورة كبيرة جداً. الحد الأقصى 5 ميجابايت", true);
            reject(new Error("File too large"));
            return;
        }
        
        let reader = new FileReader();
        reader.onload = e => {
            let img = new Image();
            img.onload = async () => {
                let canvas = document.createElement('canvas');
                let w = img.width, h = img.height;
                let maxW = 800; // تحديد أبعاد معقولة
                if(w > maxW) { h *= maxW / w; w = maxW; }
                canvas.width = w; canvas.height = h;
                canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                
                let blob = await new Promise(r => canvas.toBlob(r, 'image/jpeg', 0.8));
                let url = await window.uploadToCloudinary(blob, type === 'guide' ? 'guides' : (type === 'bus' ? 'vehicles' : 'avatars'));
                
                if(type === 'guide') {
                    window.currentGuidePhoto = url;
                    document.getElementById('guidePhotoImg').src = url;
                    document.getElementById('guidePhotoPreview').style.display = 'flex';
                } else if(type === 'bus') {
                    window.currentBusPhoto = url;
                    document.getElementById('busPhotoImg').src = url;
                    document.getElementById('busPhotoPreview').style.display = 'flex';
                } else if(type === 'avatar') {
                    window.userAvatarUrl = url;
                }
                
                window.showToast("✓ تم رفع الصورة");
                resolve(url);
            };
            img.onerror = () => {
                window.showToast("فشل تحميل الصورة", true);
                reject(new Error("Image load failed"));
            };
            img.src = e.target.result;
        };
        reader.onerror = () => {
            window.showToast("فشل قراءة الملف", true);
            reject(new Error("File read failed"));
        };
        reader.readAsDataURL(file);
    });
};

window.triggerImageUpload = function(type) {
    let inp = document.createElement('input');
    inp.type = 'file';
    inp.accept = 'image/*';
    inp.onchange = async e => {
        if(e.target.files[0]) {
            await window.compressAndUploadImage(e.target.files[0], type);
        }
    };
    inp.click();
};
