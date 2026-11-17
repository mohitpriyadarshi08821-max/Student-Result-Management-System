const http = require('http');
const fs = require('fs');
const path = require('path');
const net = require('net');
const { exec } = require('child_process');

const DEFAULT_PORT = parseInt(process.env.PORT || '8080', 10);
const WEB_ROOT = path.join(__dirname);
const ROOT_DIR = path.resolve(__dirname, '..');
const LOCK_FILE = path.join(WEB_ROOT, '.server.lock');

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.js': 'application/javascript; charset=UTF-8',
  '.mjs': 'application/javascript; charset=UTF-8',
  '.css': 'text/css; charset=UTF-8',
  '.json': 'application/json',
  '.wasm': 'application/wasm',
  '.jar': 'application/java-archive',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.pdf': 'application/pdf',
  '.txt': 'text/plain; charset=UTF-8'
};

let activePort = DEFAULT_PORT;

/**
 * Checks if a specific process ID is currently running.
 */
function isPidRunning(pid) {
  if (!pid || typeof pid !== 'number') return false;
  try {
    process.kill(pid, 0);
    return true;
  } catch (err) {
    return false;
  }
}

/**
 * Checks whether an active server lock file exists and the associated process is alive.
 */
function checkExistingServerLock() {
  if (!fs.existsSync(LOCK_FILE)) {
    return null;
  }
  try {
    const raw = fs.readFileSync(LOCK_FILE, 'utf8');
    const lockData = JSON.parse(raw);
    if (lockData && lockData.pid && isPidRunning(lockData.pid)) {
      return lockData;
    }
    // Stale lock file from a terminated process; clean it up
    try { fs.unlinkSync(LOCK_FILE); } catch (_) {}
    return null;
  } catch (_) {
    try { fs.unlinkSync(LOCK_FILE); } catch (_) {}
    return null;
  }
}

/**
 * Writes the active server PID, port, and URL to the lock file.
 */
function writeServerLock(port) {
  try {
    const data = {
      pid: process.pid,
      port: port,
      url: `http://localhost:${port}`,
      startTime: new Date().toISOString()
    };
    fs.writeFileSync(LOCK_FILE, JSON.stringify(data, null, 2), 'utf8');
  } catch (e) {
    console.warn('[WARN] Could not write server lock file:', e.message);
  }
}

/**
 * Removes the server lock file upon clean shutdown.
 */
function removeServerLock() {
  try {
    if (fs.existsSync(LOCK_FILE)) {
      const raw = fs.readFileSync(LOCK_FILE, 'utf8');
      const lockData = JSON.parse(raw);
      if (lockData.pid === process.pid) {
        fs.unlinkSync(LOCK_FILE);
      }
    }
  } catch (_) {}
}

/**
 * Checks if a port is responding to the SRMS health check endpoint over HTTP.
 */
function checkSrmsHealth(port) {
  return new Promise((resolve) => {
    const req = http.get(
      {
        host: '127.0.0.1',
        port: port,
        path: '/health',
        timeout: 1200
      },
      (res) => {
        let body = '';
        res.setEncoding('utf8');
        res.on('data', (chunk) => { body += chunk; });
        res.on('end', () => {
          try {
            const data = JSON.parse(body);
            if (data && data.app === 'student-result-management-system') {
              resolve({ healthy: true, port, pid: data.pid, version: data.version });
              return;
            }
          } catch (_) {}
          resolve({ healthy: false, port, occupiedByOther: true });
        });
      }
    );

    req.on('error', () => {
      resolve({ healthy: false, port, occupiedByOther: false });
    });

    req.on('timeout', () => {
      req.destroy();
      resolve({ healthy: false, port, occupiedByOther: true });
    });
  });
}

/**
 * Checks whether a port is free to bind on 0.0.0.0.
 */
function isPortAvailable(port) {
  return new Promise((resolve) => {
    const tester = net.createServer();
    tester.once('error', (err) => {
      resolve(false);
    });
    tester.once('listening', () => {
      tester.close(() => resolve(true));
    });
    tester.listen(port, '0.0.0.0');
  });
}

/**
 * Automatically opens the browser on the active server URL.
 */
function openBrowser(url) {
  console.log(`[INFO] Launching default web browser at: ${url}`);
  const openCommand = process.platform === 'win32' ? `start "" "${url}"` :
                      process.platform === 'darwin' ? `open "${url}"` : `xdg-open "${url}"`;
  exec(openCommand, (error) => {
    if (error) {
      console.log(`[NOTICE] Could not automatically open browser. Please navigate manually to: ${url}`);
    }
  });
}

/**
 * Main request handler with intelligent range request support for CheerpJ bytecode streaming.
 */
