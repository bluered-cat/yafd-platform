import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import api from '../api';

export default function CheckoutPage() {
  const { currentUser } = useAuth();
  const { cartItems, cartTotal, removeFromCart, addToCart, restaurantInfo, clearCart } = useCart();
  const navigate = useNavigate();

  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [voucherCode, setVoucherCode] = useState('');
  const [voucherResult, setVoucherResult] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [addrRes, pmRes] = await Promise.all([
          api.get(`/api/accounts/${currentUser.uid}/addresses`),
          api.get(`/api/payment-methods/user/${currentUser.uid}`),
        ]);
        setAddresses(addrRes.data);
        setPaymentMethods(pmRes.data);
        const defaultAddr = addrRes.data.find((a) => a.isDefault);
        if (defaultAddr) setSelectedAddress(defaultAddr.id);
        const defaultPm = pmRes.data.find((p) => p.isDefault);
        if (defaultPm) setSelectedPayment(defaultPm.id);
      } catch { /* ignore */ }
    };
    load();
  }, [currentUser]);

  const validateVoucher = async () => {
    if (!voucherCode.trim()) return;
    try {
      const res = await api.post('/api/vouchers/validate', {
        code: voucherCode,
        orderAmount: cartTotal,
      });
      setVoucherResult(res.data);
    } catch (err) {
      setVoucherResult({ valid: false, message: err.response?.data?.message || 'Error validating voucher' });
    }
  };

  const discountAmount = voucherResult?.valid ? voucherResult.discountAmount : 0;
  const grandTotal = Math.max(0, cartTotal - discountAmount);

  const handleSubmit = async () => {
    if (!selectedAddress) { setError('Please select a delivery address'); return; }
    if (!selectedPayment) { setError('Please select a payment method'); return; }
    if (cartItems.length === 0) { setError('Your cart is empty'); return; }

    setSubmitting(true);
    setError('');
    try {
      const res = await api.post('/api/orders', {
        userId: currentUser.uid,
        items: cartItems.map((item) => ({
          menuItemId: item.id,
          quantity: item.quantity,
        })),
        voucherCode: voucherResult?.valid ? voucherCode : null,
        addressId: selectedAddress,
        paymentMethodId: selectedPayment,
      });
      clearCart();
      navigate(`/orders/${res.data.id}/confirmation`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit order');
    }
    setSubmitting(false);
  };

  if (cartItems.length === 0) {
    return (
      <div className="empty-state">
        <h2>Your cart is empty</h2>
        <p style={{ marginTop: 8 }}>Browse restaurants and add items to get started.</p>
      </div>
    );
  }

  return (
    <div>
      <h2 style={{ marginBottom: 20 }}>Checkout</h2>
      {error && <div style={{ padding: 12, background: '#fdf2f2', color: '#e74c3c', borderRadius: 8, marginBottom: 16 }}>{error}</div>}
      <div className="checkout-layout">
        <div>
          {/* Address selection */}
          <div className="checkout-section">
            <h3>Delivery Address</h3>
            <div className="selection-list">
              {addresses.map((addr) => (
                <div
                  key={addr.id}
                  className={`selection-item ${selectedAddress === addr.id ? 'selected' : ''}`}
                  onClick={() => setSelectedAddress(addr.id)}
                >
                  <strong>{addr.label}</strong>
                  <div style={{ fontSize: '0.85rem', color: '#666' }}>
                    {addr.street}{addr.unitNumber ? `, ${addr.unitNumber}` : ''}, {addr.city} {addr.postalCode}
                  </div>
                </div>
              ))}
              {addresses.length === 0 && <p>No addresses. <a href="/profile">Add one</a></p>}
            </div>
          </div>

          {/* Payment method selection */}
          <div className="checkout-section">
            <h3>Payment Method</h3>
            <div className="selection-list">
              {paymentMethods.map((pm) => (
                <div
                  key={pm.id}
                  className={`selection-item ${selectedPayment === pm.id ? 'selected' : ''}`}
                  onClick={() => setSelectedPayment(pm.id)}
                >
                  <strong>{pm.label}</strong>
                  <div style={{ fontSize: '0.85rem', color: '#666' }}>
                    {pm.type} {pm.lastFour ? `ending ${pm.lastFour}` : ''}
                  </div>
                </div>
              ))}
              {paymentMethods.length === 0 && <p>No payment methods saved.</p>}
            </div>
          </div>

          {/* Voucher */}
          <div className="checkout-section">
            <h3>Voucher Code</h3>
            <div className="voucher-input">
              <input
                placeholder="Enter voucher code"
                value={voucherCode}
                onChange={(e) => { setVoucherCode(e.target.value); setVoucherResult(null); }}
              />
              <button className="btn btn-outline" onClick={validateVoucher}>Apply</button>
            </div>
            {voucherResult && (
              <div className={`voucher-msg ${voucherResult.valid ? 'success' : 'error'}`}>
                {voucherResult.valid
                  ? `Discount applied: -$${voucherResult.discountAmount.toFixed(2)}`
                  : voucherResult.message}
              </div>
            )}
          </div>
        </div>

        {/* Cart summary */}
        <div>
          <div className="checkout-section" style={{ position: 'sticky', top: 80 }}>
            <h3>Order Summary</h3>
            {restaurantInfo && <p style={{ fontSize: '0.9rem', color: '#777', marginBottom: 12 }}>From: {restaurantInfo.name}</p>}
            {cartItems.map((item) => (
              <div key={item.id} className="cart-item">
                <div>
                  <div style={{ fontWeight: 600 }}>{item.name}</div>
                  <div style={{ fontSize: '0.85rem', color: '#777' }}>${item.price?.toFixed(2)} x {item.quantity}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontWeight: 600 }}>${(item.price * item.quantity).toFixed(2)}</span>
                  <button className="btn btn-secondary btn-small" onClick={() => removeFromCart(item.id)}>-</button>
                  <button className="btn btn-secondary btn-small" onClick={() => addToCart(item, restaurantInfo)}>+</button>
                </div>
              </div>
            ))}

            <div className="cart-summary-totals">
              <div className="total-row">
                <span>Subtotal</span><span>${cartTotal.toFixed(2)}</span>
              </div>
              {discountAmount > 0 && (
                <div className="total-row" style={{ color: '#27ae60' }}>
                  <span>Voucher Discount</span><span>-${discountAmount.toFixed(2)}</span>
                </div>
              )}
              <div className="total-row grand-total">
                <span>Total</span><span>${grandTotal.toFixed(2)}</span>
              </div>
            </div>

            <button
              className="btn btn-primary"
              style={{ width: '100%', marginTop: 16, padding: '14px 0', fontSize: '1.05rem' }}
              onClick={handleSubmit}
              disabled={submitting}
            >
              {submitting ? 'Placing Order...' : `Place Order - $${grandTotal.toFixed(2)}`}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
