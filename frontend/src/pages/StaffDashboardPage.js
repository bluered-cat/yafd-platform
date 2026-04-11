import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api';

// ─── Restaurant Form Modal ───────────────────────────────────────────────────

function RestaurantModal({ restaurant, onClose, onSaved }) {
  const editing = !!restaurant;
  const [form, setForm] = useState({
    name: restaurant?.name || '',
    cuisine: restaurant?.cuisine || '',
    description: restaurant?.description || '',
    imageUrl: restaurant?.imageUrl || '',
    rating: restaurant?.rating ?? '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        name: form.name,
        cuisine: form.cuisine,
        description: form.description,
        imageUrl: form.imageUrl || null,
        rating: form.rating !== '' ? parseFloat(form.rating) : null,
      };
      if (editing) {
        await api.put(`/api/restaurants/${restaurant.id}`, payload);
      } else {
        await api.post('/api/restaurants', payload);
      }
      onSaved();
    } catch {
      setError('Failed to save restaurant. Please try again.');
    }
    setSaving(false);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{editing ? 'Edit Restaurant' : 'Add Restaurant'}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>
        <form onSubmit={handleSubmit}>
          {error && <div className="staff-error">{error}</div>}
          <div className="form-group">
            <label>Name *</label>
            <input name="name" value={form.name} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Cuisine *</label>
            <input name="cuisine" value={form.cuisine} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Description</label>
            <input name="description" value={form.description} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Image URL</label>
            <input name="imageUrl" value={form.imageUrl} onChange={handleChange} placeholder="https://..." />
          </div>
          <div className="form-group">
            <label>Rating (0–5)</label>
            <input
              name="rating" type="number" min="0" max="5" step="0.1"
              value={form.rating} onChange={handleChange}
            />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Restaurants Panel ───────────────────────────────────────────────────────

function RestaurantsPanel() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalTarget, setModalTarget] = useState(undefined); // undefined=closed, null=new, obj=edit
  const [deleteId, setDeleteId] = useState(null);

  useEffect(() => { fetchRestaurants(); }, []);

  const fetchRestaurants = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/restaurants');
      setRestaurants(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this restaurant? This cannot be undone.')) return;
    setDeleteId(id);
    try {
      await api.delete(`/api/restaurants/${id}`);
      setRestaurants((prev) => prev.filter((r) => r.id !== id));
    } catch {
      alert('Failed to delete restaurant.');
    }
    setDeleteId(null);
  };

  return (
    <div className="staff-panel">
      <div className="staff-panel-header">
        <h2>Restaurants</h2>
        <button className="btn btn-primary" onClick={() => setModalTarget(null)}>
          + Add Restaurant
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : restaurants.length === 0 ? (
        <div className="empty-state">No restaurants yet. Add one to get started.</div>
      ) : (
        <div className="staff-table-wrapper">
          <table className="staff-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Cuisine</th>
                <th>Rating</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {restaurants.map((r) => (
                <tr key={r.id}>
                  <td>{r.name}</td>
                  <td><span className="cuisine-tag">{r.cuisine}</span></td>
                  <td>{r.rating > 0 ? `★ ${r.rating}` : '—'}</td>
                  <td>
                    <span className={`status-badge ${r.isActive ? 'active' : 'inactive'}`}>
                      {r.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="table-actions">
                    <button
                      className="btn btn-secondary btn-small"
                      onClick={() => setModalTarget(r)}
                    >
                      Edit
                    </button>
                    <button
                      className="btn btn-danger btn-small"
                      onClick={() => handleDelete(r.id)}
                      disabled={deleteId === r.id}
                    >
                      {deleteId === r.id ? '...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalTarget !== undefined && (
        <RestaurantModal
          restaurant={modalTarget}
          onClose={() => setModalTarget(undefined)}
          onSaved={() => { setModalTarget(undefined); fetchRestaurants(); }}
        />
      )}
    </div>
  );
}

// ─── Menu Form Modal ─────────────────────────────────────────────────────────

function MenuModal({ restaurantId, menu, onClose, onSaved }) {
  const editing = !!menu;
  const [form, setForm] = useState({ name: menu?.name || '', description: menu?.description || '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editing) {
        await api.put(`/api/restaurants/${restaurantId}/menus/${menu.id}`, form);
      } else {
        await api.post(`/api/restaurants/${restaurantId}/menus`, form);
      }
      onSaved();
    } catch {
      setError('Failed to save menu. Please try again.');
    }
    setSaving(false);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{editing ? 'Edit Menu' : 'Add Menu'}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>
        <form onSubmit={handleSubmit}>
          {error && <div className="staff-error">{error}</div>}
          <div className="form-group">
            <label>Name *</label>
            <input name="name" value={form.name} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Description</label>
            <input name="description" value={form.description} onChange={handleChange} />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Menu Item Form Modal ─────────────────────────────────────────────────────

function MenuItemModal({ restaurantId, menuId, item, onClose, onSaved }) {
  const editing = !!item;
  const [form, setForm] = useState({
    name: item?.name || '',
    description: item?.description || '',
    price: item?.price ?? '',
    category: item?.category || '',
    imageUrl: item?.imageUrl || '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        price: form.price !== '' ? parseFloat(form.price) : null,
        imageUrl: form.imageUrl || null,
      };
      if (editing) {
        await api.put(`/api/restaurants/${restaurantId}/menus/${menuId}/items/${item.id}`, payload);
      } else {
        await api.post(`/api/restaurants/${restaurantId}/menus/${menuId}/items`, payload);
      }
      onSaved();
    } catch {
      setError('Failed to save item. Please try again.');
    }
    setSaving(false);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{editing ? 'Edit Item' : 'Add Item'}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>
        <form onSubmit={handleSubmit}>
          {error && <div className="staff-error">{error}</div>}
          <div className="form-group">
            <label>Name *</label>
            <input name="name" value={form.name} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Category</label>
            <input name="category" value={form.category} onChange={handleChange} placeholder="e.g. Mains, Drinks" />
          </div>
          <div className="form-group">
            <label>Price *</label>
            <input name="price" type="number" min="0" step="0.01" value={form.price} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Description</label>
            <input name="description" value={form.description} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Image URL</label>
            <input name="imageUrl" value={form.imageUrl} onChange={handleChange} placeholder="https://..." />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Menu Items Sub-panel ─────────────────────────────────────────────────────

function MenuItemsPanel({ restaurant, menu, onBack }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalTarget, setModalTarget] = useState(undefined);
  const [deleteId, setDeleteId] = useState(null);

  useEffect(() => { fetchItems(); }, []);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/api/restaurants/${restaurant.id}/menus/${menu.id}/items`);
      setItems(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this item?')) return;
    setDeleteId(id);
    try {
      await api.delete(`/api/restaurants/${restaurant.id}/menus/${menu.id}/items/${id}`);
      setItems((prev) => prev.filter((i) => i.id !== id));
    } catch {
      alert('Failed to delete item.');
    }
    setDeleteId(null);
  };

  return (
    <div className="staff-panel">
      <div className="staff-panel-header">
        <div>
          <button className="btn-back" onClick={onBack}>&#8592; Back to Menus</button>
          <h2 style={{ marginTop: 6 }}>
            {menu.name}
            <span className="panel-subtitle"> — {restaurant.name}</span>
          </h2>
        </div>
        <button className="btn btn-primary" onClick={() => setModalTarget(null)}>+ Add Item</button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : items.length === 0 ? (
        <div className="empty-state">No items in this menu yet.</div>
      ) : (
        <div className="staff-table-wrapper">
          <table className="staff-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th>Price</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td>{item.name}</td>
                  <td>{item.category || '—'}</td>
                  <td>${parseFloat(item.price).toFixed(2)}</td>
                  <td className="table-actions">
                    <button className="btn btn-secondary btn-small" onClick={() => setModalTarget(item)}>Edit</button>
                    <button
                      className="btn btn-danger btn-small"
                      onClick={() => handleDelete(item.id)}
                      disabled={deleteId === item.id}
                    >
                      {deleteId === item.id ? '...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalTarget !== undefined && (
        <MenuItemModal
          restaurantId={restaurant.id}
          menuId={menu.id}
          item={modalTarget}
          onClose={() => setModalTarget(undefined)}
          onSaved={() => { setModalTarget(undefined); fetchItems(); }}
        />
      )}
    </div>
  );
}

// ─── Menus Panel ──────────────────────────────────────────────────────────────

function MenusPanel() {
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [menus, setMenus] = useState([]);
  const [loadingRestaurants, setLoadingRestaurants] = useState(true);
  const [loadingMenus, setLoadingMenus] = useState(false);
  const [modalTarget, setModalTarget] = useState(undefined);
  const [deleteId, setDeleteId] = useState(null);
  const [selectedMenu, setSelectedMenu] = useState(null); // drill-down to items

  useEffect(() => {
    const fetchRestaurants = async () => {
      try {
        const res = await api.get('/api/restaurants');
        setRestaurants(res.data);
      } catch { /* ignore */ }
      setLoadingRestaurants(false);
    };
    fetchRestaurants();
  }, []);

  useEffect(() => {
    if (selectedRestaurant) fetchMenus();
    else setMenus([]);
  }, [selectedRestaurant]);

  const fetchMenus = async () => {
    setLoadingMenus(true);
    try {
      const res = await api.get(`/api/restaurants/${selectedRestaurant.id}/menus`);
      setMenus(res.data);
    } catch { /* ignore */ }
    setLoadingMenus(false);
  };

  const handleRestaurantChange = (e) => {
    const r = restaurants.find((r) => r.id === e.target.value) || null;
    setSelectedRestaurant(r);
    setSelectedMenu(null);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this menu?')) return;
    setDeleteId(id);
    try {
      await api.delete(`/api/restaurants/${selectedRestaurant.id}/menus/${id}`);
      setMenus((prev) => prev.filter((m) => m.id !== id));
    } catch {
      alert('Failed to delete menu.');
    }
    setDeleteId(null);
  };

  if (selectedMenu) {
    return (
      <MenuItemsPanel
        restaurant={selectedRestaurant}
        menu={selectedMenu}
        onBack={() => setSelectedMenu(null)}
      />
    );
  }

  return (
    <div className="staff-panel">
      <div className="staff-panel-header">
        <h2>Menus</h2>
        {selectedRestaurant && (
          <button className="btn btn-primary" onClick={() => setModalTarget(null)}>+ Add Menu</button>
        )}
      </div>

      <div className="form-group" style={{ maxWidth: 360, marginBottom: 24 }}>
        <label>Restaurant</label>
        {loadingRestaurants ? (
          <div className="loading" style={{ padding: 8 }}>Loading restaurants...</div>
        ) : (
          <select value={selectedRestaurant?.id || ''} onChange={handleRestaurantChange}>
            <option value="">— Select a restaurant —</option>
            {restaurants.map((r) => (
              <option key={r.id} value={r.id}>{r.name}</option>
            ))}
          </select>
        )}
      </div>

      {!selectedRestaurant ? (
        <div className="empty-state">Select a restaurant to manage its menus.</div>
      ) : loadingMenus ? (
        <div className="loading">Loading menus...</div>
      ) : menus.length === 0 ? (
        <div className="empty-state">No menus for this restaurant yet.</div>
      ) : (
        <div className="staff-table-wrapper">
          <table className="staff-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {menus.map((m) => (
                <tr key={m.id}>
                  <td>{m.name}</td>
                  <td style={{ color: '#888' }}>{m.description || '—'}</td>
                  <td className="table-actions">
                    <button className="btn btn-secondary btn-small" onClick={() => setSelectedMenu(m)}>Items</button>
                    <button className="btn btn-secondary btn-small" onClick={() => setModalTarget(m)}>Edit</button>
                    <button
                      className="btn btn-danger btn-small"
                      onClick={() => handleDelete(m.id)}
                      disabled={deleteId === m.id}
                    >
                      {deleteId === m.id ? '...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalTarget !== undefined && (
        <MenuModal
          restaurantId={selectedRestaurant.id}
          menu={modalTarget}
          onClose={() => setModalTarget(undefined)}
          onSaved={() => { setModalTarget(undefined); fetchMenus(); }}
        />
      )}
    </div>
  );
}

// ─── Voucher Form Modal ───────────────────────────────────────────────────────

const EMPTY_VOUCHER_FORM = {
  code: '',
  description: '',
  discountType: 'PERCENTAGE',
  discountValue: '',
  maxUsage: '',
  minOrderAmount: '',
  validFrom: '',
  validUntil: '',
  active: true,
};

function toLocalDateTimeInput(isoString) {
  if (!isoString) return '';
  return isoString.slice(0, 16); // "YYYY-MM-DDTHH:MM"
}

function toOffsetDateTime(localString) {
  if (!localString) return null;
  return new Date(localString).toISOString();
}

function VoucherModal({ voucher, onClose, onSaved }) {
  const editing = !!voucher;
  const [form, setForm] = useState(
    editing
      ? {
          code: voucher.code,
          description: voucher.description || '',
          discountType: voucher.discountType,
          discountValue: voucher.discountValue,
          maxUsage: voucher.maxUsage ?? '',
          minOrderAmount: voucher.minOrderAmount ?? '',
          validFrom: toLocalDateTimeInput(voucher.validFrom),
          validUntil: toLocalDateTimeInput(voucher.validUntil),
          active: voucher.active,
        }
      : { ...EMPTY_VOUCHER_FORM }
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        description: form.description || null,
        discountType: form.discountType,
        discountValue: form.discountValue !== '' ? parseFloat(form.discountValue) : null,
        maxUsage: form.maxUsage !== '' ? parseInt(form.maxUsage) : null,
        minOrderAmount: form.minOrderAmount !== '' ? parseFloat(form.minOrderAmount) : null,
        validFrom: toOffsetDateTime(form.validFrom),
        validUntil: toOffsetDateTime(form.validUntil),
      };
      if (editing) {
        await api.put(`/api/vouchers/${voucher.id}`, { ...payload, active: form.active });
      } else {
        await api.post('/api/vouchers', { ...payload, code: form.code });
      }
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save voucher. Please try again.');
    }
    setSaving(false);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal modal-wide" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{editing ? 'Edit Voucher' : 'Create Voucher'}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>
        <form onSubmit={handleSubmit}>
          {error && <div className="staff-error">{error}</div>}

          <div className="form-row-2">
            <div className="form-group">
              <label>Code *</label>
              <input
                name="code" value={form.code} onChange={handleChange}
                required disabled={editing}
                style={editing ? { background: '#f5f5f5', color: '#aaa' } : {}}
              />
            </div>
            <div className="form-group">
              <label>Discount Type *</label>
              <select name="discountType" value={form.discountType} onChange={handleChange}>
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED_AMOUNT">Fixed Amount ($)</option>
              </select>
            </div>
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label>Discount Value *</label>
              <input name="discountValue" type="number" min="0" step="0.01" value={form.discountValue} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Min Order Amount</label>
              <input name="minOrderAmount" type="number" min="0" step="0.01" value={form.minOrderAmount} onChange={handleChange} placeholder="No minimum" />
            </div>
          </div>

          <div className="form-group">
            <label>Description</label>
            <input name="description" value={form.description} onChange={handleChange} placeholder="Optional description" />
          </div>

          <div className="form-group">
            <label>Max Usage <span style={{ fontWeight: 400, color: '#aaa' }}>(leave blank for unlimited)</span></label>
            <input name="maxUsage" type="number" min="1" step="1" value={form.maxUsage} onChange={handleChange} placeholder="Unlimited" />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label>Valid From</label>
              <input name="validFrom" type="datetime-local" value={form.validFrom} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>Valid Until</label>
              <input name="validUntil" type="datetime-local" value={form.validUntil} onChange={handleChange} />
            </div>
          </div>

          {editing && (
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
              <input type="checkbox" name="active" checked={form.active} onChange={handleChange} />
              Active
            </label>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Vouchers Panel ───────────────────────────────────────────────────────────

function VouchersPanel() {
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalTarget, setModalTarget] = useState(undefined);
  const [deleteId, setDeleteId] = useState(null);

  useEffect(() => { fetchVouchers(); }, []);

  const fetchVouchers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/vouchers');
      setVouchers(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this voucher? This cannot be undone.')) return;
    setDeleteId(id);
    try {
      await api.delete(`/api/vouchers/${id}`);
      setVouchers((prev) => prev.filter((v) => v.id !== id));
    } catch {
      alert('Failed to delete voucher.');
    }
    setDeleteId(null);
  };

  const formatValidity = (from, until) => {
    const fmt = (dt) => dt ? new Date(dt).toLocaleDateString() : '—';
    return `${fmt(from)} → ${fmt(until)}`;
  };

  const formatDiscount = (v) =>
    v.discountType === 'PERCENTAGE' ? `${v.discountValue}%` : `$${parseFloat(v.discountValue).toFixed(2)}`;

  return (
    <div className="staff-panel">
      <div className="staff-panel-header">
        <h2>Vouchers</h2>
        <button className="btn btn-primary" onClick={() => setModalTarget(null)}>+ Create Voucher</button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : vouchers.length === 0 ? (
        <div className="empty-state">No vouchers yet. Create one to get started.</div>
      ) : (
        <div className="staff-table-wrapper">
          <table className="staff-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Discount</th>
                <th>Min Order</th>
                <th>Usage</th>
                <th>Validity</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {vouchers.map((v) => (
                <tr key={v.id}>
                  <td><span className="voucher-code-badge">{v.code}</span></td>
                  <td>{formatDiscount(v)}</td>
                  <td>{v.minOrderAmount ? `$${parseFloat(v.minOrderAmount).toFixed(2)}` : '—'}</td>
                  <td>{v.currentUsage ?? 0} / {v.maxUsage ?? '∞'}</td>
                  <td style={{ fontSize: '0.85rem', color: '#888' }}>{formatValidity(v.validFrom, v.validUntil)}</td>
                  <td>
                    <span className={`status-badge ${v.active ? 'active' : 'inactive'}`}>
                      {v.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="table-actions">
                    <button className="btn btn-secondary btn-small" onClick={() => setModalTarget(v)}>Edit</button>
                    <button
                      className="btn btn-danger btn-small"
                      onClick={() => handleDelete(v.id)}
                      disabled={deleteId === v.id}
                    >
                      {deleteId === v.id ? '...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalTarget !== undefined && (
        <VoucherModal
          voucher={modalTarget}
          onClose={() => setModalTarget(undefined)}
          onSaved={() => { setModalTarget(undefined); fetchVouchers(); }}
        />
      )}
    </div>
  );
}

// ─── Dashboard Shell ─────────────────────────────────────────────────────────

const NAV_ITEMS = [
  { key: 'restaurants', label: 'Restaurants' },
  { key: 'menus', label: 'Menus' },
  { key: 'vouchers', label: 'Vouchers' },
];

export default function StaffDashboardPage() {
  const { userProfile } = useAuth();
  const navigate = useNavigate();
  const [activeSection, setActiveSection] = useState('restaurants');

  useEffect(() => {
    if (userProfile && userProfile.role !== 'STAFF') {
      navigate('/');
    }
  }, [userProfile, navigate]);

  const renderPanel = () => {
    switch (activeSection) {
      case 'restaurants': return <RestaurantsPanel />;
      case 'menus': return <MenusPanel />;
      case 'vouchers': return <VouchersPanel />;
      default: return null;
    }
  };

  return (
    <div className="staff-layout">
      <aside className="staff-sidebar">
        <div className="staff-sidebar-title">Staff Dashboard</div>
        <nav>
          {NAV_ITEMS.map((item) => (
            <button
              key={item.key}
              className={`staff-nav-item ${activeSection === item.key ? 'active' : ''}`}
              onClick={() => setActiveSection(item.key)}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>
      <div className="staff-main">
        {renderPanel()}
      </div>
    </div>
  );
}
