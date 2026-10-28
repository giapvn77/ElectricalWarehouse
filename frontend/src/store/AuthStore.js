import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { AuthApi } from '../api/AuthApi';
import { tokenStorage } from '../utils/tokenStorage';

export const login = createAsyncThunk('auth/login', async ({ username, password }) => {
  const res = await AuthApi.login(username, password);
  tokenStorage.save(res);
  return res.user;
});

export const loadCurrentUser = createAsyncThunk('auth/me', async () => AuthApi.me());

export const logout = createAsyncThunk('auth/logout', async () => {
  const refresh = tokenStorage.getRefresh();
  tokenStorage.clear();
  if (refresh) {
    try {
      await AuthApi.logout(refresh);
    } catch {
      // Server-side revoke is best effort; local tokens are already gone.
    }
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    // 'idle' until we know whether a stored token is still valid.
    status: tokenStorage.getAccess() ? 'idle' : 'anonymous',
  },
  reducers: {
    sessionExpired: (state) => {
      state.user = null;
      state.status = 'anonymous';
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.fulfilled, (state, action) => {
        state.user = action.payload;
        state.status = 'authenticated';
      })
      .addCase(loadCurrentUser.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(loadCurrentUser.fulfilled, (state, action) => {
        state.user = action.payload;
        state.status = 'authenticated';
      })
      .addCase(loadCurrentUser.rejected, (state) => {
        state.user = null;
        state.status = 'anonymous';
      })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.status = 'anonymous';
      });
  },
});

export const { sessionExpired } = authSlice.actions;
export default authSlice.reducer;

export const selectUser = (state) => state.auth.user;
export const selectAuthStatus = (state) => state.auth.status;

/** ADMIN implicitly has every permission (the backend seeds it that way too). */
export const hasPermission = (user, permission) =>
  !permission || !!user?.permissions?.includes(permission) || !!user?.roles?.includes('ADMIN');
export const hasRole = (user, ...roles) => roles.some((r) => user?.roles?.includes(r));
