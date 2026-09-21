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
    setStatus('loading');
    demoLogin(role)
      .then(() => active && setStatus('ready'))
      .catch(() => active && setStatus('error'));
    return () => {
      active = false;
    };
  }, [role, demoLogin]);

  if (status === 'error') {
    return (
      <div className="auth-shell">
        <div className="auth-card">
          <h2>Cannot reach the service</h2>
          <p className="muted">The backend is not responding yet. Wait for it to finish starting then reload.</p>
          <button className="btn" onClick={() => window.location.reload()}>Reload</button>
        </div>
      </div>
    );
  }
  if (status !== 'ready') {
    return <div className="auth-shell"><div className="auth-card">Entering...</div></div>;
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
