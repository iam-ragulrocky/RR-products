import { useEffect, useState } from 'react';
import { api, inr } from '../api.js';

const empty = { id: null, name: '', description: '', price: '', stock: '', category: '', emoji: '' };

export default function Admin({ user }) {
  const [tab, setTab] = useState('products');
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [f, setF] = useState(empty);
  const [msg, setMsg] = useState('');
  const t = user.token;

  const load = () => {
    api('/api/products').then(setProducts).catch((e) => setMsg(e.message));
    api('/api/orders', { token: t }).then(setOrders).catch((e) => setMsg(e.message));
  };
  useEffect(() => { load(); }, []);

  const save = async (e) => {
    e.preventDefault();
    setMsg('');
    const body = { ...f, price: Number(f.price), stock: Number(f.stock) };
    try {
      await api('/api/products' + (f.id ? '/' + f.id : ''), { method: f.id ? 'PUT' : 'POST', token: t, body });
      setF(empty); load();
    } catch (x) { setMsg(x.message); }
  };
  const del = async (id) => {
    try { await api('/api/products/' + id, { method: 'DELETE', token: t }); load(); } catch (x) { setMsg(x.message); }
  };
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const paid = orders.filter((o) => o.status === 'PAID');
  const unpaid = orders.filter((o) => o.status === 'PENDING_PAYMENT');

  return (
    <>
      <h1>Admin</h1>
      <div className="stats">
        <div className="card">Products<b>{products.length}</b></div>
        <div className="card">Orders<b>{orders.length}</b></div>
        <div className="card">Unpaid<b>{unpaid.length}</b></div>
        <div className="card">Revenue<b>{inr(paid.reduce((a, o) => a + o.total, 0))}</b></div>
      </div>
      <div className="tabs">
        <button className={'btn small ' + (tab === 'products' ? '' : 'ghost')} onClick={() => setTab('products')}>Products</button>
        <button className={'btn small ' + (tab === 'orders' ? '' : 'ghost')} onClick={() => setTab('orders')}>Orders</button>
      </div>
      {msg && <p className="err" role="alert">{msg}</p>}

      {tab === 'products' && (
        <div className="split">
          <div className="card scroll">
            <table>
              <thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th></th></tr></thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id}>
                    <td>{p.emoji} {p.name}</td><td>{p.category}</td><td>{inr(p.price)}</td><td>{p.stock}</td>
                    <td>
                      <button className="btn small ghost" onClick={() => setF({ ...p, price: String(p.price), stock: String(p.stock) })}>Edit</button>{' '}
                      <button className="btn small ghost" onClick={() => del(p.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <form className="card" onSubmit={save}>
            <b>{f.id ? 'Edit product' : 'Add product'}</b>
            <label>Name</label><input value={f.name} onChange={set('name')} required />
            <label>Description</label><input value={f.description} onChange={set('description')} />
            <label>Price (₹)</label><input type="number" min="1" value={f.price} onChange={set('price')} required />
            <label>Stock</label><input type="number" min="0" value={f.stock} onChange={set('stock')} required />
            <label>Category</label><input value={f.category} onChange={set('category')} />
            <label>Emoji</label><input value={f.emoji} onChange={set('emoji')} />
            <button className="btn full">{f.id ? 'Save changes' : 'Add product'}</button>
            {f.id && <button type="button" className="btn full ghost" onClick={() => setF(empty)}>Cancel edit</button>}
          </form>
        </div>
      )}

      {tab === 'orders' && (
        <div className="card scroll">
          <table>
            <thead><tr><th>Order</th><th>Buyer</th><th>Items</th><th>Total</th><th>Status</th></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td>#{o.id}</td><td>{o.userEmail}</td>
                  <td>{o.items.map((i) => i.name + ' × ' + i.qty).join(', ')}</td>
                  <td>{inr(o.total)}</td>
                  <td><span className={'pill ' + o.status}>{o.status === 'PENDING_PAYMENT' ? 'Unpaid' : o.status === 'PAID' ? 'Paid' : 'Cancelled'}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
          {orders.length === 0 && <p className="muted">No orders yet.</p>}
        </div>
      )}
    </>
  );
}
