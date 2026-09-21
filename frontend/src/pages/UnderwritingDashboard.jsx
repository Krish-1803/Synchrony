import { useEffect, useState } from 'react';
import api, { apiError } from '../api/client.js';
import DecisionBadge from '../components/DecisionBadge.jsx';
import ScoreGauge from '../components/ScoreGauge.jsx';
import AttributionBars from '../components/AttributionBars.jsx';

function BucketWeights({ weights }) {
  if (!weights) return null;
  const entries = [
    ['Traditional', weights.TRADITIONAL || 0],
    ['Alternative', weights.ALTERNATIVE || 0],
    ['Network (GNN)', weights.GNN || 0]
  ];
  return (
    <div>
      {entries.map(([label, w]) => (
        <div className="attr-row" key={label} style={{ gridTemplateColumns: '140px 1fr 50px' }}>
          <div className="attr-label">{label}</div>
          <div className="attr-bar-track" style={{ justifyContent: 'flex-start' }}>
            <div className="attr-bar" style={{ position: 'relative', width: `${w * 100}%`, background: 'var(--syf-gold)' }} />
          </div>
          <div className="attr-value">{Math.round(w * 100)}%</div>
        </div>
      ))}
    </div>
  );
}

export default function UnderwritingDashboard() {
  const [queue, setQueue] = useState([]);
  const [fairness, setFairness] = useState(null);
  const [status, setStatus] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');
  const [override, setOverride] = useState({ decision: 'APPROVE', reason: '' });
  const [busy, setBusy] = useState(false);

  const loadQueue = async () => {
    try {
      const [{ data: q }, { data: f }, { data: s }] = await Promise.all([
        api.get('/api/underwriting/queue'),
        api.get('/api/underwriting/fairness'),
        api.get('/api/meta/status')
      ]);
      setQueue(q);
      setFairness(f);
      setStatus(s);
    } catch (err) {
      setError(apiError(err, 'Could not load the underwriting queue.'));
    }
  };

  useEffect(() => {
    loadQueue();
  }, []);

  const openDetail = async (id) => {
    setSelectedId(id);
    setDetail(null);
    setError('');
    try {
      const { data } = await api.get(`/api/underwriting/applications/${id}`);
      setDetail(data);
      setOverride({ decision: data.decision === 'APPROVE' ? 'DECLINE' : 'APPROVE', reason: '' });
    } catch (err) {
      setError(apiError(err, 'This application has no evaluation yet.'));
    }
  };

  const submitOverride = async () => {
    if (!override.reason.trim()) {
      setError('An override reason is required.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const { data } = await api.post(`/api/underwriting/applications/${selectedId}/override`, override);
      setDetail(data);
      await loadQueue();
    } catch (err) {
      setError(apiError(err, 'Override failed.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="container">
      <div className="page-head">
        <h1>Underwriting dashboard</h1>
        <p>Review risk profiles, feature attribution, fairness metrics and apply manual overrides.</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="grid-3">
        <div className="stat">
          <div className="label">Applications in queue</div>
          <div className="value">{queue.length}</div>
        </div>
        <div className="stat">
          <div className="label">Disparate impact ratio</div>
          <div className="value" style={{ color: fairness && !fairness.compliant ? 'var(--decline)' : 'var(--approve)' }}>
            {fairness ? fairness.disparateImpactRatio.toFixed(2) : '--'}
          </div>
          <div className="small muted">
            {fairness ? (fairness.compliant ? 'Within the four fifths rule' : 'Below 0.80, recalibration advised') : ''}
          </div>
        </div>
        <div className="stat">
          <div className="label">Decision engine</div>
          <div className="value" style={{ fontSize: '1rem' }}>
            {status ? status.modelVersion : '--'}
          </div>
          <div className="small muted">
            {status ? `Bedrock ${status.bedrockEnabled ? 'on' : 'fallback'} | pgvector ${status.pgvectorEnabled ? 'on' : 'fallback'}` : ''}
          </div>
        </div>
      </div>

      <div className="card">
        <h3>Applicant queue</h3>
        <table className="table">
          <thead>
            <tr>
              <th>App</th><th>Reference</th><th>Segment</th><th>File</th>
              <th>Score</th><th>Tier</th><th>Decision</th>
            </tr>
          </thead>
          <tbody>
            {queue.map((a) => (
              <tr
                key={a.id}
                className={`clickable ${a.id === selectedId ? 'selected' : ''}`}
                onClick={() => openDetail(a.id)}
              >
                <td>#{a.id}</td>
                <td className="mono">{a.applicantRef}</td>
                <td>{a.applicantSegment || '--'}</td>
                <td><span className="badge badge-muted">{a.bankedStatus}</span></td>
                <td><strong>{a.score ?? '--'}</strong></td>
                <td className="small">{(a.riskTier || '').replace(/_/g, ' ')}</td>
                <td>{a.decision ? <DecisionBadge decision={a.decision} /> : <span className="muted">--</span>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {detail && (
        <>
          <div className="card">
            <div className="row-between">
              <h3>Risk profile {detail.application.applicantRef}</h3>
              <div>
                {detail.manualOverride && <span className="badge badge-refer" style={{ marginRight: 8 }}>Manual override</span>}
                <DecisionBadge decision={detail.decision} />
              </div>
            </div>
            <ScoreGauge score={detail.score} tier={detail.riskTier} />
            <div className="grid-3" style={{ marginTop: '1rem' }}>
              <div className="stat"><div className="label">Default probability</div><div className="value">{detail.pdPercent}%</div></div>
              <div className="stat"><div className="label">Logit z</div><div className="value">{detail.logitZ}</div></div>
              <div className="stat"><div className="label">Product</div><div className="value" style={{ fontSize: '1rem' }}>{detail.application.productType}</div></div>
            </div>
          </div>

          <div className="grid-2">
            <div className="card">
              <h3>Model weighting</h3>
              <p className="small muted">Weights reallocate automatically when a data group is missing.</p>
              <BucketWeights weights={detail.bucketWeights} />
            </div>
            <div className="card">
              <h3>Explainable rationale</h3>
              <p>{detail.rationale}</p>
              {detail.principalReasons?.length > 0 && (
                <div className="tag-list">
                  {detail.principalReasons.map((r) => <span key={r} className="pill">{r}</span>)}
                </div>
              )}
            </div>
          </div>

          <div className="card">
            <h3>Feature attribution</h3>
            <p className="small muted">Additive contributions to the score logit. Positive lifts, negative lowers.</p>
            <AttributionBars attributions={detail.attributions} />
          </div>

          {detail.recourse?.length > 0 && (
            <div className="card">
              <h3>Counterfactual recourse</h3>
              {detail.recourse.map((step) => (
                <div className="recourse-step" key={step.key}>{step.description}</div>
              ))}
            </div>
          )}

          <div className="grid-2">
            <div className="card">
              <h3>Similar cases</h3>
              {detail.similarCases?.length > 0 ? (
                <table className="table">
                  <thead><tr><th>App</th><th>Reference</th><th>Decision</th><th>Score</th><th>Similarity</th></tr></thead>
                  <tbody>
                    {detail.similarCases.map((c) => (
                      <tr key={c.applicationId}>
                        <td>#{c.applicationId}</td>
                        <td className="mono">{c.applicantRef}</td>
                        <td><DecisionBadge decision={c.decision} /></td>
                        <td>{c.score}</td>
                        <td>{(c.similarity * 100).toFixed(0)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p className="muted small">No comparable cases in the vector store yet.</p>
              )}
            </div>

            <div className="card">
              <h3>Manual override</h3>
              <p className="small muted">Overrides are written to the immutable audit trail with your reason.</p>
              <div className="field">
                <label>Decision</label>
                <select value={override.decision} onChange={(e) => setOverride({ ...override, decision: e.target.value })}>
                  <option value="APPROVE">Approve</option>
                  <option value="DECLINE">Decline</option>
                  <option value="REFER">Refer</option>
                </select>
              </div>
              <div className="field">
                <label>Reason</label>
                <input
                  value={override.reason}
                  onChange={(e) => setOverride({ ...override, reason: e.target.value })}
                  placeholder="State the basis for this override"
                />
              </div>
              <button className="btn btn-dark" onClick={submitOverride} disabled={busy}>
                {busy ? 'Applying...' : 'Apply override'}
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
