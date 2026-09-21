import { useAuth } from '../auth/AuthContext.jsx';

export default function TopBar() {
  const { user, logout } = useAuth();
  const roleText = user?.role === 'CREDIT_OFFICER' ? 'Credit Officer' : 'Applicant';

  return (
    <div className="topbar">
      <div className="brand">
        <span className="brand-mark">S</span>
        <span>
          Synchrony <span className="brand-sub">Dynamic Risk Assessment</span>
        </span>
      </div>
      {user && (
        <div className="topbar-right">
          <span className="role-pill">{roleText}</span>
          <span>{user.fullName || user.username}</span>
          <button className="btn-ghost" style={{ color: '#fff', borderColor: '#555' }} onClick={logout}>
            Sign out
          </button>
        </div>
      )}
    </div>
  );
}
