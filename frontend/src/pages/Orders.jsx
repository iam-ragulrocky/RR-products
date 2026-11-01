import { useEffect, useState } from 'react';
import { api, inr } from '../api.js';

export default function Orders({ user }) {
  const [orders, setOrders] = useState([]);
  const [payId, setPayId] = useState(null);
  const [f, setF] = useState({ method: 'Card', cardNumber: '' });
  const [msg, setMsg] = useState('');

  const load = () => api('/api/orders/my', { token: user.token }).then(setOrders).catch((e) => setMsg(e.message));
  useEffect(() => { load(); }, []);

  const pay = async (id) => {
    setMsg('');
    try {
      await api('/api/orders/' + id + '/pay', { method: 'POST', token: user.token, body: f });
      setPayId(null); setF({ method: 'Card', cardNumber: '' }); setMsg('Payment successful. Order #' + id + ' is confirmed.');
      load();
    } catch (e) { setMsg(e.message); }
  };
  const cancel = async (id) => {
    setMsg('');
    try { await api('/api/orders/' + id + '/cancel', { method: 'POST', token: user.token }); setMsg('Order #' + id + ' cancelled.'); load(); }
    catch (e) { setMsg(e.message); }
  };

  const tag = (s) => <span className={'pill ' + s}>{s === 'PENDING_PAYMENT' ? 'Unpaid' : s === 'PAID' ? 'Paid' : 'Cancelled'}</span>;

  return (
    <>
      <h1>My orders</h1>
      {msg && <p className="note" role="status">{msg}</p>}
      {orders.length === 0 && <p className="muted">No orders yet. Place one from your cart.</p>}
      {orders.map((o) => (
        <div className="card" key={o.id}>
          <div className="row"><b>Order #{o.id}</b><span>{tag(o.status)} <b>{inr(o.total)}</b></span></div>
          {o.items.map((i, k) => (<div className="muted" key={k}>{i.name} × {i.qty} — {inr(i.price * i.qty)}</div>))}
          {o.status === 'PENDING_PAYMENT' && payId !== o.id && (
            <div className="actions">
              <button className="btn small" onClick={() => setPayId(o.id)}>Pay now</button>
              <button className="btn small ghost" onClick={() => cancel(o.id)}>Cancel order</button>
            </div>
          )}
          {payId === o.id && (
            <div className="payform">
              <label>Method</label>
              <select value={f.method} onChange={(e) => setF({ ...f, method: e.target.value })}>
                <option>Card</option><option>UPI</option><option>Net banking</option>
              </select>
              {f.method === 'Card' && (<><label>Card number</label>
                <input inputMode="numeric" placeholder="4111 1111 1111 1111" value={f.cardNumber}
                  onChange={(e) => setF({ ...f, cardNumber: e.target.value })} /></>)}
              <div className="actions">
                <button className="btn small" onClick={() => pay(o.id)}>Pay {inr(o.total)}</button>
                <button className="btn small ghost" onClick={() => setPayId(null)}>Close</button>
              </div>
            </div>
          )}
        </div>
      ))}
    </>
  );
}
