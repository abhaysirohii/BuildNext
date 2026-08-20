import { useState } from 'react';
import { api } from '../api/client';

const Icon = ({ name, size = 18 }) => {
  const paths = {
    user: <><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></>,
    lock: <><rect x="5" y="10" width="14" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></>,
    eye: <><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="2.5"/></>,
    spark: <><path d="M12 3l1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7L12 3Z"/><path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7L19 16Z"/></>,
    arrow: <><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>,
  };
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{paths[name]}</svg>;
};

export default function Auth({ onAuth }) {
  const [mode, setMode] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault(); setError(''); setLoading(true);
    try {
      const fn = mode === 'login' ? api.login : api.register;
      onAuth(await fn(username, password));
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  const login = mode === 'login';
  return (
    <div className="auth-page">
      <div className="auth-glow glow-one"/><div className="auth-glow glow-two"/>
      <div className="auth-top-brand"><span className="auth-mark">B</span><strong>BUILDNEST</strong></div>
      <div className="auth-layout">
        <section className="auth-showcase">
          <span className="auth-kicker">YOUR DEVELOPER + CAREER WORKSPACE</span>
          <h1>Build. Track.<br/><span>Get Hired.</span></h1>
          <p>One focused workspace for your projects, tasks and resume readiness.</p>
          <div className="showcase-pills"><span>✦ Resume intelligence</span><span>✓ Task tracking</span><span>↗ ATS ready</span></div>
          <div className="orbit-card"><div className="orbit-dot">B</div><div><strong>Career momentum</strong><small>Buildnest keeps your work moving forward.</small></div></div>
        </section>
        <section className="auth-card">
          <div className="auth-card-head"><div className="auth-mini-mark"><Icon name="spark" size={19}/></div><span>BUILDNEST</span></div>
          <h2>{login ? 'Welcome Back! 👋' : 'Create your account'}</h2>
          <p className="auth-subtitle">{login ? 'Log in to continue to your workspace.' : 'Start building your career workspace today.'}</p>
          <div className="auth-switch"><button className={login ? 'selected' : ''} onClick={() => {setMode('login');setError('')}}>Log in</button><button className={!login ? 'selected' : ''} onClick={() => {setMode('register');setError('')}}>Register</button></div>
          <form onSubmit={submit} className="auth-form">
            <label>Username</label>
            <div className="auth-input"><Icon name="user" size={17}/><input value={username} onChange={e => setUsername(e.target.value)} placeholder="Enter your username" required autoComplete="username"/></div>
            <label>Password</label>
            <div className="auth-input"><Icon name="lock" size={17}/><input value={password} onChange={e => setPassword(e.target.value)} placeholder="Enter your password" type={showPassword ? 'text' : 'password'} required autoComplete={login ? 'current-password' : 'new-password'}/><button type="button" className="eye-button" onClick={() => setShowPassword(v => !v)}><Icon name="eye" size={17}/></button></div>
            {login && <div className="remember-row"><label><input type="checkbox"/> <span>Remember me</span></label><span className="muted-link">Secure JWT login</span></div>}
            {error && <div className="auth-error">{error}</div>}
            <button className="auth-submit" disabled={loading}>{loading ? 'Please wait...' : login ? 'Log in' : 'Create account'} <Icon name="arrow" size={16}/></button>
          </form>
          <p className="auth-foot">{login ? 'No account?' : 'Already registered?'} <button onClick={() => {setMode(login ? 'register' : 'login');setError('')}}>{login ? 'Register' : 'Log in'}</button></p>
          <small className="admin-note">First registered account becomes ADMIN automatically.</small>
        </section>
      </div>
      <div className="auth-bottom">BUILDNEST • Build. Track. Get Hired.</div>
    </div>
  );
}
