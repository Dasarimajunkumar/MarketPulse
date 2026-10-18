const fetch = require('node-fetch');
const { XMLParser } = require('fast-xml-parser');

async function testBseXml() {
  const urls = [
    'https://www.bseindia.com/data/xml/CorporateAction.xml',
    'https://www.bseindia.com/xml-data/corpfiling/AttachLive/bse_announcements.xml'
  ];

  for (const url of urls) {
    try {
      const res = await fetch(url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        },
        timeout: 8000
      });
      console.log(`[BSE TEST] ${url} | Status: ${res.status}`);
      if (res.ok) {
        const text = await res.text();
        console.log(`  Payload: ${text.length} bytes`);
        const parser = new XMLParser({ ignoreAttributes: false });
        const xmlObj = parser.parse(text);
        console.log('  Keys parsed:', Object.keys(xmlObj));
      }
    } catch (err) {
      console.log(`[BSE TEST ERR] ${url}: ${err.message}`);
    }
  }
}

testBseXml();
