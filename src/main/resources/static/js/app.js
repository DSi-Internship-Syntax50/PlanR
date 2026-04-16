// Security Context
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

const userRole = document.body.getAttribute('data-user-role') || 'GUEST';
const canEdit = (userRole === 'SUPERADMIN' || userRole === 'COORDINATOR');

// --- PURE GLOBAL HELPERS ---
function getCookie(name) {
  let value = "; " + document.cookie;
  let parts = value.split("; " + name + "=");
  if (parts.length === 2) return parts.pop().split(";").shift();
  return "";
}

async function apiFetch(url, options = {}) {
  const csrfTokenCookie = getCookie("XSRF-TOKEN");
  const headers = new Headers(options.headers || {});
  
  if (csrfTokenCookie) {
    headers.append("X-XSRF-TOKEN", csrfTokenCookie);
  } else if (csrfHeader && csrfToken) {
    headers.append(csrfHeader, csrfToken);
  }

  const newOptions = { ...options, headers };
  return fetch(url, newOptions);
}

