import { useEffect, useId, useRef, useState } from 'react';

/**
 * Works with USB/Bluetooth scanners (they type the code then Enter) and, optionally,
 * the device camera via html5-qrcode (loaded only when the camera is opened).
 */
export default function BarcodeScanner({ onScan, placeholder = 'Quét hoặc nhập mã rồi nhấn Enter', autoFocus = true }) {
  const [code, setCode] = useState('');
  const [cameraOn, setCameraOn] = useState(false);
  const [cameraError, setCameraError] = useState(null);
  const inputRef = useRef(null);
  const regionId = 'scanner-' + useId().replace(/:/g, '');

  const submit = (value) => {
    const v = value.trim();
    if (v) onScan(v);
    setCode('');
    inputRef.current?.focus();
  };

  useEffect(() => {
    if (!cameraOn) return undefined;
    let scanner;
    let stopped = false;
    import('html5-qrcode')
      .then(({ Html5Qrcode }) => {
        if (stopped) return;
        scanner = new Html5Qrcode(regionId);
        return scanner.start({ facingMode: 'environment' }, { fps: 10, qrbox: 240 }, (text) => onScan(text.trim()));
      })
      .catch((e) => setCameraError(String(e?.message || e)));
    return () => {
      stopped = true;
      scanner?.isScanning && scanner.stop().catch(() => {});
    };
  }, [cameraOn, regionId, onScan]);

  return (
    <div className="scanner">
      <div className="row">
        <input
          ref={inputRef}
          autoFocus={autoFocus}
          className="grow"
          value={code}
          placeholder={placeholder}
          onChange={(e) => setCode(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              submit(code);
            }
          }}
        />
        <button type="button" className="btn" onClick={() => submit(code)}>Thêm</button>
        <button type="button" className="btn" onClick={() => { setCameraError(null); setCameraOn((v) => !v); }}>
          {cameraOn ? 'Tắt camera' : '📷 Camera'}
        </button>
      </div>
      {cameraOn && <div id={regionId} className="scanner-camera" />}
      {cameraError && <small className="error">Không mở được camera: {cameraError}</small>}
    </div>
  );
}
