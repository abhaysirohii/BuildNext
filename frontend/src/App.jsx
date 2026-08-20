import { useState } from 'react';
import Auth from './pages/Auth';
import Tasks from './pages/Tasks';
import ResumeHelper from './pages/ResumeHelper';

const Icon = ({ name, size = 18 }) => {
  const paths = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></>,
    check: <><path d="m5 12 4 4L19 6"/><circle cx="12" cy="12" r="9"/></>,
    spark: <><path d="M12 3l1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7L12 3Z"/><path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7L19 16Z"/></>,
    upload: <><path d="M12 16V4"/><path d="m7 9 5-5 5 5"/><path d="M5 20h14"/></>,
    paste: <><rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4.5V3h6v1.5M8 9h8M8 13h8M8 17h5"/></>,
    chart: <><path d="M4 19V5M4 19h16"/><path d="m7 15 3-4 3 2 5-7"/></>,
    plus: <><path d="M12 5v14M5 12h14"/></>,
    logout: <><path d="M10 17l5-5-5-5"/><path d="M15 12H3"/><path d="M21 4v16"/></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></>,
    user: <><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></>,
    arrow: <><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>,
  };
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>;
};

function Brand({ compact = false }) {
  return <div className={`brand ${compact ? 'compact' : ''}`}><span className="brand-mark">B</span><span>BUILDNEST</span></div>;
}

export default function App() {
  const [session, setSession] = useState(null);
  const [tab, setTab] = useState('dashboard');

  if (!session) return <Auth onAuth={setSession} />;

  const tabs = [
    ['dashboard', 'Overview', 'grid'],
    ['tasks', 'Tasks', 'check'],
    ['resume', 'Project Analysis', 'spark'],
    ['upload', 'Upload Resume', 'upload'],
  ];

  return (
    <div className="app-shell">
      <header className="topbar">
        <Brand />
        <nav className="top-nav">
          {tabs.map(([key, label, icon]) => (
            <button key={key} className={tab === key ? 'nav-link active' : 'nav-link'} onClick={() => setTab(key)}>
              <Icon name={icon} size={16}/>{label}
            </button>
          ))}
        </nav>
        <div className="top-actions">
          <div className="profile-chip"><span className="avatar"><Icon name="user" size={15}/></span><span>{session.username}</span><small>{session.role}</small></div>
          <button className="logout" onClick={() => setSession(null)}><Icon name="logout" size={16}/> Logout</button>
        </div>
      </header>

      <main className="main-content">
        {tab === 'dashboard' && <Dashboard session={session} onTab={setTab} />}
        {tab === 'tasks' && <Tasks token={session.token} />}
        {tab === 'resume' && <ResumeHelper token={session.token} defaultMode="paste" />}
        {tab === 'upload' && <ResumeHelper token={session.token} defaultMode="upload" />}
      </main>
    </div>
  );
}

function Dashboard({ session, onTab }) {
  return (
    <div className="dashboard-page">
      <section className="hero-row">
        <div>
          <div className="eyebrow">YOUR CAREER WORKSPACE</div>
          <h1>Build smarter. <span>Get noticed.</span></h1>
          <p>Welcome back, <strong>{session.username}</strong>. Turn your development work into a job-ready profile.</p>
        </div>
        <button className="primary-button hero-button" onClick={() => onTab('resume')}><Icon name="spark"/> Open project analysis <Icon name="arrow" size={16}/></button>
      </section>

      <section className="stats-grid">
        <Stat label="Workspace" value="Active" note="Ready to build" icon="grid" />
        <Stat label="Task flow" value="Organized" note="Keep momentum" icon="check" />
        <Stat label="Project Analysis" value="3 tools" note="ATS + quality + red flags" icon="spark" />
      </section>

      <section className="dashboard-grid">
        <div className="feature-card feature-main">
          <div className="card-head"><div><span className="mini-label">RESUME INTELLIGENCE</span><h2>Optimize your next application</h2></div><span className="green-badge">3 ANALYZERS</span></div>
          <p>Compare your project against a job description, spot missing keywords, improve your bullet points, and check ATS compatibility.</p>
          <div className="tool-grid">
            <ToolCard icon="spark" title="JD Match" text="Find missing keywords and red flags." onClick={() => onTab('resume')} />
            <ToolCard icon="chart" title="Quality Score" text="Improve impact, verbs and clarity." onClick={() => onTab('resume')} />
            <ToolCard icon="upload" title="ATS Scanner" text="Upload a PDF and inspect parsing." onClick={() => onTab('upload')} />
          </div>
        </div>
        <div className="side-card green-card">
          <span className="mini-label">QUICK START</span>
          <h3>Make your resume stronger in 3 steps.</h3>
          <ol><li>Paste the target JD.</li><li>Add your project bullets.</li><li>Run the analysis and rewrite.</li></ol>
          <button className="outline-green" onClick={() => onTab('resume')}>Start analysis <Icon name="arrow" size={15}/></button>
        </div>
      </section>

      <section className="wide-callout">
        <div className="callout-icon"><Icon name="check" size={22}/></div>
        <div><strong>Keep building, keep shipping.</strong><p>Buildnest brings tasks and resume readiness into one focused workspace.</p></div>
        <button className="soft-button" onClick={() => onTab('tasks')}>Open task board</button>
      </section>
    </div>
  );
}

function Stat({ label, value, note, icon }) {
  return <div className="stat-card"><span className="stat-icon"><Icon name={icon} size={18}/></span><div><span className="stat-label">{label}</span><strong>{value}</strong><small>{note}</small></div></div>;
}

function ToolCard({ icon, title, text, onClick }) {
  return <button className="tool-card" onClick={onClick}><span className="tool-icon"><Icon name={icon}/></span><span><strong>{title}</strong><small>{text}</small></span><Icon name="arrow" size={16}/></button>;
}
