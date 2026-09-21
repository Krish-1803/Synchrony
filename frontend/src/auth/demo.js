// Demo credentials used to auto-authenticate so the portals are reachable
// without a login screen. These match the seeded accounts. Override with
// VITE_DEMO_* build variables if the seed passwords are changed. This is a
// demo convenience only. The backend still enforces JWT auth and RBAC.
export const DEMO_ACCOUNTS = {
  APPLICANT: {
    username: import.meta.env.VITE_DEMO_APPLICANT_USER || 'maria',
    password: import.meta.env.VITE_DEMO_APPLICANT_PASS || 'Applicant#2024'
  },
  CREDIT_OFFICER: {
    username: import.meta.env.VITE_DEMO_OFFICER_USER || 'officer',
    password: import.meta.env.VITE_DEMO_OFFICER_PASS || 'Officer#2024'
  }
};
