import { useNavigate } from 'react-router-dom';

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="auth-shell">
      <div style={{ width: '100%', maxWidth: 880 }}>
        <div className="brand" style={{ color: '#fff', justifyContent: 'center', marginBottom: '0.5rem' }}>
          <span className="brand-mark">S</span>
          <span style={{ fontSize: '1.3rem' }}>
            Synchrony <span className="brand-sub">Dynamic Risk Assessment</span>
          </span>
        </div>
        <p style={{ textAlign: 'center', color: '#d7d7d7', marginTop: 0, marginBottom: '1.5rem' }}>
          AI-powered financial inclusion. Choose a portal to continue.
        </p>

        <div className="grid-2">
          <button className="card" style={cardStyle} onClick={() => navigate('/portal')}>
            <div style={iconStyle}>A</div>
            <h3 style={{ borderBottom: 'none' }}>Applicant Portal</h3>
            <p className="muted" style={{ margin: 0 }}>
              Link alternative data streams and run a dynamic credit evaluation with a transparent decision and recourse.
            </p>
            <span className="badge badge-approve" style={{ marginTop: '1rem' }}>Enter as applicant</span>
          </button>

          <button className="card" style={cardStyle} onClick={() => navigate('/underwriting')}>
            <div style={iconStyle}>U</div>
            <h3 style={{ borderBottom: 'none' }}>Underwriting Dashboard</h3>
            <p className="muted" style={{ margin: 0 }}>
              Review risk profiles, feature attribution, fairness metrics and similar cases, and apply manual overrides.
            </p>
            <span className="badge badge-refer" style={{ marginTop: '1rem' }}>Enter as credit officer</span>
          </button>
        </div>
      </div>
    </div>
  );
}

const cardStyle = {
  textAlign: 'left',
  cursor: 'pointer',
  border: '1px solid #e6e6e6',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  gap: '0.4rem',
  minHeight: 220
};

const iconStyle = {
  width: 48,
  height: 48,
  borderRadius: 10,
  background: 'var(--syf-gold)',
  color: 'var(--syf-charcoal)',
  display: 'grid',
  placeItems: 'center',
  fontWeight: 800,
  fontSize: '1.4rem',
  marginBottom: '0.5rem'
};
