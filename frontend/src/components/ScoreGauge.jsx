// Renders the hybrid score against the Synchrony 300 to 850 band.
export default function ScoreGauge({ score, tier }) {
  const clamped = Math.max(300, Math.min(850, score || 300));
  const percent = ((clamped - 300) / 550) * 100;
  const tierText = (tier || '').replace(/_/g, ' ').toLowerCase();

  return (
    <div className="gauge">
      <div>
        <div className="gauge-number">{score ?? '--'}</div>
        <div className="small muted">{tierText}</div>
      </div>
      <div style={{ flex: 1 }}>
        <div className="row-between small muted">
          <span>300</span>
          <span>850</span>
        </div>
        <div className="gauge-track">
          <div className="gauge-marker" style={{ left: `${percent}%` }} />
        </div>
      </div>
    </div>
  );
}
