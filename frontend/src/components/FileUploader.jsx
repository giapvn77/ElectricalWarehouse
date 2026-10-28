import { useRef, useState } from 'react';

export default function FileUploader({ accept = 'image/*', label = 'Tải ảnh lên', onUpload }) {
  const inputRef = useRef(null);
  const [busy, setBusy] = useState(false);

  const handleChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setBusy(true);
    try {
      await onUpload(file);
    } finally {
      setBusy(false);
      e.target.value = '';
    }
  };

  return (
    <>
      <input ref={inputRef} type="file" accept={accept} hidden onChange={handleChange} />
      <button type="button" className="btn" disabled={busy} onClick={() => inputRef.current?.click()}>
        {busy ? 'Đang tải...' : label}
      </button>
    </>
  );
}
