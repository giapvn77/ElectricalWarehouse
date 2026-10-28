// Browser storage can throw (private mode, blocked cookies), so every access is guarded.
const ACCESS = 'ew.accessToken';
const REFRESH = 'ew.refreshToken';

function read(key) {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeValue(key, value) {
  try {
    if (value) localStorage.setItem(key, value);
    else localStorage.removeItem(key);
  } catch {
    // Storage unavailable: the session simply will not survive a reload.
  }
}

export const tokenStorage = {
  getAccess: () => read(ACCESS),
  getRefresh: () => read(REFRESH),
  save: ({ accessToken, refreshToken }) => {
    writeValue(ACCESS, accessToken);
    writeValue(REFRESH, refreshToken);
  },
  clear: () => {
    writeValue(ACCESS, null);
    writeValue(REFRESH, null);
  },
};
