import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { ReportApi } from '../api/ReportApi';

export const fetchUnreadCount = createAsyncThunk('notifications/unread', () => ReportApi.unreadCount());

let toastId = 0;

const notificationSlice = createSlice({
  name: 'notifications',
  initialState: { unreadCount: 0, toasts: [] },
  reducers: {
    pushToast: {
      reducer: (state, action) => {
        state.toasts.push(action.payload);
      },
      prepare: (type, message) => ({ payload: { id: ++toastId, type, message } }),
    },
    dismissToast: (state, action) => {
      state.toasts = state.toasts.filter((t) => t.id !== action.payload);
    },
    setUnreadCount: (state, action) => {
      state.unreadCount = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder.addCase(fetchUnreadCount.fulfilled, (state, action) => {
      state.unreadCount = action.payload;
    });
  },
});

export const { pushToast, dismissToast, setUnreadCount } = notificationSlice.actions;
export default notificationSlice.reducer;
