import { useMemo } from 'react';
import { useDispatch } from 'react-redux';
import { pushToast } from '../store/NotificationStore';
import { getErrorMessage } from '../utils/format';

export function useToast() {
  const dispatch = useDispatch();
  return useMemo(
    () => ({
      success: (message) => dispatch(pushToast('success', message)),
      error: (errorOrMessage) =>
        dispatch(pushToast('error', typeof errorOrMessage === 'string' ? errorOrMessage : getErrorMessage(errorOrMessage))),
    }),
    [dispatch],
  );
}
