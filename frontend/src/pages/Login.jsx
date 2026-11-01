import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api.js';

export default function Login({ onLogin }) {
  const [mode, setMode] = useState('login');
  const [f, setF] = useState({ name: '', email: '', password: '' });
  const [err, setErr] = useState('');
  const nav = useNavigate();
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setErr('');
    try {
      const u = await api('/api/auth/' + (mode === 'login' ? 'login' : 'register'), { method: 'POST', body: f });
      onLogin(u);
      nav(u.role === 'ADMIN' ? '/admin' : '/');
    } catch (x) { setErr(x.message); }
  };

  return (
    <form className="card narrow" onSubmit={submit}>
      <h1>{mode === 'login' ? 'Log in' : 'Create account'}</h1>
      {mode === 'register' && (<><label>Name</label><input value={f.name} onChange={set('name')} required /></>)}
      <label>Email</label><input type="email" value={f.email} onChange={set('email')} required />
      <label>Password</label><input type="password" value={f.password} onChange={set('password')} required />
      {err && <p className="err" role="alert">{err}</p>}
      <button className="btn full">{mode === 'login' ? 'Log in' : 'Sign up'}</button>
      <p className="muted">
        {mode === 'login' ? 'New here? ' : 'Already have an account? '}
        <a href="#" onClick={(e) => { e.preventDefault(); setMode(mode === 'login' ? 'register' : 'login'); setErr(''); }}>
          {mode === 'login' ? 'Create an account' : 'Log in'}
        </a>
      </p>
      <p className="muted">Admin login: admin@rr.com / admin123</p>
    </form>
  );
}
