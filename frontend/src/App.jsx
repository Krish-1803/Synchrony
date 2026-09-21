import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext.jsx';
import TopBar from './components/TopBar.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ApplicantPortal from './pages/ApplicantPortal.jsx';
import UnderwritingDashboard from './pages/UnderwritingDashboard.jsx';

function Home() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'CREDIT_OFFICER' ? '/underwriting' : '/portal'} replace />;
}

function Protected({ role, children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="container">Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  const { user } = useAuth();
  return (
    <>
      {user && <TopBar />}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/portal"
          element={
            <Protected role="APPLICANT">
              <ApplicantPortal />
            </Protected>
          }
        />
        <Route
          path="/underwriting"
          element={
            <Protected role="CREDIT_OFFICER">
              <UnderwritingDashboard />
            </Protected>
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
