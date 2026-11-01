import { useState } from 'react';
import { Routes, Route, NavLink, Navigate, useNavigate } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Shop from './pages/Shop.jsx';
import Cart from './pages/Cart.jsx';
import Orders from './pages/Orders.jsx';
import Admin from './pages/Admin.jsx';

export default function App() {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('rr_user') || 'null'));
  const [cart, setCart] = useState([]);
  const nav = useNavigate();

  const login = (u) => { localStorage.setItem('rr_user', JSON.stringify(u)); setUser(u); };
  const logout = () => { localStorage.removeItem('rr_user'); setUser(null); setCart([]); nav('/'); };
  const add = (p) => setCart((c) => {
    const f = c.find((x) => x.id === p.id);
    return f ? c.map((x) => (x.id === p.id ? { ...x, qty: x.qty + 1 } : x)) : [...c, { ...p, qty: 1 }];
  });
  const count = cart.reduce((a, c) => a + c.qty, 0);

  return (
    <>
      <header className="top">
        <span className="logo">RR-products</span>
        <nav>
          <NavLink to="/" end>Shop</NavLink>
          {user && <NavLink to="/cart">Cart ({count})</NavLink>}
          {user && <NavLink to="/orders">My orders</NavLink>}
          {user?.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
        </nav>
        {user ? (
          <span className="who">Hi, {user.name} <button className="btn small dark" onClick={logout}>Log out</button></span>
        ) : (
          <NavLink className="btn small dark" to="/login">Log in</NavLink>
        )}
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Shop onAdd={add} user={user} />} />
          <Route path="/login" element={<Login onLogin={login} />} />
          <Route path="/cart" element={user ? <Cart cart={cart} setCart={setCart} user={user} /> : <Navigate to="/login" />} />
          <Route path="/orders" element={user ? <Orders user={user} /> : <Navigate to="/login" />} />
          <Route path="/admin" element={user?.role === 'ADMIN' ? <Admin user={user} /> : <Navigate to="/" />} />
        </Routes>
      </main>
    </>
  );
}
