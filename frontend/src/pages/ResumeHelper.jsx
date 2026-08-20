import { useState } from 'react';
import { api } from '../api/client';
import { RedFlagResults, ScoreResults, AtsResults } from '../components/AnalysisResults';

const Icon = ({ name, size = 18 }) => {
  const p = {
    spark: (
      <>
        <path d="M12 3l1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7L12 3Z" />
        <path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7L19 16Z" />
      </>
    ),
    upload: (
      <>
        <path d="M12 16V4" />
        <path d="m7 9 5-5 5 5" />
        <path d="M5 20h14" />
      </>
    ),
    paste: (
      <>
        <rect x="5" y="4" width="14" height="17" rx="2" />
        <path d="M8 9h8M8 13h8M8 17h5" />
      </>
    ),
  };
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {p[name]}
    </svg>
  );
};

export default function ResumeHelper({ token, defaultMode = 'paste' }) {
  const [mode, setMode] = useState(defaultMode);

  return (
    <div className="workspace-page">
      <div className="page-heading">
        <div>
          <span className="eyebrow">PROJECT ANALYSIS</span>
          <h1>Project Analysis</h1>
          <p>Analyze your project against a job description and make your application stronger.</p>
        </div>
        <div className="analyzer-tabs">
          <button
            className={mode === 'paste' ? 'active' : ''}
            onClick={() => setMode('paste')}
          >
            <Icon name="paste" /> Paste text
          </button>
          <button
            className={mode === 'upload' ? 'active' : ''}
            onClick={() => setMode('upload')}
          >
            <Icon name="upload" /> Upload PDF
          </button>
        </div>
      </div>

      {/* Dynamic Mode Switch */}
      {mode === 'paste' ? <PasteTextAnalyzer token={token} /> : <PdfUploadAnalyzer token={token} />}
    </div>
  );
}

function PasteTextAnalyzer({ token }) {
  const [jd, setJd] = useState('');
  const [project, setProject] = useState('');
  const [rf, setRf] = useState(null);
  const [sc, setSc] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const run = async () => {
    setError('');
    setLoading(true);
    try {
      const [a, b] = await Promise.all([
        api.redFlags(token, jd, project),
        api.scoreDescription(token, project, jd),
      ]);
      setRf(a);
      setSc(b);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="split-layout-container">
      {/* LEFT PANEL: Stationary Input Form */}
      <div className="left-panel-stationary">
        <section className="input-card">
          <div className="card-head">
            <div>
              <span className="mini-label">STEP 01</span>
              <h2>Target job description</h2>
            </div>
            <span className="counter">{jd.length}/5000</span>
          </div>
          <textarea
            value={jd}
            onChange={(e) => setJd(e.target.value)}
            placeholder="Paste the job posting, required skills and responsibilities here..."
            maxLength={5000}
          />

          <div className="card-head project-head">
            <div>
              <span className="mini-label">STEP 02</span>
              <h2>Your project / resume bullets</h2>
            </div>
            <span className="counter">{project.length}/5000</span>
          </div>
          <textarea
            value={project}
            onChange={(e) => setProject(e.target.value)}
            placeholder="Paste your project description or resume bullet points here..."
            maxLength={5000}
          />

          <div className="action-row">
            <button
              className="primary-button"
              onClick={run}
              disabled={loading || !jd || !project}
            >
              {loading ? 'Analyzing...' : 'Run analysis'} <span>→</span>
            </button>
            <button
              className="soft-button"
              onClick={() => {
                setJd('');
                setProject('');
                setRf(null);
                setSc(null);
              }}
            >
              Clear
            </button>
          </div>
          {error && <div className="inline-error">{error}</div>}
        </section>
      </div>

      {/* RIGHT PANEL: Scrollable Results & Output */}
      <div className="right-panel-scrollable">
        <aside className="tips-card mb-4">
          <span className="mini-label">QUICK TIPS</span>
          <h3>Write bullets that show impact.</h3>
          <ul>
            <li>Start with a strong action verb.</li>
            <li>Add a number or measurable outcome.</li>
            <li>Name the technologies you actually used.</li>
            <li>Keep each bullet concise.</li>
          </ul>
          <div className="tip-quote">
            “Specific work reads stronger than generic responsibility.”
          </div>
        </aside>

        {rf && <RedFlagResults result={rf} />}
        {sc && <ScoreResults result={sc} />}
      </div>
    </div>
  );
}

function PdfUploadAnalyzer({ token }) {
  const [file, setFile] = useState(null);
  const [jd, setJd] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const analyze = async () => {
    setError('');
    setLoading(true);
    setResult(null);
    try {
      setResult(await api.analyzeResumePdf(token, file, jd));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="split-layout-container">
      {/* LEFT PANEL: Stationary Upload Form */}
      <div className="left-panel-stationary">
        <section className="input-card upload-card">
          <span className="mini-label">ATS PDF SCANNER</span>
          <h2>Upload your resume</h2>
          <p>
            We'll extract the text and run ATS, keyword and description checks against your target JD.
          </p>

          <label className={`drop-zone ${file ? 'has-file' : ''}`}>
            <input
              type="file"
              accept="application/pdf"
              onChange={(e) => {
                const f = e.target.files?.[0];
                if (f && f.type !== 'application/pdf') {
                  setError('Please select a PDF file.');
                  return;
                }
                setError('');
                setFile(f || null);
              }}
            />
            <span className="drop-icon">
              <Icon name="upload" size={24} />
            </span>
            <strong>{file ? file.name : 'Choose a PDF resume'}</strong>
            <small>{file ? 'Ready to analyze' : 'PDF only • click to browse'}</small>
          </label>

          <label className="field-label">Target job description</label>
          <textarea
            rows="8"
            value={jd}
            onChange={(e) => setJd(e.target.value)}
            placeholder="Paste the target job description here..."
          />

          <button
            className="primary-button"
            onClick={analyze}
            disabled={loading || !file || !jd}
          >
            {loading ? 'Analyzing PDF...' : 'Analyze resume'} <span>→</span>
          </button>
          {error && <div className="inline-error">{error}</div>}
        </section>
      </div>

      {/* RIGHT PANEL: Scrollable Output */}
      <div className="right-panel-scrollable">
        {result ? (
          <>
            <AtsResults result={result.atsScore} />
            <RedFlagResults result={result.redFlagAnalysis} />
            <ScoreResults result={result.descriptionScore} />
            <div className="result-card">
              <div className="card-head">
                <div>
                  <span className="mini-label">RAW EXTRACTION</span>
                  <h2>What the ATS can read</h2>
                </div>
              </div>
              <textarea
                className="extracted"
                rows="12"
                readOnly
                value={result.extractedResumeText}
              />
            </div>
          </>
        ) : (
          <div className="empty-results-placeholder">
            <p>
              Upload your resume PDF and enter a target JD to view detailed analysis results here.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}