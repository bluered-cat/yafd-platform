import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

export default function RestaurantListPage() {
  const [restaurants, setRestaurants] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchRestaurants(); }, []);

  const fetchRestaurants = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/restaurants');
      setRestaurants(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const handleSearch = async () => {
    if (!search.trim()) {
      fetchRestaurants();
      return;
    }
    setLoading(true);
    try {
      const res = await api.get(`/api/restaurants/search?q=${encodeURIComponent(search)}`);
      setRestaurants(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleSearch();
  };

  if (loading) return <div className="loading">Loading restaurants...</div>;

  return (
    <div>
      <h2 style={{ marginBottom: 20 }}>Browse Restaurants</h2>
      <div className="search-bar" style={{ display: 'flex', gap: 8 }}>
        <input
          placeholder="Search by name or cuisine..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button className="btn btn-primary" onClick={handleSearch}>Search</button>
      </div>

      {restaurants.length === 0 ? (
        <div className="empty-state">No restaurants found.</div>
      ) : (
        <div className="restaurant-grid">
          {restaurants.map((r) => (
            <Link key={r.id} to={`/restaurants/${r.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="card restaurant-card">
                <img src={r.imageUrl || 'https://placehold.co/400x200/ffeaea/e74c3c?text=' + encodeURIComponent(r.name)} alt={r.name} />
                <div className="card-body">
                  <h3>{r.name}</h3>
                  <p style={{ fontSize: '0.9rem', color: '#777', marginBottom: 8 }}>{r.description}</p>
                  <span className="cuisine-tag">{r.cuisine}</span>
                  {r.rating > 0 && <span className="rating" style={{ marginLeft: 12 }}>&#9733; {r.rating}</span>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
