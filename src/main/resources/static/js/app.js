function getCookie(name) {
  let value = "; " + document.cookie;
  let parts = value.split("; " + name + "=");
  if (parts.length === 2) return parts.pop().split(";").shift();
  return "";
}

async function apiFetch(url, options = {}) {
  const csrfToken = getCookie("XSRF-TOKEN");

  const headers = new Headers(options.headers || {});
  if (csrfToken) {
    headers.append("X-XSRF-TOKEN", csrfToken);
  }

  const newOptions = { ...options, headers };
  return fetch(url, newOptions);
}
