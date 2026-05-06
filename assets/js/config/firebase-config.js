// ========== إعدادات Firebase (آمنة للعميل) ==========
// هذه الإعدادات عامة وآمنة للاستخدام في التطبيقات العميلة
const firebaseConfig = {
    apiKey: "AIzaSyC4vYbhHGfAWMSh-fRl4NlG1WiEyS6Ez9s",
    authDomain: "f-notepro-work-2d25b.firebaseapp.com",
    projectId: "f-notepro-work-2d25b",
    storageBucket: "f-notepro-work-2d25b.firebasestorage.app",
    messagingSenderId: "221438380381",
    appId: "1:221438380381:web:b46c35d0ec0944079480b4"
};

// تهيئة Firebase
firebase.initializeApp(firebaseConfig);

// تصدير الكائنات للاستخدام العام
window.db = firebase.firestore();
window.auth = firebase.auth();
window.rtdb = firebase.database();

// تفعيل التخزين المؤقت للعمل غير المتصل (مع التعامل مع الأخطاء)
window.db.enablePersistence({ synchronizeTabs: true })
    .catch(err => {
        if (err.code === 'failed-precondition') {
            console.log("[DEV] Multiple tabs open, persistence enabled in first tab only");
        } else if (err.code === 'unimplemented') {
            console.log("[DEV] Browser doesn't support persistence");
        }
    });

// إعداد قواعد أمان إضافية على مستوى العميل (لإخفاء الأخطاء)
window.db.settings({
    ignoreUndefinedProperties: true
});
