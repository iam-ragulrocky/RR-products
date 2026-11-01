import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, inr } from '../api.js';

export default function Cart({ cart, setCart, user }) {
  const [err, setErr] = useState('');
  const nav = useNavigate();
  const total = cart.reduce((a, c) => a + c.price * c.qty, 0);
  const change = (id, d) => setCart(cart.map((c) => (c.id === id ? { ...c, qty: Math.max(1, c.qty + d) } : c)));

  const place = async () => {
    setErr('');
    try {
      await api('/api/orders', { method: 'POST', token: user.token, body: { items: cart.map((c) => ({ productId: c.id, qty: c.qty })) } });
      setCart([]);
      nav('/orders');
    } catch (e) { setErr(e.message); }
  };

  return (
    <>
      <h1>Cart</h1>
      {cart.length === 0 ? (
        <p className="muted">Your cart is empty. Add something from the shop.</p>
      ) : (
        <div className="card">
          {cart.map((c) => (
            <div className="row" key={c.id}>
              <span>{c.emoji} {c.name}</span>
              <span>
                <button className="btn small ghost" onClick={() => change(c.id, -1)}>−</button> {c.qty}{' '}
                <button className="btn small ghost" onClick={() => change(c.id, 1)}>+</button>{' '}
                {inr(c.price * c.qty)}{' '}
                <button className="btn small ghost" onClick={() => setCart(cart.filter((x) => x.id !== c.id))}>Remove</button>
              </span>
            </div>
          ))}
          <div className="row"><b>Total</b><b className="price">{inr(total)}</b></div>
          {err && <p className="err" role="alert">{err}</p>}
          <button className="btn full" onClick={place}>Place order</button>
          <p className="muted">You can pay right after, or later from My orders.</p>
        </div>
      )}
    </>
  );
}
