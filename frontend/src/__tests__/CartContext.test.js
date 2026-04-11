import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { CartProvider, useCart } from '../context/CartContext';

const wrapper = ({ children }) => <CartProvider>{children}</CartProvider>;

const mockRestaurantA = { id: 'rest-1', name: 'Warung Makan Selera' };
const mockRestaurantB = { id: 'rest-2', name: 'Dragon Palace' };

const mockItemA = { id: 'item-1', name: 'Nasi Lemak Ayam', price: 7.50 };
const mockItemB = { id: 'item-2', name: 'Rendang Daging', price: 9.00 };

describe('CartContext', () => {

  // -------------------------------------------------------------------------
  // addToCart
  // -------------------------------------------------------------------------

  test('addToCart adds a new item with quantity 1', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    expect(result.current.cartItems).toHaveLength(1);
    expect(result.current.cartItems[0].id).toBe('item-1');
    expect(result.current.cartItems[0].quantity).toBe(1);
  });

  test('addToCart increments quantity when the same item is added again', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    expect(result.current.cartItems).toHaveLength(1);
    expect(result.current.cartItems[0].quantity).toBe(2);
  });

  test('addToCart stores the restaurant info', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    expect(result.current.restaurantInfo).toEqual(mockRestaurantA);
  });

  test('addToCart clears cart and starts fresh when switching to a different restaurant', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    act(() => {
      result.current.addToCart(mockItemB, mockRestaurantB);
    });

    expect(result.current.cartItems).toHaveLength(1);
    expect(result.current.cartItems[0].id).toBe('item-2');
    expect(result.current.restaurantInfo).toEqual(mockRestaurantB);
  });

  // -------------------------------------------------------------------------
  // removeFromCart
  // -------------------------------------------------------------------------

  test('removeFromCart decrements item quantity by 1', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    act(() => {
      result.current.removeFromCart('item-1');
    });

    expect(result.current.cartItems[0].quantity).toBe(1);
  });

  test('removeFromCart removes item entirely when quantity reaches 0', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    act(() => {
      result.current.removeFromCart('item-1');
    });

    expect(result.current.cartItems).toHaveLength(0);
  });

  test('removeFromCart clears restaurantInfo when cart becomes empty', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
    });

    act(() => {
      result.current.removeFromCart('item-1');
    });

    expect(result.current.restaurantInfo).toBeNull();
  });

  // -------------------------------------------------------------------------
  // clearCart
  // -------------------------------------------------------------------------

  test('clearCart empties the cart and clears restaurant info', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA);
      result.current.addToCart(mockItemB, mockRestaurantA);
    });

    act(() => {
      result.current.clearCart();
    });

    expect(result.current.cartItems).toHaveLength(0);
    expect(result.current.restaurantInfo).toBeNull();
  });

  // -------------------------------------------------------------------------
  // cartTotal
  // -------------------------------------------------------------------------

  test('cartTotal calculates the correct total price', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA); // 7.50 x1
      result.current.addToCart(mockItemB, mockRestaurantA); // 9.00 x1
      result.current.addToCart(mockItemB, mockRestaurantA); // 9.00 x2 = 18.00
    });

    // 7.50 + 18.00 = 25.50
    expect(result.current.cartTotal).toBeCloseTo(25.50);
  });

  test('cartTotal is 0 when cart is empty', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    expect(result.current.cartTotal).toBe(0);
  });

  // -------------------------------------------------------------------------
  // cartCount
  // -------------------------------------------------------------------------

  test('cartCount returns the total number of items across all entries', () => {
    const { result } = renderHook(() => useCart(), { wrapper });

    act(() => {
      result.current.addToCart(mockItemA, mockRestaurantA); // qty 1
      result.current.addToCart(mockItemA, mockRestaurantA); // qty 2
      result.current.addToCart(mockItemB, mockRestaurantA); // qty 1
    });

    expect(result.current.cartCount).toBe(3);
  });
});
