const fetch = require('node-fetch');

async function testLiveIndices() {
  console.log('=== TESTING REAL LIVE MARKET DATA ENDPOINTS FOR INDIAN INDICES ===');

  const symbols = [
    { symbol: 'NIFTY 50', yahooSymbol: '^NSEI' },
    { symbol: 'SENSEX', yahooSymbol: '^BSESN' },
    { symbol: 'BANK NIFTY', yahooSymbol: '^NSEBANK' }
  ];

  for (const item of symbols) {
    try {
      const url = `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(item.yahooSymbol)}?interval=1m&range=1d`;
      const res = await fetch(url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        },
        timeout: 8000
      });

      console.log(`[${item.symbol}] HTTP Status: ${res.status}`);
      if (res.ok) {
        const data = await res.json();
        const result = data.chart?.result?.[0];
        if (result) {
          const meta = result.meta;
          const currentPrice = meta.regularMarketPrice;
          const prevClose = meta.chartPreviousClose || meta.previousClose;
          const changeVal = currentPrice - prevClose;
          const changePct = (changeVal / prevClose) * 100;
          const marketTime = meta.regularMarketTime * 1000;

          console.log(`   Provider       : Yahoo Finance (${meta.dataGranularity})`);
          console.log(`   Current Value  : ${currentPrice}`);
          console.log(`   Previous Close : ${prevClose}`);
          console.log(`   Absolute Change: ${changeVal.toFixed(2)}`);
          console.log(`   Percent Change : ${changePct.toFixed(2)}%`);
          console.log(`   Source Timestamp: ${new Date(marketTime).toISOString()} (${marketTime})`);
          console.log(`   Exchange Name  : ${meta.exchangeName}`);
        }
      }
    } catch (err) {
      console.log(`[${item.symbol}] ERROR: ${err.message}`);
    }
  }
}

testLiveIndices();
