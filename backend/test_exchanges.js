const fetch = require('node-fetch');

async function testExchanges() {
  const exchangeUrls = [
    { name: 'NSE RSS Static', url: 'https://www.nseindia.com/static/rss-feed' },
    { name: 'NSE Corporate Announcements API', url: 'https://www.nseindia.com/api/corporate-announcements?index=equities' },
    { name: 'BSE Corporate Announcements Feed', url: 'https://www.bseindia.com/corporates/ann.html' }
  ];

  for (const item of exchangeUrls) {
    try {
      const res = await fetch(item.url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
          'Accept': 'text/html,application/xhtml+xml,application/xml,application/json'
        },
        timeout: 8000
      });
      console.log(`[${item.name}] Status: ${res.status} | Content-Type: ${res.headers.get('content-type')}`);
    } catch (err) {
      console.log(`[${item.name}] ERROR: ${err.message}`);
    }
  }
}

testExchanges();
