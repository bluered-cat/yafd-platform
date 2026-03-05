import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signOut } from 'firebase/auth';
import { auth } from '../firebase';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { currentUser } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await signOut(auth);
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">YAFD</Link>
      <div className="navbar-links">
        <Link to="/">Restaurants</Link>
        {currentUser ? (
          <>
            <Link to="/checkout">
              Cart {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
            </Link>
            <Link to="/orders">Orders</Link>
            <Link to="/profile">Profile</Link>
            <button className="btn btn-secondary btn-small" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
