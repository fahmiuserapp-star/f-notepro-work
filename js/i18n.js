// js/i18n.js

const translations = {
    ar: {
        title: "إدارة الوفود - كرت المتابعة",
        login: "تسجيل الدخول",
        email: "البريد الإلكتروني",
        password: "كلمة المرور",
        logout: "تسجيل الخروج",
        save: "حفظ",
        delete: "حذف",
        exportPDF: "تصدير PDF",
        exportImage: "تصدير كصورة",
        plateTN: "تونس",
        devSection: "قسم المطور",
        settings: "الإعدادات",
        darkMode: "الوضع الداكن",
        lightMode: "الوضع المضيء"
    },
    fr: {
        title: "Gestion des Délégations - Carte de Suivi",
        login: "Connexion",
        email: "E-mail",
        password: "Mot de passe",
        logout: "Déconnexion",
        save: "Enregistrer",
        delete: "Supprimer",
        exportPDF: "Exporter en PDF",
        exportImage: "Exporter en Image",
        plateTN: "TN",
        devSection: "Section Développeur",
        settings: "Paramètres",
        darkMode: "Mode Sombre",
        lightMode: "Mode Clair"
    },
    en: {
        title: "Delegation Management - Tracking Card",
        login: "Login",
        email: "Email",
        password: "Password",
        logout: "Logout",
        save: "Save",
        delete: "Delete",
        exportPDF: "Export PDF",
        exportImage: "Export Image",
        plateTN: "TN",
        devSection: "Developer Section",
        settings: "Settings",
        darkMode: "Dark Mode",
        lightMode: "Light Mode"
    },
    de: {
        title: "Delegationsmanagement - Tracking-Karte",
        login: "Einloggen",
        email: "E-Mail",
        password: "Passwort",
        logout: "Ausloggen",
        save: "Speichern",
        delete: "Löschen",
        exportPDF: "Als PDF exportieren",
        exportImage: "Als Bild exportieren",
        plateTN: "TN",
        devSection: "Entwicklerbereich",
        settings: "Einstellungen",
        darkMode: "Dunkelmodus",
        lightMode: "Heller Modus"
    },
    it: {
        title: "Gestione Delegazioni - Scheda di Monitoraggio",
        login: "Accedi",
        email: "E-mail",
        password: "Password",
        logout: "Disconnetتي",
        save: "Salva",
        delete: "Elimina",
        exportPDF: "Esporta in PDF",
        exportImage: "Esporta come Immagine",
        plateTN: "TN",
        devSection: "Sezione Sviluppatore",
        settings: "Impostazioni",
        darkMode: "Modalità Scura",
        lightMode: "Modalità Chiara"
    }
};

let currentLang = localStorage.getItem('f_note_lang') || 'ar';

function setLanguage(lang) {
    currentLang = lang;
    localStorage.setItem('f_note_lang', lang);
    
    // تغيير اتجاه الصفحة بناءً على اللغة
    if (lang === 'ar') {
        document.documentElement.dir = 'rtl';
        document.documentElement.lang = 'ar';
    } else {
        document.documentElement.dir = 'ltr';
        document.documentElement.lang = lang;
    }

    // تطبيق الترجمات على العناصر التي تحمل خاصية data-i18n
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (translations[lang][key]) {
            if (element.tagName === 'INPUT' && (element.type === 'text' || element.type === 'password' || element.type === 'email')) {
                element.placeholder = translations[lang][key];
            } else {
                element.innerText = translations[lang][key];
            }
        }
    });
}

// تشغيل اللغة الافتراضية عند تحميل الصفحة
document.addEventListener('DOMContentLoaded', () => {
    setLanguage(currentLang);
});
