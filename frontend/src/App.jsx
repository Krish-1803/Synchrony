import { useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext.jsx';
import TopBar from './components/TopBar.jsx';
import LandingPage from './pages/LandingPage.jsx';
import ApplicantPortal from './pages/ApplicantPortal.jsx';
import UnderwritingDashboard from './pages/UnderwritingDashboard.jsx';

// Auto-authenticates as the demo account for the given role, then renders the
// portal. This replaces the login screen so both portals open directly.
function DemoGate({ role, children }) {
  const { demoLogin } = useAuth();
  const [status, setStatus] = useState('loading');

  useEffect(() => {
    let active = true;
    let attempts = 0;
    setStatus('loading');
    // Retry while the backend finishes its first-boot, so opening the app never
    // needs a manual reload.
    const attempt = () => {
      demoLogin(role)
        .then(() => active && setStatus('ready'))
        .catch(() => {
          if (!active) return;
          attempts += 1;
          if (attempts >= 60) {
            setStatus('error');
          } else {
            setTimeout(attempt, 2000);
          }
        });
    };
    attempt();
    return () => {
      active = false;
    };
  }, [role, demoLogin]);

  if (status === 'error') {
    return (
      <div className="auth-shell">
        <div className="auth-card">
          <h2>Still starting</h2>
          <p className="muted">The service is taking longer than usual. It will connect automatically.</p>
          <button className="btn" onClick={() => window.location.reload()}>Reload now</button>
        </div>
      </div>
    );
  }
  if (status !== 'ready') {
    return (
      <div className="auth-shell">
        <div className="auth-card">
          <h2>Starting the service</h2>
          <p className="muted">Connecting to the underwriting engine. This can take a moment on first launch.</p>
        </div>
      </div>
    );
  }
  return (
    <>
      <TopBar />
      {children}
    </>
  );
}

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route
          path="/portal"
          element={
            <DemoGate role="APPLICANT">
              <ApplicantPortal />
            </DemoGate>
          }
        />
        <Route
          path="/underwriting"
          element={
            <DemoGate role="CREDIT_OFFICER">
              <UnderwritingDashboard />
            </DemoGate>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <div className="footer-note">
        Synchrony Financial Inclusion prototype. Alternative data underwriting with explainable, auditable decisions.
      </div>
    </>
  );
}
