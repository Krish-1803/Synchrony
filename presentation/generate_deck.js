const pptxgen = require('pptxgenjs');
const pres = new pptxgen();
pres.layout = 'LAYOUT_WIDE'; // 13.33 x 7.5
pres.author = 'Synchrony';
pres.title = 'AI-Powered Financial Inclusion';

const GOLD = 'FFC220';
const GOLD_DK = 'E0A800';
const CHAR = '333333';
const CHAR_DK = '242424';
const OFF = 'F4F4F4';
const WHITE = 'FFFFFF';
const INK = '333333';
const SOFT = '5C5C5C';
const GREEN = '1A7F47';
const RED = 'B3261E';
const HEAD = 'Calibri';
const BODY = 'Calibri';

const W = 13.33;
const H = 7.5;

function shadow() {
  return { type: 'outer', color: '999999', blur: 6, offset: 2, angle: 90, opacity: 0.35 };
}

function brandMark(slide, x, y, dark) {
  slide.addShape('roundRect', { x, y, w: 0.42, h: 0.42, rectRadius: 0.07, fill: { color: GOLD } });
  slide.addText('S', { x, y, w: 0.42, h: 0.42, align: 'center', valign: 'middle', fontFace: HEAD, fontSize: 20, bold: true, color: CHAR, isTextBox: true, margin: 0 });
  slide.addText([
    { text: 'Synchrony ', options: { color: dark ? WHITE : CHAR, bold: true } },
    { text: 'Dynamic Risk Assessment', options: { color: GOLD_DK } }
  ], { x: x + 0.5, y, w: 6, h: 0.42, valign: 'middle', fontFace: HEAD, fontSize: 13, isTextBox: true, margin: 0 });
}

function footer(slide, n) {
  slide.addText('Synchrony Financial Inclusion', { x: 0.5, y: 7.05, w: 6, h: 0.3, fontFace: BODY, fontSize: 9, color: SOFT, isTextBox: true, margin: 0 });
  slide.addText(String(n), { x: 12.5, y: 7.05, w: 0.4, h: 0.3, align: 'right', fontFace: BODY, fontSize: 9, color: SOFT, isTextBox: true, margin: 0 });
}

function contentHeader(slide, kicker, title) {
  brandMark(slide, 0.5, 0.4, false);
  slide.addText(kicker.toUpperCase(), { x: 0.5, y: 1.15, w: 12, h: 0.3, fontFace: BODY, fontSize: 12, bold: true, color: GOLD_DK, charSpacing: 2, isTextBox: true, margin: 0 });
  slide.addText(title, { x: 0.5, y: 1.4, w: 12.3, h: 0.8, fontFace: HEAD, fontSize: 34, bold: true, color: CHAR, isTextBox: true, margin: 0 });
}

function card(slide, x, y, w, h, fill) {
  slide.addShape('roundRect', { x, y, w, h, rectRadius: 0.08, fill: { color: fill || WHITE }, line: { color: 'E6E6E6', width: 1 }, shadow: shadow() });
}

