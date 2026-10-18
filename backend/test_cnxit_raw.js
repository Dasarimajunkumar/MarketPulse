const fetch = require('node-fetch');

async function testCnxitRaw() {
  console.log('=== INSPECTING RAW PAYLOAD FOR NIFTY IT (^CNXIT) ===');
  const url = 'https://query1.finance.yahoo.com/v8/finance/chart/%5ECNXIT?interval=1m&range=1d';
  try {
    const res = await fetch(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
      },
      timeout: 8000
    });

    console.log('HTTP Status:', res.status);
    if (res.ok) {
      const data = await res.json();
      const meta = data.chart?.result?.[0]?.meta;
      console.log('CNXIT Meta Payload:', JSON.stringify(meta, null, 2));

      if (meta) {
        const currentVal = meta.regularMarketPrice;
        const prevClose = meta.chartPreviousClose || meta.previousClose;
        const diff = currentVal - prevClose;
        const pct = (diff / prevClose) * 100;

        console.log('--- ARITHMETIC VERIFICATION FROM RAW PAYLOAD ---');
        console.log(`currentValue   : ${currentVal}`);
        console.log(`previousClose  : ${prevClose}`);
        console.log(`diff (exact)   : ${diff}`);
        console.log(`diff (2 decimals): ${diff.toFixed(2)}`);
        console.log(`pct (exact)    : ${pct}%`);
        console.log(`pct (2 decimals): ${pct.toFixed(2)}%`);
      }
    }
  } catch (err) {
    console.log('ERROR:', err.message);
  }
}

testCnxitRaw();
