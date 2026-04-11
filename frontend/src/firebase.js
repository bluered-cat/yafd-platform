import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || 'your-api-key',
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || 'yafd-platform.firebaseapp.com',
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || 'yafd-platform',
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || 'yafd-platform.appspot.com',
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || '000000000000',
  appId: process.env.REACT_APP_FIREBASE_APP_ID || '1:000000000000:web:000000000000',
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export default app;
