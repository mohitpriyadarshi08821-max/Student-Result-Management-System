const http = require('http');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const PORT = process.env.PORT || 8080;
const WEB_ROOT = path.join(__dirname);
const ROOT_DIR = path.resolve(__dirname, '..');

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
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.pdf': 'application/pdf'
};

const server = http.createServer((req, res) => {
  // Extract path without query parameters
  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = decodeURIComponent(parsedUrl.pathname);

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
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('403 Forbidden');
    return;
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('404 Not Found: ' + pathname);
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    const total = stats.size;

    // Set CORS and Cross-Origin isolation headers for WebAssembly
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

    // Handle HTTP Range Requests (Required by CheerpJ for partial JAR streaming)
    const range = req.headers.range;
    if (range) {
      const parts = range.replace(/bytes=/, '').split('-');
      const start = parseInt(parts[0], 10);
      const end = parts[1] ? parseInt(parts[1], 10) : total - 1;

      if (start >= total || end >= total || start > end) {
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

      fileStream.pipe(res);
    } else {
      // Full content delivery
      res.writeHead(200, {
        'Content-Length': total,
        'Content-Type': contentType,
        ...commonHeaders
      });

      fs.createReadStream(filePath).pipe(res);
    }
  });
});

server.listen(PORT, '0.0.0.0', () => {
  const url = `http://localhost:${PORT}`;
  console.log('================================================================');
  console.log('  Student Result Management System - Web Preview Server Active');
  console.log(`  Local URL: ${url}`);
  console.log('  CheerpJ & WebAssembly Bytecode Streaming: Enabled');
  console.log('================================================================');

  // Auto-open browser if requested
  if (process.argv.includes('--open')) {
    const openCommand = process.platform === 'win32' ? `start ${url}` :
                        process.platform === 'darwin' ? `open ${url}` : `xdg-open ${url}`;
    exec(openCommand, (error) => {
      if (error) {
        console.log(`Open browser manually at: ${url}`);
      }
    });
  }
});
