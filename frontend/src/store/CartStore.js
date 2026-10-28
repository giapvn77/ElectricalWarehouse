import { createSlice } from '@reduxjs/toolkit';

/**
 * Temporary line items while building a stock-in / stock-out / transfer document,
 * so the list survives navigating to the scanner page and back.
 */
const emptyCart = () => ({ stockIn: [], stockOut: [], transfer: [] });

const cartSlice = createSlice({
  name: 'cart',
  initialState: emptyCart(),
  reducers: {
    addItem: (state, { payload: { cart, item } }) => {
      const existing = state[cart].find((i) => i.productId === item.productId && !item.lotNumber);
      if (existing && !item.serialManaged) existing.quantity += item.quantity || 1;
      else state[cart].push({ quantity: 1, ...item, key: `${item.productId}-${Date.now()}` });
    },
    updateItem: (state, { payload: { cart, key, changes } }) => {
      const item = state[cart].find((i) => i.key === key);
      if (item) Object.assign(item, changes);
    },
    removeItem: (state, { payload: { cart, key } }) => {
      state[cart] = state[cart].filter((i) => i.key !== key);
    },
    setItems: (state, { payload: { cart, items } }) => {
      state[cart] = items;
    },
    clearCart: (state, { payload: { cart } }) => {
      state[cart] = [];
    },
  },
});

export const { addItem, updateItem, removeItem, setItems, clearCart } = cartSlice.actions;
export default cartSlice.reducer;
