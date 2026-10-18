const fetch = require('node-fetch');

async function testNseJson() {
  try {
    const res = await fetch('https://www.nseindia.com/api/corporate-announcements?index=equities', {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'application/json'
      },
      timeout: 8000
    });
    console.log('NSE API HTTP Status:', res.status);
    const data = await res.json();
    console.log('NSE Announcements count:', Array.isArray(data) ? data.length : typeof data);
    if (Array.isArray(data) && data.length > 0) {
      console.log('First announcement:', JSON.stringify(data[0], null, 2));
    }
  } catch (err) {
    console.log('NSE JSON Error:', err.message);
  }
}

testNseJson();
