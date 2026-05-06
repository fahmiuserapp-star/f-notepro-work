// Firebase Configuration
const firebaseConfig = {
    apiKey: "AIzaSyC4vYbhHGfAWMSh-fRl4NlG1WiEyS6Ez9s",
    authDomain: "f-notepro-work-2d25b.firebaseapp.com",
    projectId: "f-notepro-work-2d25b",
    storageBucket: "f-notepro-work-2d25b.firebasestorage.app",
    messagingSenderId: "221438380381",
    appId: "1:221438380381:web:b46c35d0ec0944079480b4"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();
const auth = firebase.auth();
const rtdb = firebase.database();
const storage = firebase.storage();

db.enablePersistence({ synchronizeTabs: true }).catch(e => console.warn("Persistence error:", e));