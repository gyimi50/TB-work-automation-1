# Architecture Overview

## Package Structure

### `db` – Database Layer
- `DisciplinaryCase` – JPA entity, maps to database table
- `DisciplinaryCaseRepository` – Spring Data repository, database operations
- `DocumentService` – business logic: save from web, process files, BBCode conversion
- `FileProcessorService` – iterates a directory and processes all supported files

### `document` – Document Processing Layer
- `DocumentTypeDetector` – detects file type (.docx, .odt, .doc)
- `ToDocxConverter` – converts .odt/.doc to .docx via LibreOffice CLI
- `DocxParser` – extracts text from .docx, wraps bold text in [b]...[/b]
- `SignatureExtractor` – extracts signatures from document footer table
- `DocumentProcessor` – orchestrator: coordinates detection, conversion, parsing

### `selenium` – Browser Automation Layer
- `BasePage` – abstract base: ChromeDriver init, explicit waits, iframe handling, JS executor
- `YamlLocatorReader` – reads HTML locators from locators.yaml
- `input/InputPageLogin` – handles login on input site
- `input/InputPageDownload` – navigates, collects data, downloads files from input site
- `output/OutputPageLogin` – handles login on output site (SMF forum)
- `output/OutputPagePost` – navigates forum, finds season topic, submits post
- `DisciplinaryData` – DTO: holds raw data collected from web before DB persistence

## Data Flow
Input site → Login → Navigate → Collect data + Download files
↓
DocumentProcessor → BBCode conversion
↓
PostgreSQL (Docker) → Store records
↓
Output site → Login → Navigate to season topic → Submit post

## Key Design Decisions
- **Repository Pattern** – database layer abstracted behind interfaces, easily swappable (e.g. PostgreSQL → Supabase)
- **Page Object Model** – each web page has its own class, locators stored in YAML
- **Modular Monolith** – clear separation of concerns without microservice complexity
- **Separate WebDriver instances** – input and output flows use independent browser sessions
- **iframe handling** – output site embeds SMF forum in iframe, handled transparently via BasePage