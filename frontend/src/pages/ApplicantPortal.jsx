import { useEffect, useMemo, useState } from 'react';
import api, { apiError } from '../api/client.js';
import DecisionBadge from '../components/DecisionBadge.jsx';
import ScoreGauge from '../components/ScoreGauge.jsx';

const DATA_SOURCES = [
  {
    type: 'MOBILE_MONEY',
    title: 'Mobile money wallet',
    hint: 'Cash flow and settlement behavior from your wallet.',
    fields: [
      { key: 'inflow', label: 'Monthly inflow', value: 3200 },
      { key: 'outflow', label: 'Monthly outflow', value: 2500 },
      { key: 'avgDailyBalance', label: 'Average daily balance', value: 620 },
      { key: 'peakBalance', label: 'Peak balance', value: 900 },
      { key: 'settlementDays', label: 'Settlement days', value: 6 },
      { key: 'counterpartyCount', label: 'Counterparties', value: 35 }
    ]
  },
  {
    type: 'UTILITY_PAYMENTS',
    title: 'Utility payments',
    hint: 'On-time payment record across your bills.',
    fields: [
      { key: 'onTimePayments', label: 'On-time payments', value: 22 },
      { key: 'totalPayments', label: 'Total payments', value: 24 }
    ]
  },
  {
    type: 'TELCO_CDR',
    title: 'Telecom activity',
    hint: 'Top-up regularity from your mobile plan.',
    fields: [{ key: 'topupConsistency', label: 'Top-up consistency (0 to 1)', value: 0.8, step: 0.05 }]
  },
  {
    type: 'BEHAVIORAL_SDK',
    title: 'App behavior',
    hint: 'Device and app signals captured in-app.',
    fields: [
      { key: 'typingStability', label: 'Typing stability (0 to 1)', value: 0.75, step: 0.05 },
      { key: 'appDiversityCount', label: 'App diversity', value: 24 },
      { key: 'sessionRegularity', label: 'Session regularity (0 to 1)', value: 0.7, step: 0.05 }
    ]
  },
  {
    type: 'TRANSACTION_GRAPH',
    title: 'Transaction network',
    hint: 'Network stability and distance from fraud clusters.',
    fields: [
      { key: 'networkStability', label: 'Network stability (0 to 1)', value: 0.75, step: 0.05 },
      { key: 'fraudProximity', label: 'Fraud proximity (0 to 1)', value: 0.1, step: 0.05 }
    ]
  },
  {
    type: 'BUREAU_TRADELINE',
    title: 'Bureau trade lines (optional)',
    hint: 'Only if you have a traditional credit file.',
    fields: [
      { key: 'tradeLineCount', label: 'Trade lines', value: 3 },
      { key: 'utilization', label: 'Utilization (0 to 1)', value: 0.3, step: 0.05 }
    ]
  }
];

function initialValues() {
  const state = {};
  DATA_SOURCES.forEach((s) => {
    state[s.type] = {};
    s.fields.forEach((f) => {
      state[s.type][f.key] = f.value;
    });
  });
  return state;
}

