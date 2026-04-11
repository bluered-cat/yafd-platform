import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api';

const STATUS_LABELS = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  PREPARING: 'Preparing',
  OUT_FOR_DELIVERY: 'Out for Delivery',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
  FAILED: 'Failed',
};

export default function OrderHistoryPage() {
  const { currentUser } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchOrders = async ({ silent = false } = {}) => {
    if (!silent) setLoading(true);
    else setRefreshing(true);
    try {
      const res = await api.get(`/api/orders/user/${currentUser.uid}`);
      setOrders(res.data);
    } catch { /* ignore */ }
    setLoading(false);
    setRefreshing(false);
  };

  useEffect(() => { fetchOrders(); }, [currentUser]);

  if (loading) return <div className="loading">Loading orders...</div>;

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Order History</h2>
        <button className="btn btn-secondary" onClick={() => fetchOrders({ silent: true })} disabled={refreshing}>
          {refreshing ? 'Refreshing...' : '↻ Refresh'}
        </button>
      </div>
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
                <span className={`order-status ${order.status.toLowerCase()}`}>{STATUS_LABELS[order.status] || order.status}</span>
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
