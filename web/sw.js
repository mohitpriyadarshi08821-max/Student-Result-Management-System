const CACHE_NAME = 'srms-cache-v1';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './srms_data.json',
  './student-result-management-system-1.0.0.jar',
  './manifest.json',
  'https://cjrtnc.leaningtech.com/5.0/loader.js'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[ServiceWorker] Pre-caching offline application assets...');
      return cache.addAll(ASSETS_TO_CACHE).catch(err => {
        console.warn('[ServiceWorker] Some assets could not be pre-cached, will cache on fetch:', err);
      });
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keyList) => {
      return Promise.all(
        keyList.map((key) => {
          if (key !== CACHE_NAME) {
            console.log('[ServiceWorker] Removing old cache:', key);
            return caches.delete(key);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  // Let range requests or non-GET pass through normally if not cached
  if (event.request.method !== 'GET') {
    return;
  }

  // Handle Range requests for CheerpJ streaming
  const isRange = event.request.headers.has('range');

  event.respondWith(
    caches.match(event.request, { ignoreSearch: true }).then((cachedResponse) => {
      if (cachedResponse && !isRange) {
        return cachedResponse;
      }

      // Try network first, falling back to cache
      return fetch(event.request)
        .then((networkResponse) => {
          if (networkResponse && networkResponse.status === 200) {
            const responseToCache = networkResponse.clone();
            caches.open(CACHE_NAME).then((cache) => {
              cache.put(event.request, responseToCache);
            });
          }
          return networkResponse;
        })
        .catch(() => {
          if (cachedResponse) {
            return cachedResponse;
          }
          if (event.request.mode === 'navigate') {
            return caches.match('./index.html');
          }
        });
    })
  );
});