function createHttpServer() {
  return http.createServer((req, res) => {
    // Extract path without query parameters
    const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    let pathname = decodeURIComponent(parsedUrl.pathname);

    // Cross-Origin and WebAssembly Isolation Headers
    const commonHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
      'Access-Control-Allow-Headers': 'Range, Content-Type, Accept',
      'Access-Control-Expose-Headers': 'Content-Range, Content-Length, Accept-Ranges',
      'Cross-Origin-Opener-Policy': 'same-origin',
      'Cross-Origin-Embedder-Policy': 'require-corp',
      'Accept-Ranges': 'bytes'
    };

    if (req.method === 'OPTIONS') {
      res.writeHead(204, commonHeaders);
      res.end();
      return;
    }

    // Health Check Endpoint
    if (pathname === '/health' || pathname === '/api/health' || pathname === '/status') {
      res.writeHead(200, {
        'Content-Type': 'application/json',
        ...commonHeaders
      });
      res.end(JSON.stringify({
        status: 'ok',
        app: 'student-result-management-system',
        version: '1.0.0',
        pid: process.pid,
        port: activePort,
        uptime: process.uptime(),
        timestamp: new Date().toISOString()
      }));
      return;
    }

    if (pathname === '/' || pathname === '/index.html') {
      pathname = '/index.html';
    } else if (pathname.startsWith('/app/')) {
      pathname = pathname.substring(4);
      if (pathname.endsWith('student-result-management-system-1.0.0.jar')) {
        pathname = '/student-result-management-system-1.0.0.jar';
      }
    }

    // Map requested file: Check web/ directory first, then root directory
    let filePath = path.join(WEB_ROOT, pathname);
    if (!fs.existsSync(filePath)) {
      filePath = path.join(ROOT_DIR, pathname);
    }

    // Security check: ensure path is within workspace
    if (!filePath.startsWith(ROOT_DIR) && !filePath.startsWith(WEB_ROOT)) {
      res.writeHead(403, { 'Content-Type': 'text/plain', ...commonHeaders });
      res.end('403 Forbidden');
      return;
    }

    fs.stat(filePath, (err, stats) => {
      if (err || !stats.isFile()) {
        res.writeHead(404, { 'Content-Type': 'text/plain', ...commonHeaders });
        res.end('404 Not Found: ' + pathname);
        return;
      }

      const ext = path.extname(filePath).toLowerCase();
      const contentType = MIME_TYPES[ext] || 'application/octet-stream';
      const total = stats.size;

      // Handle HTTP HEAD requests
      if (req.method === 'HEAD') {
        res.writeHead(200, {
          'Content-Length': total,
          'Content-Type': contentType,
          ...commonHeaders
        });
        res.end();
        return;
      }

      // Handle HTTP Range Requests (Required by CheerpJ for partial JAR streaming and ZIP central directory reading)
      const range = req.headers.range;
      if (range) {
        let start = 0;
        let end = total - 1;

        const rangeMatch = range.match(/bytes=(\d*)-(\d*)/);
        if (rangeMatch) {
          const rawStart = rangeMatch[1];
          const rawEnd = rangeMatch[2];

          if (rawStart === '' && rawEnd !== '') {
            // Suffix range: bytes=-500 (request last 500 bytes of the file)
            const suffix = parseInt(rawEnd, 10);
            start = Math.max(0, total - suffix);
            end = total - 1;
          } else if (rawStart !== '' && rawEnd === '') {
            // Open-ended range: bytes=1000- (request from byte 1000 to end of file)
            start = parseInt(rawStart, 10);
            end = total - 1;
          } else if (rawStart !== '' && rawEnd !== '') {
            // Closed range: bytes=1000-1999
            start = parseInt(rawStart, 10);
            end = parseInt(rawEnd, 10);
          }
        }

        // Validate range bounds
        if (isNaN(start) || isNaN(end) || start >= total || end >= total || start > end || start < 0) {
          res.writeHead(416, {
            'Content-Range': `bytes */${total}`,
            ...commonHeaders
          });
          res.end();
          return;
        }

        const chunkSize = (end - start) + 1;
        const fileStream = fs.createReadStream(filePath, { start, end });

        res.writeHead(206, {
          'Content-Range': `bytes ${start}-${end}/${total}`,
          'Content-Length': chunkSize,
          'Content-Type': contentType,
          ...commonHeaders
        });

        fileStream.on('error', (streamErr) => {
          console.error('[SERVER-ERROR] ReadStream error:', streamErr.message);
          if (!res.headersSent) {
            res.writeHead(500, commonHeaders);
          }
          res.end();
        });

        fileStream.pipe(res);
      } else {
        // Full file content delivery
        res.writeHead(200, {
          'Content-Length': total,
          'Content-Type': contentType,
          ...commonHeaders
        });

        const fullStream = fs.createReadStream(filePath);
        fullStream.on('error', (streamErr) => {
          console.error('[SERVER-ERROR] ReadStream error:', streamErr.message);
          if (!res.headersSent) {
            res.writeHead(500, commonHeaders);
          }
          res.end();
        });
        fullStream.pipe(res);
      }
    });
  });
}

