Development Setup:
Java 21
nodejs v24.14.0
Apache Maven v3.9.12
Docker v29.2.1

To generate firebase credentials:
1. Go to console.firebase.google.com
2. Create a project (name it YAFD)
3. Project Settings → Service Accounts
4. Create a new Firebase Admin SDK
5. Click "Generate new private key" using Java
6. Download the JSON file and name it firebase-service-account.json
7. Go to your Firebase console → Project Settings → General → scroll down to "Your apps" → choose Web app
8. Select "config" and copy the config values and write it to frontend/.env (create the file)
    REACT_APP_FIREBASE_API_KEY=<AIzaSy...your-real-key...>
    REACT_APP_FIREBASE_AUTH_DOMAIN=<yafd-your-platform-name.firebaseapp.com>
    REACT_APP_FIREBASE_PROJECT_ID=<yafd-your-platform-id>
    REACT_APP_FIREBASE_STORAGE_BUCKET=<yafd-your-platform-storage-bucket.com>
    REACT_APP_FIREBASE_MESSAGING_SENDER_ID=<your-sender-id>
    REACT_APP_FIREBASE_APP_ID=<your-app-id>
9. Go to your Firebase console -> Select your yafd-platform project
10. Left sidebar → Authentication → Sign-in method tab
11. Click Email/Password (so that you can create account using email/password on frontend)
12. Toggle Enable → Save
13. Make sure project-id (line 69) inside api-gateway/application.yml is the same as your REACT_APP_FIREBASE_PROJECT_ID

To run the platform:
1. Compile each Spring Boot service (e.g. cd account-service && mvn clean install -DskipTests)
2. cd into project root folder and run "docker-compose up -d" (starts PostgreSQL)
3. Place your firebase-service-account.json in menu-service/src/main/resources/ and api-gateway/src/main/resources/
4. Start each Spring Boot service (e.g. cd account-service && mvn spring-boot:run)
5. cd frontend && npm install && npm start
6. Set Firebase config env vars in frontend/.env
