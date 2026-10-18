const fetch = require('node-fetch');

async function testIndexTimestamps() {
  console.log('=== VERIFYING DYNAMIC RETRIEVAL TIMESTAMPS ===');
  try {
    const res = await fetch('http://localhost:8085/api/v1/market/indices');
    if (!res.ok) {
      console.log('Server not on 8085, fetching directly from provider...');
    } else {
      const data = await res.json();
      console.log('API Index Response Data:');
      data.indices.forEach(idx => {
        console.log(`[${idx.symbol}]`);
        console.log(`  Source Timestamp      : ${idx.sourceTimestamp} (${new Date(idx.sourceTimestamp).toISOString()})`);
        console.log(`  Last Fetched Timestamp: ${idx.lastFetchedTimestamp} (${new Date(idx.lastFetchedTimestamp).toISOString()})`);
        console.log(`  Session Status        : ${idx.marketSessionStatus}`);
        console.log(`  Freshness Status      : ${idx.dataFreshnessStatus}`);
      });
    }
  } catch (err) {
    console.log('Fetch Error:', err.message);
  }
}

testIndexTimestamps();
