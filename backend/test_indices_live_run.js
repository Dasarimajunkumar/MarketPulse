const { spawn } = require('child_process');
const fetch = require('node-fetch');

process.env.PORT = '8088';
const server = spawn('node', ['server.js'], {
  cwd: __dirname,
  env: { ...process.env, PORT: '8088' }
});

async function runTest() {
  await new Promise(r => setTimeout(r, 2500));
  const reqStart = Date.now();
  console.log(`[CLIENT REQUEST START] System Time: ${reqStart} (${new Date(reqStart).toISOString()})`);

  try {
    const res = await fetch('http://localhost:8088/api/v1/market/indices');
    const reqEnd = Date.now();
    console.log(`[CLIENT RESPONSE RECV] System Time: ${reqEnd} (${new Date(reqEnd).toISOString()}) | HTTP ${res.status}`);

    const data = await res.json();
    console.log('\n=== REAL RETRIEVAL vs SOURCE TIMESTAMPS ===');
    data.indices.forEach(idx => {
      console.log(`\nIndex: ${idx.symbol}`);
      console.log(`  Source Trade Timestamp (Exchange) : ${idx.sourceTimestamp} -> ${new Date(idx.sourceTimestamp).toISOString()} (${formatIST(idx.sourceTimestamp)})`);
      console.log(`  Server Fetch Completion Timestamp  : ${idx.lastFetchedTimestamp} -> ${new Date(idx.lastFetchedTimestamp).toISOString()} (${formatIST(idx.lastFetchedTimestamp)})`);
      console.log(`  Market Session Status             : ${idx.marketSessionStatus}`);
      console.log(`  Data Freshness Status             : ${idx.dataFreshnessStatus}`);
      console.log(`  Provider Name                     : ${idx.provider}`);
    });
  } catch (err) {
    console.log('Error:', err.message);
  } finally {
    server.kill();
  }
}

function formatIST(timestampMs) {
  const date = new Date(timestampMs);
  const options = { timeZone: 'Asia/Kolkata', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true };
  return date.toLocaleTimeString('en-IN', options) + ' IST';
}

runTest();
