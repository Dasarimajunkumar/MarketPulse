const fetch = require('node-fetch');

async function checkRbiRssPage() {
  const possibleRbiUrls = [
    'https://www.rbi.org.in/Scripts/rss.aspx',
    'https://rbi.org.in/Scripts/BS_PressReleaseDisplay.aspx',
    'https://www.rbi.org.in/rssfeed/pressrelease.xml',
    'https://www.rbi.org.in/rssfeed/notifications.xml',
    'https://www.rbi.org.in/rss/pressrelease.xml',
    'https://www.rbi.org.in/pressreleases_rss.xml',
    'https://rbi.org.in/rssfeed/pressreleases.xml'
  ];

  for (const url of possibleRbiUrls) {
    try {
      const res = await fetch(url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        },
        timeout: 8000
      });
      console.log(`[${res.status}] ${url}`);
      if (res.status === 200 && url.includes('rss.aspx')) {
        const body = await res.text();
        const matches = body.match(/href=["']([^"']+\.xml)["']/gi) || [];
        console.log('  Found XML links in rss.aspx:', matches);
      }
    } catch (err) {
      console.log(`[ERR] ${url}: ${err.message}`);
    }
  }
}

checkRbiRssPage();
