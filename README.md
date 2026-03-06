To run the platform:
1. docker-compose up -d (starts PostgreSQL)
2. Place your firebase-service-account.json in menu-service/src/main/resources/ and api-gateway/src/main/resources/
3. Start each Spring Boot service (e.g., cd account-service && mvn spring-boot:run)
4. cd frontend && npm install && npm start
5. Set Firebase config env vars in frontend/.env
