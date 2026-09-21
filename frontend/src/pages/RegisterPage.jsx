import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { apiError } from '../api/client.js';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    username: '',
    password: '',
    segment: 'Gig Economy',
    bankedStatus: 'UNBANKED',
    protectedClass: ''
  });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await register(form);
      navigate('/portal');
    } catch (err) {
      setError(apiError(err, 'Registration failed.'));
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
        <h2>Create an applicant account</h2>
        <p className="muted small">Link alternative data to get a fair, transparent credit decision.</p>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={submit}>
          <div className="field">
            <label>Full name</label>
            <input value={form.fullName} onChange={update('fullName')} required />
          </div>
          <div className="field">
            <label>Username</label>
            <input value={form.username} onChange={update('username')} required />
          </div>
          <div className="field">
            <label>Password (minimum 8 characters)</label>
            <input type="password" value={form.password} onChange={update('password')} required minLength={8} />
          </div>
          <div className="field">
            <label>Segment</label>
            <input value={form.segment} onChange={update('segment')} />
          </div>
          <div className="field">
            <label>File type</label>
            <select value={form.bankedStatus} onChange={update('bankedStatus')}>
              <option value="UNBANKED">Unbanked</option>
              <option value="THIN_FILE">Thin file</option>
              <option value="BANKED">Banked</option>
            </select>
          </div>
          <button className="btn" style={{ width: '100%' }} disabled={busy}>
            {busy ? 'Creating...' : 'Create account'}
          </button>
        </form>
        <div className="divider" />
        <p className="small muted">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
