// js/app.js

// 1. إعدادات Firebase الخاصة بك
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "YOUR_AUTH_DOMAIN",
    projectId: "YOUR_PROJECT_ID",
    storageBucket: "YOUR_STORAGE_BUCKET",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
    appId: "YOUR_APP_ID"
};

// تهيئة الفايربيس
if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}

const auth = firebase.auth();
const db = firebase.firestore();
const storage = firebase.storage();

const DEVELOPER_UID = "YOUR_SECRET_DEVELOPER_UID"; // الـ UID الخاص بك لحماية لوحة التحكم
let currentUser = null;
let isDeveloper = false;

// 2. مراقبة حالة تسجيل الدخول
auth.onAuthStateChanged(async (user) => {
    if (user) {
        currentUser = user;
        document.getElementById('loginScreen').style.display = 'none';
        document.getElementById('appContainer').style.display = 'block';
        
        // التحقق مما إذا كان المستخدم هو المطور
        isDeveloper = (user.uid === DEVELOPER_UID);
        if (isDeveloper) {
            document.getElementById('developerSection').style.display = 'block';
        } else {
            document.getElementById('developerSection').style.display = 'none';
        }
        
        // هنا يتم استدعاء دوال جلب البيانات والتزامن من Firestore
    } else {
        currentUser = null;
        isDeveloper = false;
        document.getElementById('appContainer').style.display = 'none';
        document.getElementById('loginScreen').style.display = 'flex';
    }
});

// 3. دالة التعامل مع كروت المتابعة وتصديرها كـ PDF
async function exportToPDF() {
    const element = document.getElementById('trackingCard'); // العنصر المراد تصديره
    const options = {
        margin: 10,
        filename: `F-Note-${Date.now()}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    
    html2pdf().set(options).from(element).save();
}

// أضف مستمعي الأحداث وباقي كود الـ JavaScript التشغيلي هنا...
