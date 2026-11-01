import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, inr } from '../api.js';

export default function Shop({ onAdd, user }) {
  const [items, setItems] = useState([]);
  const [q, setQ] = useState('');
  const [err, setErr] = useState('');
  const nav = useNavigate();

  useEffect(() => { api('/api/products').then(setItems).catch((e) => setErr(e.message)); }, []);
  const shown = items.filter((p) => (p.name + ' ' + p.category).toLowerCase().includes(q.toLowerCase()));

  return (
    <>
      <h1>Shop</h1>
      <input className="search" placeholder="Search products" value={q} onChange={(e) => setQ(e.target.value)} />
      {err && <p className="err">Cannot reach the server. Is the gateway running on port 8080?</p>}
      <div className="grid">
        {shown.map((p) => (
          <div className="card" key={p.id}>
            <div className="thumb">{p.emoji || '📦'}</div>
            <b>{p.name}</b>
            <div className="muted">{p.description}</div>
            <div className="muted">{p.stock > 0 ? p.stock + ' in stock' : 'Out of stock'}</div>
            <div className="price">{inr(p.price)}</div>
            <button className="btn full" disabled={p.stock < 1}
              onClick={() => (user ? onAdd(p) : nav('/login'))}>Add to cart</button>
          </div>
        ))}
      </div>
    </>
  );
}
