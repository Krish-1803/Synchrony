import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { apiError } from '../api/client.js';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const data = await login(username, password);
      navigate(data.role === 'CREDIT_OFFICER' ? '/underwriting' : '/portal');
    } catch (err) {
      setError(apiError(err, 'Sign in failed. Check your credentials.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="brand" style={{ color: 'var(--syf-charcoal)' }}>
          <span className="brand-mark">S</span>
          <span>Synchrony</span>
        </div>
        <h2>Sign in</h2>
        <p className="muted small">Access the applicant portal or the underwriting dashboard.</p>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={submit}>
          <div className="field">
            <label>Username</label>
            <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required />
          </div>
          <div className="field">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          <button className="btn" style={{ width: '100%' }} disabled={busy}>
            {busy ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <div className="divider" />
        <p className="small muted">
          New applicant? <Link to="/register">Create an account</Link>
        </p>
        <div className="alert alert-info small" style={{ marginTop: '0.5rem' }}>
          Demo logins. Officer: officer / Officer#2024. Applicant: maria / Applicant#2024.
        </div>
      </div>
    </div>
  );
}
