const { spawn } = require('child_process');
const fetch = require('node-fetch');

process.env.PORT = '8085';
const server = spawn('node', ['server.js'], {
  cwd: __dirname,
  env: { ...process.env, PORT: '8085' }
});

async function runApiTests() {
  console.log('Waiting 3 seconds for server ingestion...');
  await new Promise(r => setTimeout(r, 3000));

  try {
    // 1. Test GET /api/v1/news
    const newsRes = await fetch('http://localhost:8085/api/v1/news');
    const newsData = await newsRes.json();
    console.log(`[TEST /api/v1/news] Status: ${newsRes.status} | Total Events Returned: ${newsData.total}`);

    if (newsData.data && newsData.data.length > 0) {
      console.log(`   Sample Event: "${newsData.data[0].title.substring(0, 70)}..."`);
      console.log(`   Verification Type: ${newsData.data[0].verificationType}`);
      console.log(`   Publisher: ${newsData.data[0].publisher}`);
    }

    // 2. Test GET /api/v1/sources
    const sourcesRes = await fetch('http://localhost:8085/api/v1/sources');
    const sourcesData = await sourcesRes.json();
    console.log(`[TEST /api/v1/sources] Status: ${sourcesRes.status} | Total Sources Registered: ${sourcesData.sources.length}`);
    sourcesData.sources.forEach(s => {
      console.log(`   - ${s.sourceName} [${s.status}] (${s.pollIntervalMinutes}m interval)`);
    });

    // 3. Test GET /api/v1/market/indices
    const indicesRes = await fetch('http://localhost:8085/api/v1/market/indices');
    const indicesData = await indicesRes.json();
    console.log(`[TEST /api/v1/market/indices] Status: ${indicesRes.status} | Indices Returned: ${indicesData.indices.length}`);
    indicesData.indices.forEach(idx => {
      console.log(`   - ${idx.symbol}: ${idx.currentValue} (${idx.changeValue >= 0 ? '+' : ''}${idx.changePercent}%) [${idx.tradingStatus}]`);
    });

    // 4. Test GET /api/v1/alerts
    const alertsRes = await fetch('http://localhost:8085/api/v1/alerts');
    const alertsData = await alertsRes.json();
    console.log(`[TEST /api/v1/alerts] Status: ${alertsRes.status} | High Impact Alerts: ${alertsData.alerts.length}`);

    // 5. Test POST /api/v1/chat
    const chatRes = await fetch('http://localhost:8085/api/v1/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt: 'What are the latest official SEBI circulars and NSE disclosures?' })
    });
    const chatData = await chatRes.json();
    console.log(`[TEST /api/v1/chat] Status: ${chatRes.status} | Grounded: ${chatData.isGrounded}`);
    console.log(`   Cited Sources: ${JSON.stringify(chatData.citedSources)}`);
    console.log(`   Response text preview: "${chatData.text.substring(0, 120).replace(/\n/g, ' ')}..."`);

  } catch (err) {
    console.log('API Test Error:', err.message);
  } finally {
    server.kill();
    console.log('API Endpoints Test Completed.');
  }
}

runApiTests();