export default function ApplicantPortal() {
  const [applications, setApplications] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [values, setValues] = useState(initialValues);
  const [linked, setLinked] = useState({});
  const [assessment, setAssessment] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [newApp, setNewApp] = useState({ productType: 'Private Label Card', requestedAmount: 1500 });

  const selected = useMemo(
    () => applications.find((a) => a.id === selectedId) || null,
    [applications, selectedId]
  );

  const loadApplications = async () => {
    try {
      const { data } = await api.get('/api/applications');
      setApplications(data);
      if (data.length && selectedId == null) {
        setSelectedId(data[0].id);
      }
    } catch (err) {
      setError(apiError(err, 'Could not load your applications.'));
    }
  };

  useEffect(() => {
    loadApplications();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const createApplication = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const { data } = await api.post('/api/applications', {
        productType: newApp.productType,
        requestedAmount: Number(newApp.requestedAmount)
      });
      setAssessment(null);
      setLinked({});
      await loadApplications();
      setSelectedId(data.id);
      setMessage(`Application #${data.id} created. Link your data then run the evaluation.`);
    } catch (err) {
      setError(apiError(err, 'Could not create the application.'));
    } finally {
      setBusy(false);
    }
  };

  const setField = (type, key, val) => {
    setValues((prev) => ({ ...prev, [type]: { ...prev[type], [key]: val } }));
  };

  const linkSource = async (source) => {
    if (!selectedId) return;
    setError('');
    setBusy(true);
    try {
      const payload = {};
      source.fields.forEach((f) => {
        payload[f.key] = Number(values[source.type][f.key]);
      });
      await api.post(`/api/applications/${selectedId}/data`, { sourceType: source.type, payload });
      setLinked((prev) => ({ ...prev, [source.type]: true }));
      setMessage(`${source.title} linked.`);
    } catch (err) {
      setError(apiError(err, 'Could not link this data stream.'));
    } finally {
      setBusy(false);
    }
  };

  const evaluate = async () => {
    if (!selectedId) return;
    setError('');
    setBusy(true);
    try {
      const { data } = await api.post(`/api/applications/${selectedId}/evaluate`);
      setAssessment(data);
      await loadApplications();
      setMessage('');
    } catch (err) {
      setError(apiError(err, 'Evaluation failed.'));
    } finally {
      setBusy(false);
    }
  };

  const selectApplication = async (id) => {
    setSelectedId(id);
    setAssessment(null);
    setLinked({});
    setMessage('');
    try {
      const { data } = await api.get(`/api/applications/${id}/assessment`);
      setAssessment(data);
    } catch {
      // No assessment yet is expected for a new application.
    }
  };

  const linkedCount = Object.values(linked).filter(Boolean).length;

  return (
    <div className="container">
      <div className="page-head">
        <h1>Applicant portal</h1>
        <p>Link alternative data streams then run a dynamic credit evaluation.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-info">{message}</div>}

      <div className="grid-2">
        <div className="card">
          <h3>Start an application</h3>
          <form onSubmit={createApplication}>
            <div className="field">
              <label>Product</label>
              <select
                value={newApp.productType}
                onChange={(e) => setNewApp({ ...newApp, productType: e.target.value })}
              >
                <option>Private Label Card</option>
                <option>Co-Branded Card</option>
                <option>Point-of-Sale Financing</option>
              </select>
            </div>
            <div className="field">
              <label>Requested amount</label>
              <input
                type="number"
                min="100"
                value={newApp.requestedAmount}
                onChange={(e) => setNewApp({ ...newApp, requestedAmount: e.target.value })}
              />
            </div>
            <button className="btn" disabled={busy}>Create application</button>
          </form>
        </div>

        <div className="card">
          <h3>Your applications</h3>
          {applications.length === 0 && <p className="muted small">No applications yet.</p>}
          {applications.length > 0 && (
            <table className="table">
              <thead>
                <tr><th>ID</th><th>Product</th><th>Status</th><th>Decision</th></tr>
              </thead>
              <tbody>
                {applications.map((a) => (
                  <tr
                    key={a.id}
                    className={`clickable ${a.id === selectedId ? 'selected' : ''}`}
                    onClick={() => selectApplication(a.id)}
                  >
                    <td>#{a.id}</td>
                    <td>{a.productType}</td>
                    <td><span className="badge badge-muted">{a.status}</span></td>
                    <td>{a.decision ? <DecisionBadge decision={a.decision} /> : <span className="muted">--</span>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {selected && (
        <div className="card">
          <div className="row-between">
            <h3>Link data for application #{selected.id}</h3>
            <span className="small muted">{linkedCount} streams linked this session</span>
          </div>
          <div className="grid-3">
            {DATA_SOURCES.map((source) => (
              <div key={source.type} className="stat" style={{ padding: '0.9rem' }}>
                <div className="row-between">
                  <strong>{source.title}</strong>
                  {linked[source.type] && <span className="badge badge-approve">Linked</span>}
                </div>
                <p className="small muted" style={{ marginTop: 2 }}>{source.hint}</p>
                {source.fields.map((f) => (
                  <div className="field" key={f.key} style={{ marginBottom: '0.5rem' }}>
                    <label>{f.label}</label>
                    <input
                      type="number"
                      step={f.step || 1}
                      value={values[source.type][f.key]}
                      onChange={(e) => setField(source.type, f.key, e.target.value)}
                    />
                  </div>
                ))}
                <button className="btn-ghost" onClick={() => linkSource(source)} disabled={busy}>
                  Link stream
                </button>
              </div>
            ))}
          </div>
          <div className="divider" />
          <button className="btn btn-dark" onClick={evaluate} disabled={busy}>
            {busy ? 'Running...' : 'Run dynamic underwriting'}
          </button>
          <p className="small muted" style={{ marginTop: '0.5rem' }}>
            Your identity data is anonymized before any AI processing.
          </p>
        </div>
      )}

      {assessment && (
        <div className="card">
          <div className="row-between">
            <h3>Decision for application #{assessment.applicationId}</h3>
            <DecisionBadge decision={assessment.decision} />
          </div>
          <ScoreGauge score={assessment.score} tier={assessment.riskTier} />
          <div className="grid-3" style={{ marginTop: '1rem' }}>
            <div className="stat">
              <div className="label">Hybrid score</div>
              <div className="value">{assessment.score}</div>
            </div>
            <div className="stat">
              <div className="label">Default probability</div>
              <div className="value">{assessment.pdPercent}%</div>
            </div>
            <div className="stat">
              <div className="label">Explanation source</div>
              <div className="value" style={{ fontSize: '1.1rem' }}>{assessment.explanationSource}</div>
            </div>
          </div>

          <div className="divider" />
          <h4>Why</h4>
          <p>{assessment.rationale}</p>

          {assessment.positiveDrivers?.length > 0 && (
            <>
              <h4>Strongest positive signals</h4>
              <div className="tag-list">
                {assessment.positiveDrivers.map((d) => (
                  <span key={d} className="pill active">{d}</span>
                ))}
              </div>
            </>
          )}

          {assessment.recourse?.length > 0 && (
            <>
              <div className="divider" />
              <h4>Your path to approval</h4>
              {assessment.recourseSummary && <p className="muted">{assessment.recourseSummary}</p>}
              {assessment.recourse.map((step) => (
                <div className="recourse-step" key={step.key}>{step.description}</div>
              ))}
            </>
          )}
        </div>
      )}
    </div>
  );
}
