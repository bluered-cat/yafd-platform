import React, { createContext, useContext, useState } from 'react';

const CartContext = createContext();

export function useCart() {
  return useContext(CartContext);
}

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);
  const [restaurantInfo, setRestaurantInfo] = useState(null);

  const addToCart = (item, restaurant) => {
    // If switching restaurants, clear cart
    if (restaurantInfo && restaurantInfo.id !== restaurant.id) {
      setCartItems([{ ...item, quantity: 1 }]);
      setRestaurantInfo(restaurant);
      return;
    }

    setRestaurantInfo(restaurant);
    setCartItems((prev) => {
      const existing = prev.find((i) => i.id === item.id);
      if (existing) {
        return prev.map((i) =>
          i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i
        );
      }
      return [...prev, { ...item, quantity: 1 }];
    });
  };

  const removeFromCart = (itemId) => {
    setCartItems((prev) => {
      const updated = prev
        .map((i) => (i.id === itemId ? { ...i, quantity: i.quantity - 1 } : i))
        .filter((i) => i.quantity > 0);
      if (updated.length === 0) setRestaurantInfo(null);
      return updated;
    });
  };

  const clearCart = () => {
    setCartItems([]);
    setRestaurantInfo(null);
  };

  const cartTotal = cartItems.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  const value = {
    cartItems,
    restaurantInfo,
    addToCart,
    removeFromCart,
    clearCart,
    cartTotal,
    cartCount,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}
