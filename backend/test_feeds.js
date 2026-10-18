const fetch = require('node-fetch');
const { XMLParser } = require('fast-xml-parser');

const feeds = [
  { name: 'SEBI RSS', url: 'https://www.sebi.gov.in/sebirss.xml' },
  { name: 'RBI RSS', url: 'https://www.rbi.org.in/rssfeed/pressreleases.xml' },
  { name: 'Moneycontrol RSS', url: 'https://www.moneycontrol.com/rss/MCtopnews.xml' },
  { name: 'ET Markets RSS', url: 'https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms' },
  { name: 'Mint RSS', url: 'https://www.livemint.com/rss/news' }
];

async function testFeeds() {
  console.log('=== TESTING REAL LIVE NETWORK FEEDS ===');
  for (const feed of feeds) {
    const start = Date.now();
    try {
      const res = await fetch(feed.url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 MarketPulse/1.0'
        },
        timeout: 10000
      });
      const duration = Date.now() - start;
      const text = await res.text();
      const parser = new XMLParser({ ignoreAttributes: false, attributeNamePrefix: '@_' });
      const xmlObj = parser.parse(text);

      let items = [];
      if (xmlObj.rss && xmlObj.rss.channel && xmlObj.rss.channel.item) {
        items = Array.isArray(xmlObj.rss.channel.item) ? xmlObj.rss.channel.item : [xmlObj.rss.channel.item];
      } else if (xmlObj.feed && xmlObj.feed.entry) {
        items = Array.isArray(xmlObj.feed.entry) ? xmlObj.feed.entry : [xmlObj.feed.entry];
      }

      console.log(`[${feed.name}] HTTP Status: ${res.status} | Duration: ${duration}ms | Payload: ${text.length} bytes | Items: ${items.length}`);
      if (items.length > 0) {
        const first = items[0];
        const title = first.title ? (typeof first.title === 'string' ? first.title : JSON.stringify(first.title)) : 'N/A';
        const pubDate = first.pubDate || first.published || first.updated || 'N/A';
        const link = first.link ? (typeof first.link === 'string' ? first.link : (first.link['@_href'] || JSON.stringify(first.link))) : 'N/A';
        console.log(`   Latest Title : "${title.substring(0, 80)}..."`);
        console.log(`   PubDate      : "${pubDate}"`);
        console.log(`   Link         : "${link}"`);
      }
    } catch (err) {
      console.log(`[${feed.name}] ERROR: ${err.message}`);
    }
  }
}

testFeeds();
