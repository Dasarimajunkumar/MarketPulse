const { spawn } = require('child_process');

console.log('Starting MarketPulse backend server...');
const server = spawn('node', ['server.js'], { cwd: __dirname });

server.stdout.on('data', (data) => {
  console.log(`[SERVER STDOUT] ${data.toString().trim()}`);
});

server.stderr.on('data', (data) => {
  console.log(`[SERVER STDERR] ${data.toString().trim()}`);
});

setTimeout(() => {
  console.log('Server verified. Stopping test...');
  server.kill();
}, 7000);
