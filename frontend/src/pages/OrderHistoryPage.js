import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api';

export default function OrderHistoryPage() {
  const { currentUser } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get(`/api/orders/user/${currentUser.uid}`);
        setOrders(res.data);
      } catch { /* ignore */ }
      setLoading(false);
    };
    load();
  }, [currentUser]);

  if (loading) return <div className="loading">Loading orders...</div>;

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 20 }}>Order History</h2>
      {orders.length === 0 ? (
        <div className="empty-state">
          <p>No orders yet.</p>
          <Link to="/" className="btn btn-primary" style={{ marginTop: 16, display: 'inline-block' }}>Browse Restaurants</Link>
        </div>
      ) : (
        <div className="order-list">
          {orders.map((order) => (
            <div key={order.id} className="order-card">
              <div className="order-card-header">
                <div>
                  <strong>Order #{order.id}</strong>
                  <div style={{ fontSize: '0.85rem', color: '#999', marginTop: 2 }}>
                    {new Date(order.createdAt).toLocaleString()}
                  </div>
                </div>
                <span className={`order-status ${order.status.toLowerCase()}`}>{order.status}</span>
              </div>
              <div style={{ marginBottom: 12 }}>
                {order.items?.map((item) => (
                  <div key={item.id} style={{ fontSize: '0.9rem', color: '#666' }}>
                    {item.menuItemName} x{item.quantity} — ${item.subtotal?.toFixed(2)}
                  </div>
                ))}
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontWeight: 700, color: '#e74c3c' }}>Total: ${order.totalAmount?.toFixed(2)}</span>
                <Link to={`/orders/${order.id}/confirmation`} className="btn btn-outline btn-small">Details</Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
