# Architecture Overview

## Package Structure

### `db` – Database Layer
- `DisciplinaryCase` – JPA entity, maps to database table
- `DisciplinaryCaseRepository` – Spring Data repository, database operations
- `DocumentService` – business logic: save from web, process files, BBCode conversion, post to forum
- `DownloadService` – handles authenticated file downloads via HttpClient with session cookies
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
- `YamlConfigReader` – reads business config from config.yaml (season, type mapping)
- `input/InputPageLogin` – handles login on input site
- `input/InputPageDownload` – multi-page data collection with season filtering; decomposed into single-responsibility methods
- `output/OutputPageLogin` – handles login on output site (SMF forum)
- `output/OutputPagePost` – navigates forum, finds season topic, submits post
- `DisciplinaryData` – DTO: holds raw data collected from web before DB persistence

## Data Flow
Input site → Login → Navigate → Multi-page collect + season filter + Download files
↓
DocumentProcessor → BBCode conversion
↓
PostgreSQL (Docker) → Store records (duplicate detection)
↓
Output site → Login → Navigate to season topic → Iterate PROCESSED records → Submit posts

## Key Design Decisions
- **Repository Pattern** – database layer abstracted behind interfaces, easily swappable (e.g. PostgreSQL → Supabase)
- **Page Object Model** – each web page has its own class, locators stored in YAML
- **Modular Monolith** – clear separation of concerns without microservice complexity
- **Separate WebDriver instances** – input and output flows use independent browser sessions
- **iframe handling** – output site embeds SMF forum in iframe, handled transparently via BasePage
- **Season-aware pagination** – iterates pages until old season cases found, filters current season only
- **Config-driven** – season code and type mappings externalized to config.yaml, no hardcoding
- **Single Responsibility** – InputPageDownload decomposed into focused helper methods