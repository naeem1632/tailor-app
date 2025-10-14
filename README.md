Tailor Web App (Java 21, Spring Boot 3.5.5, Thymeleaf, SQLite)
------------------------------------------------------------------
How to run:
1. Install Java 21 JDK.
2. From project root run: ./gradlew bootRun  (on Windows use gradlew.bat)
3. Open http://localhost:8080/clients

Notes:
- Data stored in 'tailor.db' SQLite file in project root.
- Uploaded pictures are saved to './uploads' and served at '/uploads/{filename}'.
- Filter clients from the list page by typing name or mobile.
- Filter dressMeasurements by type on client view page.
