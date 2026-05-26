# Case Work Automation

Automated document processing and publishing tool built with Java/Spring Boot.

## Tech Stack
- Java 17, Spring Boot, Maven
- Selenium WebDriver (Chrome automation)
- PostgreSQL (Docker), Spring Data JPA, Lombok
- Apache POI, LibreOffice CLI

## Features
- Automated login and data collection from web interface
- Multi-page iteration with season-aware filtering
- Document download and processing (.docx, .odt, .doc)
- Bold text extraction and BBCode conversion
- PostgreSQL storage with duplicate detection
- Automated publishing to output platform via SMF forum

## Setup

### Prerequisites
- Java 17, Maven, Docker Desktop, LibreOffice, Chrome

### Configuration
Create `tb-automation/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
input.username=your_input_username
input.password=your_input_password
output.username=your_output_username
output.password=your_output_password
```

Create `tb-automation/src/main/resources/locators.yaml`
based on `locators.yaml.example`.

Create `tb-automation/src/main/resources/config.yaml`
based on the following structure:
```yaml
currentSeason: "your_season_code"
involvedTypeMapping:
  your_type: "your_output_value"
seasonTopic: "your_season_topic"
```

### Run
```bash
./run.sh
```

## Architecture
See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed component overview.