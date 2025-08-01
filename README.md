# NullPath FileShare Backend 🛡️

This repository contains the Spring Boot backend service for NullPath's End-to-End Encrypted (E2EE) and anonymous file sharing solution. Designed with a strict "zero-knowledge" principle, this backend acts as a secure, "dumb pipe" for encrypted data, ensuring that NullPath (and anyone accessing the server) never has access to the plaintext contents of uploaded files.

---

### 🌟 NullPath's Core Principles (Backend Contribution)

*   **Zero-Knowledge & Confidentiality:** Our servers only store encrypted data blobs. All encryption/decryption happens client-side, meaning we never see or store your sensitive information in cleartext.
*   **No Trace Left Behind:** Files are automatically purged after a configurable retention period, and explicit deletion via a unique key is supported, minimizing digital exhaust on our servers.
*   **Privacy by Design:** The API is built to handle opaque, encrypted data, preventing any server-side inspection or logging of sensitive file contents or metadata.

---

### ✨ Features

*   **Anonymous Uploads:** Files can be uploaded without requiring user accounts or authentication.
*   **Secure Storage:** Encrypted file blobs are stored on disk with UUID-based filenames.
*   **Time-Limited Availability:** Configurable file retention period (e.g., 7 days by default) after which files are automatically deleted.
*   **Explicit Deletion:** Unique deletion keys are provided upon upload, allowing the uploader to manually remove files at any time.
*   **Public Download Endpoint:** Serves encrypted file blobs to anyone with a valid `storageIdentifier`, without requiring authentication.
*   **Minimal Logging:** Backend logs are designed to avoid capturing any sensitive user or file content information.

---

### 💻 Technology Stack

*   **Language:** Kotlin
*   **Framework:** Spring Boot 3.x
*   **Build Tool:** Gradle
*   **Database:** H2 (in-memory for development, easily configurable for persistent databases like PostgreSQL in production)
*   **API Documentation:** Springdoc OpenAPI (Swagger UI)

---

### 🚀 Getting Started (Development)

To run the NullPath FileShare Backend locally:

1.  **Prerequisites:**
    *   Java Development Kit (JDK) 21 or newer installed.
    *   Gradle (optional, the project includes a [Gradle Wrapper](#gradle-wrapper) for convenience).

2.  **Clone the Repository:**
    ```bash
    git clone https://github.com/null-path/nullpath-fileshare-backend.git
    cd nullpath-fileshare-backend
    ```

3.  **Configure Application Properties:**
    *   The application uses `application.properties`. Ensure `file.upload-dir` and `app.file-retention-days` are set as desired.
        ```properties
        # application.properties
        file.upload-dir=./uploads
        app.file-retention-days=7 # Files are deleted after 7 days
        ```
    *   By default, it uses an in-memory H2 database. Data will be lost on restart.

4.  **Run the Application:**
    *   Using Gradle Wrapper:
        ```bash
        ./gradlew bootRun # On Windows: gradlew.bat bootRun
        ```
    *   Using your IDE (e.g., IntelliJ IDEA): Open the project and run the `FileShareApplicationKt` class.

5.  **Access API Documentation (Swagger UI):**
    Once the backend is running (typically on `http://localhost:8080`), you can access the API documentation at:
    `http://localhost:8080/swagger-ui/index.html`

---

### ⚠️ Security Considerations

*   **HTTPS is MANDATORY for Production:** This backend is designed to handle *encrypted* data, but the connection itself must be secured with HTTPS to prevent eavesdropping and Man-in-the-Middle attacks on the transfer of the encrypted data and critical IDs/keys.
*   **File Retention:** While files are ephemeral, consider your retention policy carefully for production based on legal and privacy requirements.
*   **Storage Security:** Ensure the `file.upload-dir` path is secure and isolated on your production server.
*   **Database Security:** Use a robust, persistent database in production with proper access controls instead of H2 in-memory.
*   **DoS Protection:** Implement rate limiting and other DDoS mitigation strategies if deploying publicly, as this is an anonymous service.

---

### 🤝 Contributing

We welcome contributions to make NullPath even more secure and robust!

---

### 📄 License

This project is licensed under the [MIT] - see the [LICENSE](LICENSE) file for details.

---

### 🌐 Connect with NullPath

*   **GitHub Organization:** [https://github.com/null-path](https://github.com/null-path)
*   **Frontend Repository:** [https://github.com/null-path/nullpath-fileshare-frontend](https://github.com/null-path/nullpath-fileshare-frontend)

---