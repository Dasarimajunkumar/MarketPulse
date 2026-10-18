const { spawn } = require('child_process');
const fetch = require('node-fetch');

process.env.PORT = '8086';
const server = spawn('node', ['server.js'], {
  cwd: __dirname,
  env: { ...process.env, PORT: '8086' }
});

async function runHealthTests() {
  await new Promise(r => setTimeout(r, 2500));

  try {
    // 1. Health Liveness Probe
    const healthRes = await fetch('http://localhost:8086/health');
    const healthData = await healthRes.json();
    console.log(`[TEST /health] Status: ${healthRes.status} | Service State: ${healthData.status} | Uptime: ${healthData.uptimeSeconds}s`);

    // 2. Readiness Probe
    const readinessRes = await fetch('http://localhost:8086/readiness');
    const readinessData = await readinessRes.json();
    console.log(`[TEST /readiness] Status: ${readinessRes.status} | Readiness State: ${readinessData.status} | Online Sources: ${readinessData.onlineSources}/${readinessData.totalSources}`);

    // 3. FCM Registration
    const fcmRes = await fetch('http://localhost:8086/api/v1/notifications/register-token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token: 'fcm_test_device_token_xyz987', deviceId: 'pixel8_pro_test' })
    });
    const fcmData = await fcmRes.json();
    console.log(`[TEST /api/v1/notifications/register-token] Status: ${fcmRes.status} | Message: ${fcmData.message}`);

  } catch (e) {
    console.log('Health/FCM Test Error:', e.message);
  } finally {
    server.kill();
    console.log('Health and Readiness Tests Completed.');
  }
}

runHealthTests();
