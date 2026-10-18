const fs = require('fs');
const path = require('path');
const fetch = require('node-fetch');

async function downloadWrapper() {
  const url = 'https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar';
  const targetDir = path.join(__dirname, '..', 'gradle', 'wrapper');
  const targetPath = path.join(targetDir, 'gradle-wrapper.jar');

  console.log('Downloading official gradle-wrapper.jar...');
  try {
    const res = await fetch(url, { timeout: 15000 });
    if (res.ok) {
      const buffer = await res.buffer();
      if (!fs.existsSync(targetDir)) {
        fs.mkdirSync(targetDir, { recursive: true });
      }
      fs.writeFileSync(targetPath, buffer);
      console.log(`Successfully saved gradle-wrapper.jar (${buffer.length} bytes) to ${targetPath}`);
    } else {
      console.log(`Download failed with status ${res.status}`);
    }
  } catch (e) {
    console.log('Download error:', e.message);
  }
}

downloadWrapper();
