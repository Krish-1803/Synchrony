const MAP = {
  APPROVE: { cls: 'badge-approve', text: 'Approved' },
  DECLINE: { cls: 'badge-decline', text: 'Declined' },
  REFER: { cls: 'badge-refer', text: 'Referred' }
};

export default function DecisionBadge({ decision }) {
  const item = MAP[decision] || { cls: 'badge-muted', text: decision || 'Pending' };
  return <span className={`badge ${item.cls}`}>{item.text}</span>;
}