/**
 * Intelligent startup sequence:
 * 1. Checks lock file and /health to detect existing healthy SRMS server.
 *    If found: reuses it, opens browser (if --open), and exits gracefully with 0.
 * 2. If port is occupied by another process, scans alternative ports (8081-8099).
 * 3. Starts the server on the selected available port, writes lock file, and opens browser.
 */
async function bootstrapServer() {
  const shouldOpenBrowser = process.argv.includes('--open');

  console.log('================================================================');
  console.log('  Student Result Management System - Intelligent Server Engine  ');
  console.log('================================================================');

  // Step 1: Check existing server lock file first (fastest, most reliable across all environments)
  const existingLock = checkExistingServerLock();
  if (existingLock) {
    console.log(`[INFO] Existing SRMS server detected running on port ${existingLock.port} (PID: ${existingLock.pid}).`);
    console.log(`[INFO] Reusing active server instance at ${existingLock.url} (preventing duplicate server).`);
    if (shouldOpenBrowser) {
      openBrowser(existingLock.url);
    }
    console.log('================================================================\n');
    process.exit(0);
    return;
  }

  // Step 2: Probe default port via HTTP health check in case server was started independently
  const defaultHealth = await checkSrmsHealth(DEFAULT_PORT);
  if (defaultHealth.healthy) {
    const existingUrl = `http://localhost:${DEFAULT_PORT}`;
    console.log(`[INFO] Active SRMS server already detected on port ${DEFAULT_PORT} (PID: ${defaultHealth.pid}).`);
    console.log(`[INFO] Reusing existing healthy instance to prevent duplicate servers.`);
    console.log(`[INFO] Server URL: ${existingUrl}`);
    writeServerLock(DEFAULT_PORT);
    if (shouldOpenBrowser) {
      openBrowser(existingUrl);
    }
    console.log('================================================================\n');
    process.exit(0);
    return;
  }

  // Step 3: Check whether default port is free to bind
  let chosenPort = DEFAULT_PORT;
  const isDefaultAvailable = await isPortAvailable(DEFAULT_PORT);

  if (!isDefaultAvailable) {
    console.log(`[WARN] Port ${DEFAULT_PORT} is occupied by an external application or process.`);
    console.log('[INFO] Scanning for an available alternative port (8081 - 8099)...');

    let foundFreePort = false;
    for (let testPort = DEFAULT_PORT + 1; testPort <= DEFAULT_PORT + 20; testPort++) {
      // Check if an SRMS server is already alive on this alternative port
      const altHealth = await checkSrmsHealth(testPort);
      if (altHealth.healthy) {
        const altUrl = `http://localhost:${testPort}`;
        console.log(`[INFO] Active SRMS server found on port ${testPort} (PID: ${altHealth.pid}).`);
        console.log(`[INFO] Reusing existing instance at: ${altUrl}`);
        writeServerLock(testPort);
        if (shouldOpenBrowser) {
          openBrowser(altUrl);
        }
        console.log('================================================================\n');
        process.exit(0);
        return;
      }

      // Check if this alternative port is free to bind
      const free = await isPortAvailable(testPort);
      if (free) {
        chosenPort = testPort;
        foundFreePort = true;
        break;
      }
    }

    if (!foundFreePort) {
      console.error('[ERROR] Could not find any available port in range 8080-8099.');
      console.error('[ERROR] Please free up an available port and try again.');
      process.exit(1);
    }
  }

  activePort = chosenPort;
  const server = createHttpServer();

  server.on('error', (err) => {
    removeServerLock();
    if (err.code === 'EADDRINUSE') {
      console.error(`[ERROR] Port ${activePort} became busy during startup.`);
    } else {
      console.error('[ERROR] Server startup error:', err.message);
    }
    process.exit(1);
  });

  server.listen(activePort, '0.0.0.0', () => {
    writeServerLock(activePort);
    const url = `http://localhost:${activePort}`;
    console.log(`[SUCCESS] Web Preview Server running on port ${activePort}`);
    console.log(`[INFO] Local URL: ${url}`);
    console.log(`[INFO] Health Endpoint: ${url}/health`);
    console.log('[INFO] CheerpJ Bytecode Streaming & HTTP Range Requests: ACTIVE');
    console.log('================================================================\n');

    if (shouldOpenBrowser) {
      openBrowser(url);
    }
  });

  // Graceful shutdown handling
  const cleanExit = () => {
    console.log('\n[INFO] Gracefully shutting down Web Preview Server...');
    removeServerLock();
    server.close(() => process.exit(0));
  };

  process.on('SIGINT', cleanExit);
  process.on('SIGTERM', cleanExit);
  process.on('exit', () => {
    removeServerLock();
  });
}

bootstrapServer().catch((err) => {
  removeServerLock();
  console.error('[FATAL] Unhandled server error:', err);
  process.exit(1);
});