// ---------------------------------------------------------------------------
// Slide 1 - Title and Vision
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: CHAR };
  s.addShape('rect', { x: 0, y: 0, w: W, h: H, fill: { color: CHAR } });
  // gold accent block
  s.addShape('roundRect', { x: 9.6, y: -1.2, w: 5.2, h: 5.2, rectRadius: 0.2, fill: { color: GOLD, transparency: 82 }, line: { type: 'none' } });
  s.addShape('roundRect', { x: 10.8, y: 3.2, w: 5.2, h: 5.2, rectRadius: 0.2, fill: { color: GOLD, transparency: 88 }, line: { type: 'none' } });
  brandMark(s, 0.7, 0.6, true);

  s.addText('AI-POWERED FINANCIAL INCLUSION', { x: 0.7, y: 2.5, w: 11, h: 0.4, fontFace: BODY, fontSize: 16, bold: true, color: GOLD, charSpacing: 3, isTextBox: true, margin: 0 });
  s.addText('Dynamic Risk Assessment for Underserved Segments', { x: 0.7, y: 2.95, w: 11.5, h: 1.6, fontFace: HEAD, fontSize: 44, bold: true, color: WHITE, isTextBox: true, margin: 0, lineSpacingMultiple: 1.0 });
  s.addText('Fair and accurate credit for thin-file and unbanked applicants, using alternative data and explainable AI on the Synchrony 300 to 850 band.',
    { x: 0.7, y: 4.7, w: 9.2, h: 1.0, fontFace: BODY, fontSize: 16, color: 'D7D7D7', isTextBox: true, margin: 0 });

  const chips = ['Alternative data', 'Explainable AI', 'Fair lending', 'Audit ready'];
  chips.forEach((c, i) => {
    const x = 0.7 + i * 2.65;
    s.addShape('roundRect', { x, y: 6.0, w: 2.45, h: 0.5, rectRadius: 0.25, fill: { color: WHITE, transparency: 88 }, line: { color: GOLD, width: 1 } });
    s.addText(c, { x, y: 6.0, w: 2.45, h: 0.5, align: 'center', valign: 'middle', fontFace: BODY, fontSize: 12, bold: true, color: GOLD, isTextBox: true, margin: 0 });
  });
  s.addNotes('45 million adults in the United States are credit invisible or carry thin files. Synchrony sits at the point of sale, so safely approving this segment grows merchant volume and customer lifetime value.');
})();

