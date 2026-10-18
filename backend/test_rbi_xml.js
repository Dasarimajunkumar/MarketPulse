const fetch = require('node-fetch');
const { XMLParser } = require('fast-xml-parser');

async function testRbiXml() {
  const urls = [
    'https://www.rbi.org.in/pressreleases_rss.xml',
    'https://www.rbi.org.in/notifications_rss.xml'
  ];

  for (const url of urls) {
    try {
      const res = await fetch(url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        },
        timeout: 8000
      });
      const text = await res.text();
      const parser = new XMLParser({ ignoreAttributes: false });
      const xmlObj = parser.parse(text);

      let items = [];
      if (xmlObj.rss && xmlObj.rss.channel && xmlObj.rss.channel.item) {
        items = Array.isArray(xmlObj.rss.channel.item) ? xmlObj.rss.channel.item : [xmlObj.rss.channel.item];
      }

      console.log(`[RBI SUCCESS] ${url} | HTTP ${res.status} | Payload: ${text.length} bytes | Items: ${items.length}`);
      if (items.length > 0) {
        console.log(`  First item title: "${items[0].title}"`);
        console.log(`  First item link : "${items[0].link}"`);
        console.log(`  First item date : "${items[0].pubDate || items[0].date || 'N/A'}"`);
      }
    } catch (e) {
      console.log(`[RBI ERR] ${url}: ${e.message}`);
    }
  }
}

testRbiXml();
