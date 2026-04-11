import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import api from '../api';

export default function RestaurantDetailPage() {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menus, setMenus] = useState([]);
  const [menuItems, setMenuItems] = useState({}); // menuId -> items[]
  const [loading, setLoading] = useState(true);
  const { addToCart, removeFromCart, cartItems } = useCart();

  useEffect(() => {
    const load = async () => {
      try {
        const [restRes, menusRes] = await Promise.all([
          api.get(`/api/restaurants/${id}`),
          api.get(`/api/restaurants/${id}/menus`),
        ]);
        setRestaurant(restRes.data);
        setMenus([...menusRes.data].sort((a, b) => a.name.localeCompare(b.name)));

        // Fetch items for each menu
        const itemPromises = menusRes.data.map((menu) =>
          api.get(`/api/restaurants/${id}/menus/${menu.id}/items`).then((res) => ({
            menuId: menu.id,
            items: res.data,
          }))
        );
        const itemResults = await Promise.all(itemPromises);
        const itemMap = {};
        itemResults.forEach(({ menuId, items }) => {
          itemMap[menuId] = items;
        });
        setMenuItems(itemMap);
      } catch { /* ignore */ }
      setLoading(false);
    };
    load();
  }, [id]);

  const getCartQty = (itemId) => {
    const cartItem = cartItems.find((i) => i.id === itemId);
    return cartItem ? cartItem.quantity : 0;
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!restaurant) return <div className="empty-state">Restaurant not found.</div>;

  return (
    <div>
      <div className="restaurant-header">
        <h1>{restaurant.name}</h1>
        <p style={{ color: '#777' }}>{restaurant.description}</p>
        <div style={{ marginTop: 8 }}>
          <span className="cuisine-tag">{restaurant.cuisine}</span>
          {restaurant.rating > 0 && <span className="rating" style={{ marginLeft: 12 }}>&#9733; {restaurant.rating}</span>}
        </div>
      </div>

      {menus.map((menu) => (
        <div key={menu.id} className="menu-section">
          <h2>{menu.name}</h2>
          {menu.description && <p style={{ color: '#777', marginBottom: 12 }}>{menu.description}</p>}
          <div className="menu-items-grid">
            {(menuItems[menu.id] || []).map((item) => (
              <div key={item.id} className="card menu-item-card">
                {item.imageUrl && (
                  <img src={item.imageUrl} alt={item.name} style={{ width: '100%', height: 140, objectFit: 'cover' }} />
                )}
                <div className="card-body">
                  <div className="item-info">
                    <div className="item-name">{item.name}</div>
                    <div className="item-desc">{item.description}</div>
                    <div className="item-price">${item.price?.toFixed(2)}</div>
                  </div>
                  <div className="item-actions">
                    {!item.isAvailable ? (
                      <span style={{ fontSize: '0.85rem', color: '#aaa' }}>Unavailable</span>
                    ) : getCartQty(item.id) > 0 ? (
                      <div className="qty-control">
                        <button className="qty-btn" onClick={() => removeFromCart(item.id)}>−</button>
                        <span className="qty-value">{getCartQty(item.id)}</span>
                        <button className="qty-btn" onClick={() => addToCart(item, restaurant)}>+</button>
                      </div>
                    ) : (
                      <button className="btn btn-primary btn-small" onClick={() => addToCart(item, restaurant)}>
                        + Add
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
            {(!menuItems[menu.id] || menuItems[menu.id].length === 0) && (
              <div className="empty-state">No items in this menu yet.</div>
            )}
          </div>
        </div>
      ))}

      {menus.length === 0 && <div className="empty-state">No menus available for this restaurant.</div>}
    </div>
  );
}
