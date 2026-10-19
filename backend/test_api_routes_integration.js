/**
 * Comprehensive Integration Test for MarketPulse Express Backend API Routes.
 * Spawns the server on port 8089 and executes real HTTP requests to verify all endpoints.
 */

const assert = require('assert');
const fetch = require('node-fetch');
const { app } = require('./server');

async function runRouteIntegrationTests() {
  console.log('===================================================================================');
  console.log('            RUNNING REAL HTTP ROUTE INTEGRATION TESTS FOR RENDER');
  console.log('===================================================================================\n');

  const PORT = 8089;
  const server = app.listen(PORT, '127.0.0.1');
  const baseUrl = `http://127.0.0.1:${PORT}`;
  console.log(`[TEST SETUP] Server listening locally on ${baseUrl}\n`);

  try {
    // 1. Test GET / (Base Overview Route)
    console.log('[TEST 1] GET / (Root Service Overview)...');
    const rootRes = await fetch(`${baseUrl}/`);
    assert.strictEqual(rootRes.status, 200, 'Root route / should return HTTP 200!');
    const rootData = await rootRes.json();
    assert.strictEqual(rootData.service, 'marketpulse-backend');
    assert.ok(rootData.endpoints, 'Root response should include endpoints list!');
    console.log('  ✓ GET / returned 200 OK with endpoints overview.');

    // 2. Test GET /health & GET /api/v1/health
    console.log('[TEST 2] GET /health and GET /api/v1/health (Liveness Probes)...');
    const h1 = await fetch(`${baseUrl}/health`);
    assert.strictEqual(h1.status, 200);
    const h1Data = await h1.json();
    assert.strictEqual(h1Data.status, 'UP');

    const h2 = await fetch(`${baseUrl}/api/v1/health`);
    assert.strictEqual(h2.status, 200);
    const h2Data = await h2.json();
    assert.strictEqual(h2Data.status, 'UP');
    console.log('  ✓ GET /health and GET /api/v1/health returned 200 OK ("UP").');

    // 3. Test GET /readiness
    console.log('[TEST 3] GET /readiness (Readiness Probe)...');
    const readRes = await fetch(`${baseUrl}/readiness`);
    assert.strictEqual(readRes.status, 200);
    const readData = await readRes.json();
    assert.ok(readData.status === 'READY' || readData.status === 'NOT_READY');
    console.log(`  ✓ GET /readiness returned ${readRes.status} (${readData.status}).`);

    // 4. Test GET /api/v1/sources
    console.log('[TEST 4] GET /api/v1/sources...');
    const srcRes = await fetch(`${baseUrl}/api/v1/sources`);
    assert.strictEqual(srcRes.status, 200);
    const srcData = await srcRes.json();
    assert.strictEqual(srcData.status, 'success');
    assert.strictEqual(srcData.sources.length, 7);
    console.log(`  ✓ GET /api/v1/sources returned 200 OK (${srcData.sources.length} sources registered).`);

    // 5. Test GET /api/v1/news
    console.log('[TEST 5] GET /api/v1/news...');
    const newsRes = await fetch(`${baseUrl}/api/v1/news`);
    assert.strictEqual(newsRes.status, 200);
    const newsData = await newsRes.json();
    assert.strictEqual(newsData.status, 'success');
    assert.ok(Array.isArray(newsData.data));
    console.log(`  ✓ GET /api/v1/news returned 200 OK (${newsData.total} canonical events).`);

    // 6. Test GET /api/v1/market/indices
    console.log('[TEST 6] GET /api/v1/market/indices...');
    const indRes = await fetch(`${baseUrl}/api/v1/market/indices`);
    assert.strictEqual(indRes.status, 200);
    const indData = await indRes.json();
    assert.strictEqual(indData.status, 'success');
    assert.strictEqual(indData.indices.length, 4);
    console.log(`  ✓ GET /api/v1/market/indices returned 200 OK (4 indices).`);

    // 7. Test GET /api/v1/alerts
    console.log('[TEST 7] GET /api/v1/alerts...');
    const altRes = await fetch(`${baseUrl}/api/v1/alerts`);
    assert.strictEqual(altRes.status, 200);
    const altData = await altRes.json();
    assert.strictEqual(altData.status, 'success');
    console.log(`  ✓ GET /api/v1/alerts returned 200 OK (${altData.totalAlerts} alerts).`);

    // 8. Test POST /api/v1/chat
    console.log('[TEST 8] POST /api/v1/chat...');
    const chatRes = await fetch(`${baseUrl}/api/v1/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt: 'What are the latest official SEBI circulars?' })
    });
    assert.strictEqual(chatRes.status, 200);
    const chatData = await chatRes.json();
    assert.strictEqual(chatData.status, 'success');
    assert.strictEqual(chatData.isGrounded, true);
    console.log('  ✓ POST /api/v1/chat returned 200 OK (Grounded Answer).');

    // 9. Test Unmatched Route (404 Catch-All)
    console.log('[TEST 9] Catch-All 404 Route Handler...');
    const badRes = await fetch(`${baseUrl}/non_existent_route`);
    assert.strictEqual(badRes.status, 404);
    const badData = await badRes.json();
    assert.strictEqual(badData.status, 'error');
    console.log('  ✓ Unmatched route returned clean 404 JSON response.');

    console.log('\n===================================================================================');
    console.log('         ALL 9 REAL HTTP ROUTE INTEGRATION TESTS PASSED SUCCESSFULLY!');
    console.log('===================================================================================');

  } finally {
    server.close();
  }
}

runRouteIntegrationTests().catch(err => {
  console.error('\n[TEST FAILURE]', err);
  process.exit(1);
});
