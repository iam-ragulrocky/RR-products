// All calls go through the API gateway.
const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

export async function api(path, { method = 'GET', body, token } = {}) {
  const res = await fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (!res.ok) {
    const msg = (data && data.message) || (res.status === 401 ? 'Please log in again' : res.status === 403 ? 'Admin access only' : 'Request failed (' + res.status + ')');
    throw new Error(msg);
  }
  return data;
}

export const inr = (n) => '₹' + Number(n).toLocaleString('en-IN');
