Spring Boot ZIP Archive Explorer

A backend application built with Java and Spring Boot that allows users to upload ZIP archives and dynamically explore their internal folder and file structures via a REST API—all without extracting the archive onto disk.

1. How to Run the Project

    Prerequisites:
        Java 17 or higher
        Gradle

    Steps to Run
        Clone the repository and navigate to the project root.
        Build the project using Gradle:
        ./gradlew build

    Run the application using the development profile:
        ./gradlew bootRun --args='--spring.profiles.active=dev'

    Access the H2 database console (if using dev profile) at:
        http://localhost:8080/h2-console (JDBC URL: jdbc:h2:file:./data/h2db, Username: sa, Password: password).

    Test endpoints using Postman or cURL (http://localhost:8080/api/v1/archives).

2. Configuration Options Explained

    The application relies on base and profile-specific YAML configurations:
    spring.profiles.active: Determines the runtime environment (dev or test).
    spring.servlet.multipart.max-file-size / max-request-size: Enforces strict server-level limits on uploaded payload sizes (default: 100MB).
    archive.storage-dir: Specifies the local directory path where uploaded raw ZIP files are safely stored.
    archive.max-upload-size-bytes: Custom application threshold for rejecting oversized archives.
    archive.max-entry-count: Prevents ZIP bombs by capping the maximum number of individual files/folders allowed inside an archive.
    archive.max-uncompressed-size-bytes: Caps the cumulative uncompressed size to protect server memory and storage.
    spring.datasource.url: Defines the database connection string (file-based jdbc:h2:file:./data/h2db for persistent development, memory-based jdbc:h2:mem:testdb for isolated testing).
    spring.jpa.hibernate.ddl-auto: Controls schema generation (update for dev to preserve data, create-drop for test to ensure clean states).

3. Flat ZIP Entries to Tree Structure Algorithm

    When a ZIP file is read, Java's ZipFile API returns a flat list of entries containing strings like docs/resume.pdf or images/vacation/sunset.jpg.

    How the Algorithm Works:

        Normalization: Every entry path is cleaned and split by forward slashes (/) into path segments.
        Path Reconstruction: Iterating through segments, intermediate folder nodes are dynamically inferred and created if they don't already exist in the hierarchy.
        Deduplication: A map or lookup structure ensures that folders with multiple children are referenced rather than duplicated.
        Sorting: Children nodes are sorted deterministically—folders appear first alphabetically, followed by files alphabetically.

    Small Example:

        Flat Input Entries:

            images/pic.png
            notes.txt
            images/sub/logo.png

            Generated Tree Output (JSON structure):

            [
                {
                    "name": "images",
                    "isDirectory": true,
                    "children": [
                    {
                        "name": "pic.png",
                        "isDirectory": false,
                        "size": 1024
                    },
                    {
                        "name": "sub",
                        "isDirectory": true,
                        "children": [
                          {
                            "name": "logo.png",
                            "isDirectory": false,
                            "size": 512
                          }
                        ]
                    }
                ]
            },
            {
                "name": "notes.txt",
                "isDirectory": false,
                "size": 256
            }
        ]


4. Known Limitations

    Memory Overhead on Massive Archives: While archives are not unzipped to disk, building deep multi-level JSON trees for archives containing tens of thousands of entries can consume significant heap memory.
    Single-Node File Locking: Local file storage relies on unique UUID file naming, but high concurrent writes to the same local storage volume can experience disk I/O bottlenecks.
    No Direct File Content Streaming: The explorer supports browsing metadata and downloading the entire archive, but individual file content extraction via the browser tree view is limited unless downloaded as a whole.

5. Basic Questions Answered
    Q)What does Spring's IoC container do for me?
        The Inversion of Control (IoC) container manages the lifecycle and wiring of your Java objects (Beans). Instead of you manually instantiating classes using new, the container automatically creates, injects dependencies into, and destroys objects, making code modular, loosely coupled, and easy to test.

    Q)What does Hibernate do?
        Hibernate acts as an Object-Relational Mapping (ORM) framework. It bridges the gap between your relational database (like H2/MySQL) and your Java object model, automatically translating Java method calls and entity modifications into SQL statements so you don't have to write raw SQL boilerplate.