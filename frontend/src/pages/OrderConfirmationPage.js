import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api';

export default function OrderConfirmationPage() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get(`/api/orders/${orderId}`);
        setOrder(res.data);
      } catch { /* ignore */ }
      setLoading(false);
    };
    load();
  }, [orderId]);

  if (loading) return <div className="loading">Loading order details...</div>;
  if (!order) return <div className="empty-state">Order not found.</div>;

  const isSuccess = order.status === 'CONFIRMED' || order.status === 'PREPARING' || order.status === 'OUT_FOR_DELIVERY' || order.status === 'DELIVERED';

  return (
    <div className="confirmation-page">
      <div className={`status-banner ${isSuccess ? 'success' : 'failed'}`}>
        <h1 style={{ fontSize: '2rem', marginBottom: 8 }}>{isSuccess ? '🎉' : '❌'}</h1>
        <h2>{isSuccess ? 'Order Confirmed!' : 'Order Failed'}</h2>
        <p style={{ marginTop: 8 }}>Order #{order.id}</p>
      </div>

      <div className="order-details">
        <h3 style={{ marginBottom: 16 }}>Order Details</h3>

        <div style={{ marginBottom: 16 }}>
          <div style={{ fontWeight: 600, marginBottom: 4 }}>Status</div>
          <span className={`order-status ${order.status.toLowerCase()}`}>{order.status}</span>
        </div>

        <div style={{ marginBottom: 16 }}>
          <div style={{ fontWeight: 600, marginBottom: 4 }}>Delivery Address</div>
          <div style={{ color: '#666' }}>{order.deliveryAddress}</div>
        </div>

        {order.riderId && (
          <div style={{ marginBottom: 16 }}>
            <div style={{ fontWeight: 600, marginBottom: 4 }}>Delivery Rider</div>
            <div style={{ color: '#666' }}>Rider #{order.riderId} {order.riderName && `- ${order.riderName}`}</div>
          </div>
        )}

        <div style={{ marginBottom: 16 }}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>Items</div>
          {order.items?.map((item) => (
            <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid #f0f0f0' }}>
              <span>{item.menuItemName} x{item.quantity}</span>
              <span>${item.subtotal?.toFixed(2)}</span>
            </div>
          ))}
        </div>

        <div style={{ borderTop: '2px solid #eee', paddingTop: 12 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
            <span>Subtotal</span><span>${order.subtotal?.toFixed(2)}</span>
          </div>
          {order.discountAmount > 0 && (
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, color: '#27ae60' }}>
              <span>Discount ({order.voucherCode})</span><span>-${order.discountAmount?.toFixed(2)}</span>
            </div>
          )}
          <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '1.2rem', color: '#e74c3c', marginTop: 8 }}>
            <span>Total</span><span>${order.totalAmount?.toFixed(2)}</span>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
          <Link to="/orders" className="btn btn-primary">View All Orders</Link>
          <Link to="/" className="btn btn-secondary">Continue Browsing</Link>
        </div>
      </div>
    </div>
  );
}
