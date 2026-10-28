import { configureStore } from '@reduxjs/toolkit';
import auth from './AuthStore';
import cart from './CartStore';
import notifications from './NotificationStore';

export const store = configureStore({
  reducer: { auth, cart, notifications },
});
