import { useEffect, useState } from 'react';
import { ProductApi } from '../api/ProductApi';

/** Search-as-you-type product selector. onSelect receives the full product. */
export default function ProductPicker({ onSelect, placeholder = 'Tìm sản phẩm theo mã, tên...' }) {
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (keyword.trim().length < 2) {
      setResults([]);
      return undefined;
    }
    const timer = setTimeout(() => {
      ProductApi.search({ keyword, size: 10 })
        .then((page) => setResults(page.content.filter((p) => p.active)))
        .catch(() => setResults([]));
    }, 250);
    return () => clearTimeout(timer);
  }, [keyword]);

  const pick = (p) => {
    onSelect(p);
    setKeyword('');
    setOpen(false);
  };

  return (
    <div className="picker">
      <input value={keyword} placeholder={placeholder} onFocus={() => setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
        onChange={(e) => { setKeyword(e.target.value); setOpen(true); }} />
      {open && results.length > 0 && (
        <ul className="picker-list">
          {results.map((p) => (
            <li key={p.id} onMouseDown={() => pick(p)}>
              <b>{p.sku}</b> {p.name} {p.serialManaged && <span className="badge badge-violet">Serial</span>}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
