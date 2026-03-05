import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api';

export default function ProfilePage() {
  const { currentUser, userProfile, setUserProfile } = useAuth();
  const [form, setForm] = useState({ name: '', phone: '', email: '' });
  const [addresses, setAddresses] = useState([]);
  const [addrForm, setAddrForm] = useState({ label: '', street: '', unitNumber: '', city: '', postalCode: '', isDefault: false });
  const [editingAddr, setEditingAddr] = useState(null);
  const [showAddrForm, setShowAddrForm] = useState(false);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    if (userProfile) {
      setForm({ name: userProfile.name, phone: userProfile.phone || '', email: userProfile.email });
    }
    loadAddresses();
  }, [userProfile]);

  const loadAddresses = async () => {
    if (!currentUser) return;
    try {
      const res = await api.get(`/api/accounts/${currentUser.uid}/addresses`);
      setAddresses(res.data);
    } catch { /* ignore */ }
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    try {
      const res = await api.put(`/api/accounts/${currentUser.uid}`, form);
      setUserProfile(res.data);
      setMsg('Profile updated!');
      setTimeout(() => setMsg(''), 3000);
    } catch (err) {
      setMsg('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleAddrSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingAddr) {
        await api.put(`/api/accounts/${currentUser.uid}/addresses/${editingAddr}`, addrForm);
      } else {
        await api.post(`/api/accounts/${currentUser.uid}/addresses`, addrForm);
      }
      setAddrForm({ label: '', street: '', unitNumber: '', city: '', postalCode: '', isDefault: false });
      setEditingAddr(null);
      setShowAddrForm(false);
      loadAddresses();
    } catch (err) {
      setMsg('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  const deleteAddress = async (id) => {
    try {
      await api.delete(`/api/accounts/${currentUser.uid}/addresses/${id}`);
      loadAddresses();
    } catch { /* ignore */ }
  };

  const startEdit = (addr) => {
    setAddrForm({ label: addr.label, street: addr.street, unitNumber: addr.unitNumber || '', city: addr.city, postalCode: addr.postalCode, isDefault: addr.isDefault });
    setEditingAddr(addr.id);
    setShowAddrForm(true);
  };

  return (
    <div className="profile-page">
      <h2 style={{ marginBottom: 24 }}>My Profile</h2>
      {msg && <div style={{ padding: 12, background: '#eafaf1', borderRadius: 8, marginBottom: 16 }}>{msg}</div>}

      <div className="checkout-section">
        <h3>Personal Information</h3>
        <form onSubmit={handleProfileUpdate}>
          <div className="form-group">
            <label>Name</label>
            <input value={form.name} onChange={(e) => setForm({...form, name: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input value={form.email} onChange={(e) => setForm({...form, email: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Phone</label>
            <input value={form.phone} onChange={(e) => setForm({...form, phone: e.target.value})} />
          </div>
          <button className="btn btn-primary">Save Changes</button>
        </form>
      </div>

      <div className="checkout-section">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h3 style={{ margin: 0 }}>Delivery Addresses</h3>
          <button className="btn btn-outline btn-small" onClick={() => { setShowAddrForm(!showAddrForm); setEditingAddr(null); setAddrForm({ label: '', street: '', unitNumber: '', city: '', postalCode: '', isDefault: false }); }}>
            {showAddrForm ? 'Cancel' : '+ Add'}
          </button>
        </div>

        {showAddrForm && (
          <form onSubmit={handleAddrSubmit} style={{ marginBottom: 20, padding: 16, background: '#fafafa', borderRadius: 8 }}>
            <div className="form-group">
              <label>Label</label>
              <input placeholder="e.g. Home, Work" value={addrForm.label} onChange={(e) => setAddrForm({...addrForm, label: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Street</label>
              <input value={addrForm.street} onChange={(e) => setAddrForm({...addrForm, street: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Unit Number</label>
              <input value={addrForm.unitNumber} onChange={(e) => setAddrForm({...addrForm, unitNumber: e.target.value})} />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              <div className="form-group">
                <label>City</label>
                <input value={addrForm.city} onChange={(e) => setAddrForm({...addrForm, city: e.target.value})} required />
              </div>
              <div className="form-group">
                <label>Postal Code</label>
                <input value={addrForm.postalCode} onChange={(e) => setAddrForm({...addrForm, postalCode: e.target.value})} required />
              </div>
            </div>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
              <input type="checkbox" checked={addrForm.isDefault} onChange={(e) => setAddrForm({...addrForm, isDefault: e.target.checked})} />
              Set as default
            </label>
            <button className="btn btn-primary">{editingAddr ? 'Update' : 'Add'} Address</button>
          </form>
        )}

        <div className="address-list">
          {addresses.map((addr) => (
            <div key={addr.id} className="address-card">
              <div>
                <strong>{addr.label}</strong> {addr.isDefault && <span className="default-badge">Default</span>}
                <div style={{ fontSize: '0.9rem', color: '#666', marginTop: 4 }}>
                  {addr.street}{addr.unitNumber ? `, ${addr.unitNumber}` : ''}, {addr.city} {addr.postalCode}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <button className="btn btn-secondary btn-small" onClick={() => startEdit(addr)}>Edit</button>
                <button className="btn btn-secondary btn-small" onClick={() => deleteAddress(addr.id)}>Delete</button>
              </div>
            </div>
          ))}
          {addresses.length === 0 && <div className="empty-state">No addresses yet.</div>}
        </div>
      </div>
    </div>
  );
}