// ---------------------------------------------------------------------------
// Slide 2 - The Challenge
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: OFF };
  contentHeader(s, 'The Challenge', 'Conventional scoring leaves creditworthy people behind');

  // Big stat block on the left
  card(s, 0.5, 2.5, 3.9, 3.9, CHAR);
  s.addText('45M', { x: 0.7, y: 3.1, w: 3.5, h: 1.2, fontFace: HEAD, fontSize: 66, bold: true, color: GOLD, isTextBox: true, margin: 0 });
  s.addText('adults in the United States are credit invisible or carry thin files',
    { x: 0.7, y: 4.3, w: 3.5, h: 1.4, fontFace: BODY, fontSize: 15, color: WHITE, isTextBox: true, margin: 0 });

  const points = [
    ['Bureau dependent', 'Legacy FICO and VantageScore models rely on trade lines and repayment history.'],
    ['No file, high risk', 'Applicants with no record are treated as high risk by default.'],
    ['Backward looking', 'Scorecards update on monthly batches and miss real-time cash flow.'],
    ['Signals ignored', 'Recurring rent, utility and mobile money payments never count.']
  ];
  points.forEach((p, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 4.7 + col * 4.25;
    const y = 2.5 + row * 1.95;
    card(s, x, y, 4.0, 1.75, WHITE);
    s.addShape('roundRect', { x: x + 0.25, y: y + 0.28, w: 0.5, h: 0.5, rectRadius: 0.1, fill: { color: GOLD } });
    s.addText(String(i + 1), { x: x + 0.25, y: y + 0.28, w: 0.5, h: 0.5, align: 'center', valign: 'middle', fontFace: HEAD, fontSize: 18, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(p[0], { x: x + 0.9, y: y + 0.22, w: 3.0, h: 0.4, fontFace: HEAD, fontSize: 16, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(p[1], { x: x + 0.9, y: y + 0.62, w: 3.0, h: 1.0, fontFace: BODY, fontSize: 12.5, color: SOFT, isTextBox: true, margin: 0 });
  });
  footer(s, 2);
  s.addNotes('The result is a self-reinforcing cycle. No file means no approval, and no approval means no way to build a file. We break that cycle with data that already exists.');
})();

// ---------------------------------------------------------------------------
// Slide 3 - Data Strategy
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: WHITE };
  contentHeader(s, 'Data Strategy', 'Responsibly leveraging alternative data');

  const domains = [
    ['Mobile money', 'Inflow ratio, balance stability, settlement velocity and counterparty diversity.'],
    ['Utility and telecom', 'Payment punctuality and top-up consistency.'],
    ['App behavior', 'Typing stability, app diversity and session regularity.'],
    ['Transaction graph', 'Network stability and distance from fraud clusters.']
  ];
  domains.forEach((d, i) => {
    const x = 0.5 + i * 3.13;
    card(s, x, 2.4, 2.9, 2.5, OFF);
    s.addShape('ellipse', { x: x + 0.25, y: 2.65, w: 0.6, h: 0.6, fill: { color: GOLD } });
    s.addText(String(i + 1), { x: x + 0.25, y: 2.65, w: 0.6, h: 0.6, align: 'center', valign: 'middle', fontFace: HEAD, fontSize: 20, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(d[0], { x: x + 0.25, y: 3.4, w: 2.4, h: 0.6, fontFace: HEAD, fontSize: 16, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(d[1], { x: x + 0.25, y: 3.95, w: 2.45, h: 0.9, fontFace: BODY, fontSize: 12, color: SOFT, isTextBox: true, margin: 0 });
  });

  // Governance band
  card(s, 0.5, 5.15, 12.33, 1.35, CHAR);
  s.addText('GOVERNANCE', { x: 0.75, y: 5.35, w: 3, h: 0.3, fontFace: BODY, fontSize: 12, bold: true, color: GOLD, charSpacing: 2, isTextBox: true, margin: 0 });
  s.addText([
    { text: 'Protected attributes are never used to score. ', options: { bold: true, color: WHITE } },
    { text: 'They are held only for fairness monitoring. PII is anonymized before any AI processing.', options: { color: 'D7D7D7' } }
  ], { x: 0.75, y: 5.68, w: 11.8, h: 0.7, fontFace: BODY, fontSize: 15, isTextBox: true, margin: 0 });
  footer(s, 3);
  s.addNotes('Alternative data models reach AUC between 0.63 and 0.77 for applicants where traditional models fail entirely.');
})();

// ---------------------------------------------------------------------------
// Slide 4 - Synchrony Solution
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: OFF };
  contentHeader(s, 'Synchrony Solution', 'A scalable, explainable model for reliable risk insight');

  const feats = [
    ['Hybrid score', 'Blends traditional, alternative and network signals into one calibrated score.'],
    ['Dynamic weighting', 'Weights reallocate automatically when a data group is missing.'],
    ['Transparent drivers', 'Every decision returns top positive and negative risk drivers.'],
    ['Path to approval', 'Declines include concrete, feasible steps toward approval.']
  ];
  feats.forEach((f, i) => {
    const row = Math.floor(i / 2);
    const col = i % 2;
    const x = 0.5 + col * 4.2;
    const y = 2.45 + row * 1.9;
    card(s, x, y, 3.95, 1.7, WHITE);
    s.addText(f[0], { x: x + 0.3, y: y + 0.22, w: 3.4, h: 0.45, fontFace: HEAD, fontSize: 17, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(f[1], { x: x + 0.3, y: y + 0.7, w: 3.4, h: 0.9, fontFace: BODY, fontSize: 12.5, color: SOFT, isTextBox: true, margin: 0 });
  });

  // Right stat panel
  card(s, 9.0, 2.45, 3.83, 3.9, CHAR);
  s.addText('Business impact', { x: 9.25, y: 2.65, w: 3.3, h: 0.4, fontFace: HEAD, fontSize: 16, bold: true, color: WHITE, isTextBox: true, margin: 0 });
  s.addText('15 to 35%', { x: 9.25, y: 3.15, w: 3.3, h: 0.9, fontFace: HEAD, fontSize: 40, bold: true, color: GOLD, isTextBox: true, margin: 0 });
  s.addText('approval rate uplift with no rise in baseline default rates', { x: 9.25, y: 4.0, w: 3.35, h: 0.9, fontFace: BODY, fontSize: 13, color: 'D7D7D7', isTextBox: true, margin: 0 });
  s.addText('300 to 850', { x: 9.25, y: 5.0, w: 3.3, h: 0.7, fontFace: HEAD, fontSize: 26, bold: true, color: WHITE, isTextBox: true, margin: 0 });
  s.addText('unified Synchrony risk band across products', { x: 9.25, y: 5.6, w: 3.35, h: 0.6, fontFace: BODY, fontSize: 12, color: 'D7D7D7', isTextBox: true, margin: 0 });
  footer(s, 4);
  s.addNotes('Empirical implementations show approval rate uplift of 15 to 35 percent with no rise in baseline default rates.');
})();

// ---------------------------------------------------------------------------
// Slide 5 - System Architecture
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: WHITE };
  contentHeader(s, 'System Architecture', 'End-to-end technology stack');

  const layers = [
    ['Frontend', 'React applicant portal and underwriting dashboard', GOLD, CHAR],
    ['Backend', 'Spring Boot REST API, JWT auth and Role-Based Access Control', CHAR, WHITE],
    ['Data', 'PostgreSQL with pgvector for contextual risk pattern search', '5C5C5C', WHITE],
    ['AI', 'AWS Bedrock orchestrator with a deterministic fallback', GOLD_DK, WHITE]
  ];
  layers.forEach((l, i) => {
    const y = 2.4 + i * 1.02;
    card(s, 0.5, y, 7.6, 0.88, l[2]);
    s.addText(l[0], { x: 0.8, y, w: 2.2, h: 0.88, valign: 'middle', fontFace: HEAD, fontSize: 18, bold: true, color: l[3], isTextBox: true, margin: 0 });
    s.addText(l[1], { x: 2.9, y, w: 5.0, h: 0.88, valign: 'middle', fontFace: BODY, fontSize: 12.5, color: l[3], isTextBox: true, margin: 0 });
    if (i < 3) {
      s.addShape('downArrow', { x: 4.15, y: y + 0.86, w: 0.3, h: 0.2, fill: { color: GOLD_DK }, line: { type: 'none' } });
    }
  });

  // Flow panel on the right
  card(s, 8.35, 2.4, 4.48, 4.04, OFF);
  s.addText('Applicant lifecycle', { x: 8.6, y: 2.6, w: 4.0, h: 0.4, fontFace: HEAD, fontSize: 15, bold: true, color: CHAR, isTextBox: true, margin: 0 });
  const steps = ['Link data', 'Extract features', 'Score and assess fairness', 'Compute recourse', 'Generate rationale', 'Store embedding and audit'];
  steps.forEach((st, i) => {
    const y = 3.1 + i * 0.55;
    s.addShape('ellipse', { x: 8.6, y, w: 0.35, h: 0.35, fill: { color: GOLD } });
    s.addText(String(i + 1), { x: 8.6, y, w: 0.35, h: 0.35, align: 'center', valign: 'middle', fontFace: HEAD, fontSize: 12, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(st, { x: 9.05, y: y - 0.03, w: 3.6, h: 0.4, valign: 'middle', fontFace: BODY, fontSize: 12.5, color: INK, isTextBox: true, margin: 0 });
  });
  footer(s, 5);
  s.addNotes('The Spring Boot gateway validates and routes requests. pgvector retrieves similar historical cases with an HNSW cosine index.');
})();

// ---------------------------------------------------------------------------
// Slide 6 - Explainable AI in Underwriting
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: OFF };
  contentHeader(s, 'Explainable AI in Underwriting', 'Feature attribution, similarity and transparent decisions');

  // Left: attribution bars mock
  card(s, 0.5, 2.45, 6.3, 3.95, WHITE);
  s.addText('Feature attribution', { x: 0.8, y: 2.65, w: 5.5, h: 0.4, fontFace: HEAD, fontSize: 15, bold: true, color: CHAR, isTextBox: true, margin: 0 });
  const bars = [
    ['Distance from fraud clusters', 0.49, true],
    ['Utility payment punctuality', 0.46, true],
    ['Transaction network stability', 0.43, true],
    ['Payment settlement velocity', 0.33, true],
    ['Mobile money inflow ratio', 0.08, true],
    ['Credit utilization health', 0.22, false]
  ];
  const cx = 3.75; // center line x
  bars.forEach((b, i) => {
    const y = 3.2 + i * 0.5;
    s.addText(b[0], { x: 0.8, y: y - 0.02, w: 2.6, h: 0.35, valign: 'middle', fontFace: BODY, fontSize: 10.5, color: SOFT, isTextBox: true, margin: 0, align: 'right' });
    s.addShape('line', { x: cx, y: y - 0.05, w: 0, h: 0.42, line: { color: 'CCCCCC', width: 1 } });
    const len = b[1] * 5.2;
    if (b[2]) {
      s.addShape('roundRect', { x: cx, y: y + 0.03, w: len, h: 0.26, rectRadius: 0.03, fill: { color: GREEN } });
    } else {
      s.addShape('roundRect', { x: cx - len, y: y + 0.03, w: len, h: 0.26, rectRadius: 0.03, fill: { color: RED } });
    }
  });
  s.addText('Additive contributions sum to the score logit', { x: 0.8, y: 6.05, w: 5.8, h: 0.3, fontFace: BODY, fontSize: 10.5, italic: true, color: SOFT, isTextBox: true, margin: 0 });

  // Right: how it works
  const items = [
    ['SHAP by construction', 'The additive scorecard makes each contribution an exact SHAP value.'],
    ['Counterfactual recourse', 'Finds the minimal feasible changes to reach approval, holding immutable features fixed.'],
    ['Plain-language rationale', 'Bedrock turns the numbers into a compliant adverse action explanation.']
  ];
  items.forEach((it, i) => {
    const y = 2.45 + i * 1.36;
    card(s, 7.0, y, 5.83, 1.2, WHITE);
    s.addShape('roundRect', { x: 7.25, y: y + 0.32, w: 0.55, h: 0.55, rectRadius: 0.1, fill: { color: GOLD } });
    s.addText(String(i + 1), { x: 7.25, y: y + 0.32, w: 0.55, h: 0.55, align: 'center', valign: 'middle', fontFace: HEAD, fontSize: 18, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(it[0], { x: 7.95, y: y + 0.2, w: 4.7, h: 0.4, fontFace: HEAD, fontSize: 15, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(it[1], { x: 7.95, y: y + 0.6, w: 4.75, h: 0.55, fontFace: BODY, fontSize: 12, color: SOFT, isTextBox: true, margin: 0 });
  });
  footer(s, 6);
  s.addNotes('Because contributions are additive, they sum back to the score logit. That satisfies local accuracy and consistency without post-hoc guesswork.');
})();

// ---------------------------------------------------------------------------
// Slide 7 - Regulatory Compliance and Fairness
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: WHITE };
  contentHeader(s, 'Regulatory Compliance and Fairness', 'Minimizing bias and protecting privacy');

  const metrics = [
    ['DPR', 'Demographic Parity Ratio'],
    ['EOD', 'Equal Opportunity Difference'],
    ['DIR', 'Disparate Impact Ratio']
  ];
  metrics.forEach((m, i) => {
    const x = 0.5 + i * 4.15;
    card(s, x, 2.45, 3.9, 1.5, OFF);
    s.addText(m[0], { x: x + 0.3, y: 2.6, w: 3.3, h: 0.7, fontFace: HEAD, fontSize: 30, bold: true, color: GOLD_DK, isTextBox: true, margin: 0 });
    s.addText(m[1], { x: x + 0.3, y: 3.35, w: 3.4, h: 0.5, fontFace: BODY, fontSize: 12.5, color: SOFT, isTextBox: true, margin: 0 });
  });

  // four fifths callout
  card(s, 0.5, 4.15, 5.9, 2.25, CHAR);
  s.addText('Four fifths rule', { x: 0.8, y: 4.35, w: 5.3, h: 0.4, fontFace: HEAD, fontSize: 16, bold: true, color: GOLD, isTextBox: true, margin: 0 });
  s.addText('0.80', { x: 0.8, y: 4.75, w: 5.3, h: 1.0, fontFace: HEAD, fontSize: 52, bold: true, color: WHITE, isTextBox: true, margin: 0 });
  s.addText('Any monitored group below this ratio is flagged for threshold recalibration.', { x: 0.8, y: 5.8, w: 5.4, h: 0.5, fontFace: BODY, fontSize: 12.5, color: 'D7D7D7', isTextBox: true, margin: 0 });

  // compliance list
  card(s, 6.6, 4.15, 6.23, 2.25, OFF);
  const list = [
    'Adverse action notices meet ECOA, Regulation B and CFPB Circular 2022-03.',
    'Protected attributes are screened out of scoring.',
    'PII anonymized before LLM processing.',
    'Append-only audit log with SHA-256 payload hashing.'
  ];
  s.addText(list.map((t, i) => ({ text: t, options: { bullet: { code: '2022', indent: 15 }, breakLine: true, paraSpaceAfter: 6 } })),
    { x: 6.85, y: 4.35, w: 5.75, h: 1.9, fontFace: BODY, fontSize: 12.5, color: INK, isTextBox: true, margin: 0, valign: 'top' });
  footer(s, 7);
  s.addNotes('Bias mitigation spans pre-processing, in-processing and post-processing. The monitor flags any group that falls below 0.80 so strategy can recalibrate thresholds.');
})();

