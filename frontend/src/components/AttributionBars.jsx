// Shows SHAP style additive attributions as diverging bars around a center line.
// Positive contributions lift the score, negative contributions lower it.
export default function AttributionBars({ attributions }) {
  if (!attributions || attributions.length === 0) {
    return <p className="muted small">No attribution data available.</p>;
  }
  const max = Math.max(...attributions.map((a) => Math.abs(a.contribution)), 0.0001);
  const sorted = [...attributions].sort((a, b) => b.contribution - a.contribution);

  return (
    <div>
      {sorted.map((a) => {
        const width = (Math.abs(a.contribution) / max) * 48;
        return (
          <div className="attr-row" key={a.key}>
            <div className="attr-label" title={a.key}>{a.label}</div>
            <div className="attr-bar-track">
              <div className="attr-center" />
              <div
                className={`attr-bar ${a.positive ? 'attr-pos' : 'attr-neg'}`}
                style={{ width: `${width}%` }}
              />
            </div>
            <div className="attr-value">{a.contribution >= 0 ? '+' : ''}{a.contribution.toFixed(3)}</div>
          </div>
        );
      })}
    </div>
  );
}
