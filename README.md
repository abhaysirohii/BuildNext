# DevTrack - Task & Sprint Platform + Resume/JD Analyzer

Spring Boot + React + PostgreSQL/H2, JWT auth with RBAC, DTOs, global exception
handling, Swagger, Docker Compose, JUnit tests - plus three resume-analysis features:

1. **Red-flag keyword scan** (`POST /api/resume/redflags`) - compares pasted text
   against a target job description, flags missing JD keywords and weak/generic
   phrases (e.g. "responsible for", "helped with").
2. **Description quality scorer + rewrite** (`POST /api/resume/score`) - scores
   pasted text 0-100 on action verbs, quantifiable metrics, tech specificity, and
   conciseness, then drafts an improved version.
3. **Resume PDF upload + ATS score** (`POST /api/resume/analyze-pdf`, multipart) -
   upload a resume as a PDF plus a job description, and get:
   - An **ATS (Applicant Tracking System) score** - checks standard section
     headers, contact info, whether the PDF's layout extracts to clean text
     (multi-column/table layouts and scanned images often don't), resume length,
     and JD keyword match.
   - Both of the features above, run automatically against the text extracted
     from your PDF.
   - The raw extracted text itself, so you can see exactly what an ATS would see.

All rule-based, no external AI API calls - fully offline and free to run.

## Project layout

```
devtrack/
├── backend/     Spring Boot 3 (Java 21) - REST API, JWT auth, JPA, analyzer + ATS module
├── frontend/    React 18 (Vite) - login, task board, resume analyzer (paste + PDF upload)
└── docker-compose.yml
```

Configuration: Adding Your Gemini API Key
If you extend the rewrite engine or analyzer features with Google Gemini (or any external AI service), configure your API key in the backend configuration file before starting the application:

1. Open devtrack/backend/src/main/resources/application.yml in your code editor.

2. Add your Gemini API key under the configuration properties:

## Run it - Option A: Docker Compose (full stack, PostgreSQL)

```bash
cd devtrack
docker-compose up --build
```
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Run it - Option B: local dev (H2 in-memory, no Docker)

**Backend** (needs Java 21 + Maven):
```bash
cd devtrack/backend
mvn clean spring-boot:run
```
Runs on http://localhost:8080 with an in-memory H2 database (data resets on restart).
Always use `mvn clean spring-boot:run` (not just `spring-boot:run`) after pulling a
fresh copy of this project, so no stale compiled classes from a previous build linger.

**Frontend** (needs Node 18+, in a second terminal):
```bash
cd devtrack/frontend
npm install
npm run dev
```
Runs on http://localhost:5173. Register an account (the **first** account created
becomes `ROLE_ADMIN`), then click **Resume Helper** -> **Upload resume PDF**.

## Try the PDF-upload feature via curl (no frontend needed)

```bash
# 1) Register and grab the JWT
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alex","password":"password123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 2) Upload a resume PDF + JD in one call
curl -s -X POST localhost:8080/api/resume/analyze-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "resume=@/path/to/your/resume.pdf" \
  -F "jobDescription=Looking for a backend engineer with Spring Boot, PostgreSQL, Docker and Kubernetes experience." \
  | python3 -m json.tool
```

Or open http://localhost:8080/swagger-ui.html, click **Authorize**, paste
`Bearer <token>`, and call `/api/resume/analyze-pdf` interactively (Swagger
renders a file picker for multipart endpoints automatically).

The two paste-text endpoints (`/api/resume/redflags`, `/api/resume/score`) still
work exactly as before - see the earlier curl examples in this README's git
history, or just use the "Paste text" tab in the UI.

## Running the tests

```bash
cd devtrack/backend
mvn test
```
`AtsScoringServiceTest` covers the new ATS scoring logic. `ResumeAnalyzerServiceTest`
covers the red-flag and description-scoring features. `TaskServiceTest` covers the
base CRUD/ownership logic.

## Troubleshooting

- **"Unable to find a suitable main class"** or a `spring-boot-maven-plugin` version
  that doesn't match this `pom.xml` - you're building from a stale/mixed project
  folder. Delete your local `devtrack` folder entirely and unzip a fresh copy; don't
  copy individual files into an existing folder.
- **Lombok / "cannot find symbol" errors on getters/setters** - this project has no
  Lombok dependency at all; every class has hand-written getters/setters. If you see
  this error, you're not building the code in this zip.
- **PDF upload returns "No extractable text found"** - the PDF is a scanned image
  with no text layer. PDFBox (used here) only extracts real text, not OCR from
  images - scan-to-text would need a separate OCR step (e.g. Tesseract), which
  is a reasonable next extension (see below).

## What I'd extend first

1. **OCR fallback for scanned/image-based PDFs.** Right now `PdfTextExtractor` only
   pulls real text layers. Add a Tesseract-based fallback (e.g. `pdfbox` render-to-image
   + `tess4j`) when extracted text comes back empty or very short.
2. **Swap the rewrite engine for a real LLM call.** `ResumeAnalyzerService.buildSuggestion()`
   is template-based (zero cost, zero API keys, fully offline). Replace that one method
   with an LLM call if you want generated prose instead of a fill-in-the-blank template.
3. **Grow `ResumeKeywordData`** (tech dictionary, weak phrases, action verbs) and the
   `AtsScoringService` section-header list - both are plain constants, the highest-leverage
   place to improve accuracy with no logic changes.
4. **Persist analysis history per user**, and let a user re-run analysis against a
   previously uploaded resume without re-uploading.
5. **Support DOCX resumes** alongside PDF (Apache POI), since not everyone exports to PDF.
