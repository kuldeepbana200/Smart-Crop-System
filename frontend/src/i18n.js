import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

// Language resources
const resources = {
  en: {
    translation: {
      "app.name": "Smart Crop System",
      "app.tagline": "Empowering Farmers with Technology",
      "nav.dashboard": "Dashboard",
      "nav.crops": "My Crops",
      "nav.weather": "Weather",
      "nav.advisories": "Advisories",
      "nav.alerts": "Alerts",
      "nav.market": "Market Prices",
      "nav.education": "Education",
      "nav.notifications": "Notifications",
      "nav.profile": "Profile",
      "auth.login": "Login",
      "auth.register": "Register",
      "auth.email": "Email",
      "auth.password": "Password",
      "auth.name": "Full Name",
      "auth.phone": "Phone Number",
      "auth.language": "Language Preference",
      "auth.remember_me": "Remember Me",
      "auth.forgot_password": "Forgot Password?",
      "auth.dont_have_account": "Don't have an account?",
      "auth.create_account": "Create Account",
      "auth.have_account": "Already have an account?",
      "common.save": "Save",
      "common.cancel": "Cancel",
      "common.delete": "Delete",
      "common.edit": "Edit",
      "common.view": "View",
      "common.loading": "Loading...",
      "common.error": "Error",
      "common.success": "Success",
      "common.try_again": "Try Again"
    }
  },
  hi: {
    translation: {
      "app.name": "स्मार्ट क्रॉप सिस्टम",
      "app.tagline": "प्रौद्योगिकी से किसानों को सशक्त बनाना",
      "nav.dashboard": "डैशबोर्ड",
      "nav.crops": "मेरी फसलें",
      "nav.weather": "मौसम",
      "nav.advisories": "सलाहें",
      "nav.alerts": "चेतावनियां",
      "nav.market": "बाजार कीमतें",
      "nav.education": "शिक्षा",
      "nav.notifications": "सूचनाएं",
      "nav.profile": "प्रोफ़ाइल",
      "auth.login": "लॉगिन",
      "auth.register": "पंजीकरण",
      "auth.email": "ईमेल",
      "auth.password": "पासवर्ड",
      "auth.name": "पूरा नाम",
      "auth.phone": "फोन नंबर",
      "auth.language": "भाषा वरीयता",
      "auth.remember_me": "मुझे याद रखें",
      "auth.forgot_password": "पासवर्ड भूल गए?",
      "auth.dont_have_account": "खाता नहीं है?",
      "auth.create_account": "खाता बनाएं",
      "auth.have_account": "पहले से खाता है?",
      "common.save": "सेव करें",
      "common.cancel": "रद्द करें",
      "common.delete": "हटाएं",
      "common.edit": "संपादित करें",
      "common.view": "देखें",
      "common.loading": "लोड हो रहा है...",
      "common.error": "त्रुटि",
      "common.success": "सफलता",
      "common.try_again": "फिर से प्रयास करें"
    }
  },
  or: {
    translation: {
      "app.name": "ସ୍ମାର୍ଟ କ୍ରପ ସିଷ୍ଟେମ",
      "app.tagline": "ପ୍ରାତେକ୍ନୋଜି ଦ୍ୱାରା କିଷାକୁ ଶକ୍ତିମାନ କରିବା",
      "nav.dashboard": "ଡ୍ୟାଶବୋର୍ଡ",
      "nav.crops": "ମୋର ପସୁଗୋଟ",
      "nav.weather": "ପବତିକ",
      "nav.advisories": "ସଳାହ",
      "nav.alerts": "ଚେତାବାଣୀ",
      "nav.market": "ବଜାର ମୂଲ୍ୟ",
      "nav.education": "ଶିକ୍ଷା",
      "nav.notifications": "ସୂଚନା",
      "nav.profile": "ପ୍ରୋଫାଇଲ",
      "auth.login": "ଲୋଗିନ",
      "auth.register": "ପ୍ରେଷନ୍",
      "auth.email": "ଇମେଲ",
      "auth.password": "ପାସ୍ୱର୍ଡ",
      "auth.name": "ପୂରଣ ନାମ",
      "auth.phone": "ଫୋନ ନମ୍ବର",
      "auth.language": "ଭାଷା ପ୍ରାଥମିକତା",
      "auth.remember_me": "ମୋତେ ଯାଦ଼କରନ୍ତୁ",
      "auth.forgot_password": "ପାସ୍ୱର୍ଡ ଭୁଲିଗେଲେ?",
      "auth.dont_have_account": "ଖତା ନାହିଁ?",
      "auth.create_account": "ଖତା ନିର୍ମାଣ କରନ୍ତୁ",
      "auth.have_account": "ପୂର୍ବର୍ଥି ଖତା ଅଛି?",
      "common.save": "ସମ୍ପନ୍ନ କରନ୍ତୁ",
      "common.cancel": "ରଦ୍ଦ କରନ୍ତୁ",
      "common.delete": "ହଟାଉଠିବା",
      "common.edit": "ସମ୍ପାଦନ କରନ୍ତୁ",
      "common.view": "ଦେଖିବା",
      "common.loading": "ଲୋଡ୍ ହେଉଛି...",
      "common.error": "ତ୍ରୁଟି",
      "common.success": "ସଫଳତା",
      "common.try_again": "ପୁନର୍ପ୍ରୟାସ କରନ୍ତୁ"
    }
  },
  mr: {
    translation: {
      "app.name": "स्मार्ट क्रॉप सिस्टम",
      "app.tagline": "तंत्रज्ञानाद्वारे शेतकरींसक्त व करणे",
      "nav.dashboard": "डॅशबोर्ड",
      "nav.crops": "माझे पिके",
      "nav.weather": "हवामान",
      "nav.advisories": "सल्ला",
      "nav.alerts": "चेतावणी",
      "nav.market": "बाजार भाव",
      "nav.education": "शिक्षण",
      "nav.notifications": "सूचना",
      "nav.profile": "प्रोफाइल",
      "auth.login": "लॉग इन",
      "auth.register": "नोंदणी",
      "auth.email": "ईमेल",
      "auth.password": "पासवर्ड",
      "auth.name": "पूर्ण नाव",
      "auth.phone": "फोन नंबर",
      "auth.language": "भाषा प्राधान्य",
      "auth.remember_me": "मला.setAttribute lembr",
      "auth.forgot_password": "पासवर्ड विसरलं?",
      "auth.dont_have_account": "खाते नसली तर?",
      "auth.create_account": "खाता तयार करा",
      "auth.have_account": "खाते असेल तर?",
      "common.save": "जतन",
      "common.cancel": "रद्द",
      "common.delete": "काढा",
      "common.edit": " संपादित करा",
      "common.view": "बघा",
      "common.loading": "लोड होत आहे...",
      "common.error": "त्रुटि",
      "common.success": "यश",
      "common.try_again": "पुन्हा प्रयत्न करा"
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: localStorage.getItem('language') || 'en', // default to English
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false // react already safes from xss
    }
  });

export default i18n;
