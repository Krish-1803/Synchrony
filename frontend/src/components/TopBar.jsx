import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function TopBar() {
  const { user } = useAuth();
  const location = useLocation();
  const roleText = user?.role === 'CREDIT_OFFICER' ? 'Credit Officer' : 'Applicant';
  const onUnderwriting = location.pathname.startsWith('/underwriting');

  return (
    <div className="topbar">
      <Link to="/" className="brand" style={{ color: '#fff', textDecoration: 'none' }}>
        <span className="brand-mark">S</span>
        <span>
          Synchrony <span className="brand-sub">Dynamic Risk Assessment</span>
        </span>
      </Link>
      <div className="topbar-right">
        {user && <span className="role-pill">{roleText}</span>}
        <Link
          to={onUnderwriting ? '/portal' : '/underwriting'}
          style={{ color: 'var(--syf-gold)', fontWeight: 600 }}
        >
          {onUnderwriting ? 'Applicant portal' : 'Underwriting dashboard'}
        </Link>
        <Link to="/" style={{ color: '#fff' }}>Portals</Link>
      </div>
    </div>
  );
}