// ---------------------------------------------------------------------------
// Slide 8 - Prototype Walkthrough (closing, dark)
// ---------------------------------------------------------------------------
(() => {
  const s = pres.addSlide();
  s.background = { color: CHAR };
  brandMark(s, 0.7, 0.5, true);
  s.addText('PROTOTYPE WALKTHROUGH', { x: 0.7, y: 1.25, w: 12, h: 0.3, fontFace: BODY, fontSize: 12, bold: true, color: GOLD, charSpacing: 2, isTextBox: true, margin: 0 });
  s.addText('Working software, not slides alone', { x: 0.7, y: 1.55, w: 12, h: 0.8, fontFace: HEAD, fontSize: 32, bold: true, color: WHITE, isTextBox: true, margin: 0 });

  const cols = [
    ['Applicant Portal', ['Link alternative data streams', 'Run dynamic underwriting', 'Read decision, drivers and recourse']],
    ['Underwriting Dashboard', ['Risk profiles and model weighting', 'Feature attribution and similar cases', 'Manual override with audit trail']]
  ];
  cols.forEach((c, i) => {
    const x = 0.7 + i * 6.15;
    s.addShape('roundRect', { x, y: 2.7, w: 5.8, h: 2.9, rectRadius: 0.1, fill: { color: CHAR_DK }, line: { color: '4A4A4A', width: 1 } });
    s.addShape('roundRect', { x, y: 2.7, w: 5.8, h: 0.75, rectRadius: 0.1, fill: { color: GOLD } });
    s.addText(c[0], { x: x + 0.3, y: 2.7, w: 5.3, h: 0.75, valign: 'middle', fontFace: HEAD, fontSize: 18, bold: true, color: CHAR, isTextBox: true, margin: 0 });
    s.addText(c[1].map((t, j) => ({ text: t, options: { bullet: { code: '2022', indent: 15 }, breakLine: true, paraSpaceAfter: 10, color: 'E4E4E4' } })),
      { x: x + 0.35, y: 3.65, w: 5.2, h: 1.8, fontFace: BODY, fontSize: 14, isTextBox: true, margin: 0, valign: 'top' });
  });

  s.addShape('roundRect', { x: 0.7, y: 5.85, w: 11.95, h: 0.95, rectRadius: 0.1, fill: { color: GOLD, transparency: 85 }, line: { color: GOLD, width: 1 } });
  s.addText('Every decision is reproducible from the stored features, attribution and audit record.',
    { x: 0.9, y: 5.85, w: 11.6, h: 0.95, valign: 'middle', fontFace: BODY, fontSize: 15, bold: true, color: WHITE, isTextBox: true, margin: 0 });
  s.addNotes('The prototype runs end to end with seeded demo applicants. Every decision is reproducible from the stored features, attribution and audit record.');
})();

// Run from this directory with: npm install pptxgenjs && node generate_deck.js
pres.writeFile({ fileName: 'Synchrony_Financial_Inclusion.pptx' })
  .then((f) => console.log('WROTE', f))
  .catch((e) => { console.error(e); process.exit(1); });
