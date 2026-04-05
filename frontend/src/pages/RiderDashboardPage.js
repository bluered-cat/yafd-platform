import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api';

const ACTIVE_STATUSES = ['CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY'];
const DONE_STATUSES   = ['DELIVERED', 'CANCELLED', 'FAILED'];

const STATUS_LABEL = {
  CONFIRMED:        'Confirmed',
  PREPARING:        'Preparing',
  OUT_FOR_DELIVERY: 'Out for Delivery',
  DELIVERED:        'Delivered',
  CANCELLED:        'Cancelled',
  FAILED:           'Failed',
};

const STATUS_CLASS = {
  CONFIRMED:        'confirmed',
  PREPARING:        'preparing',
  OUT_FOR_DELIVERY: 'out_for_delivery',
  DELIVERED:        'delivered',
  CANCELLED:        'cancelled',
  FAILED:           'failed',
};

export default function RiderDashboardPage() {
  const { userProfile } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(null); // orderId being updated

  useEffect(() => {
    if (userProfile && userProfile.role !== 'RIDER') navigate('/');
  }, [userProfile, navigate]);

  const fetchOrders = useCallback(async () => {
    if (!userProfile?.id) return;
    setLoading(true);
    try {
      const res = await api.get(`/api/orders/rider/${userProfile.id}`);
      setOrders(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  }, [userProfile]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const updateStatus = async (orderId, newStatus) => {
    setUpdating(orderId);
    try {
      await api.put(`/api/orders/${orderId}/status`, { status: newStatus });
      await fetchOrders();
    } catch {
      alert('Failed to update order status.');
    }
    setUpdating(null);
  };

  const activeOrders = orders.filter((o) => ACTIVE_STATUSES.includes(o.status));
  const pastOrders   = orders.filter((o) => DONE_STATUSES.includes(o.status));

  if (loading) return <div className="loading">Loading orders...</div>;

  return (
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 24 }}>My Deliveries</h2>

      {/* ── Active Orders ── */}
      <h3 style={{ marginBottom: 12, color: '#555' }}>Active</h3>
      {activeOrders.length === 0 ? (
        <div className="empty-state" style={{ marginBottom: 32 }}>No active orders assigned to you.</div>
      ) : (
        <div className="order-list" style={{ marginBottom: 32 }}>
          {activeOrders.map((order) => (
            <div key={order.id} className="order-card">
              <div className="order-card-header">
                <div>
                  <strong>Order #{order.id}</strong>
                  <div style={{ fontSize: '0.85rem', color: '#888', marginTop: 2 }}>
                    {new Date(order.createdAt).toLocaleString()}
                  </div>
                </div>
                <span className={`order-status ${STATUS_CLASS[order.status]}`}>
                  {STATUS_LABEL[order.status]}
                </span>
              </div>

              <div style={{ fontSize: '0.9rem', color: '#555', marginBottom: 12 }}>
                <strong>Deliver to:</strong> {order.deliveryAddress}
              </div>

              <div style={{ marginBottom: 12 }}>
                {order.items?.map((item) => (
                  <div key={item.id} style={{ fontSize: '0.9rem', color: '#666' }}>
                    {item.quantity}× {item.menuItemName}
                    <span style={{ color: '#aaa', marginLeft: 6 }}>({item.restaurantName})</span>
                  </div>
                ))}
              </div>

              <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                {order.status !== 'OUT_FOR_DELIVERY' && (
                  <button
                    className="btn btn-outline"
                    disabled={updating === order.id}
                    onClick={() => updateStatus(order.id, 'OUT_FOR_DELIVERY')}
                  >
                    {updating === order.id ? 'Updating...' : 'Mark Out for Delivery'}
                  </button>
                )}
                <button
                  className="btn btn-primary"
                  disabled={updating === order.id}
                  onClick={() => updateStatus(order.id, 'DELIVERED')}
                >
                  {updating === order.id ? 'Updating...' : 'Mark Delivered'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── Past Orders ── */}
      <h3 style={{ marginBottom: 12, color: '#555' }}>Past Deliveries</h3>
      {pastOrders.length === 0 ? (
        <div className="empty-state">No past deliveries yet.</div>
      ) : (
        <div className="order-list">
          {pastOrders.map((order) => (
            <div key={order.id} className="order-card" style={{ opacity: 0.75 }}>
              <div className="order-card-header">
                <div>
                  <strong>Order #{order.id}</strong>
                  <div style={{ fontSize: '0.85rem', color: '#888', marginTop: 2 }}>
                    {new Date(order.createdAt).toLocaleString()}
                  </div>
                </div>
                <span className={`order-status ${STATUS_CLASS[order.status]}`}>
                  {STATUS_LABEL[order.status]}
                </span>
              </div>
              <div style={{ fontSize: '0.9rem', color: '#555' }}>
                {order.deliveryAddress}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
