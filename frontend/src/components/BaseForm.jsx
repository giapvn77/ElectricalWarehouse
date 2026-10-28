import { useEffect, useState } from 'react';
import { getErrorMessage, getFieldErrors } from '../utils/format';

/**
 * Declarative form.
 * fields: [{ name, label, type: text|number|email|password|date|textarea|select|checkbox|multiselect,
 *            options?: [{value,label}], required?, placeholder?, disabled?, full? }]
 * onSubmit(values) may throw an API error; field errors from the server are shown inline.
 */
export default function BaseForm({ fields, initialValues = {}, onSubmit, submitLabel = 'Lưu', onCancel, children }) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setValues(initialValues);
    // Reset only when a different record is loaded.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(initialValues)]);

  const set = (name, value) => setValues((v) => ({ ...v, [name]: value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setErrors({});
    setFormError(null);
    try {
      await onSubmit(normalize(fields, values));
    } catch (err) {
      setErrors(getFieldErrors(err));
      setFormError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="form" onSubmit={handleSubmit}>
      {formError && <div className="alert alert-error">{formError}</div>}
      <div className="form-grid">
        {fields.map((f) => (
          <label key={f.name} className={`field ${f.full || f.type === 'textarea' ? 'field-full' : ''} ${f.type === 'checkbox' ? 'field-check' : ''}`}>
            {f.type !== 'checkbox' && <span>{f.label}{f.required && <b className="req">*</b>}</span>}
            <Input field={f} value={values[f.name]} onChange={(v) => set(f.name, v)} />
            {f.type === 'checkbox' && <span>{f.label}</span>}
            {errors[f.name] && <small className="error">{errors[f.name]}</small>}
          </label>
        ))}
      </div>
      {children}
      <div className="form-actions">
        {onCancel && <button type="button" className="btn" onClick={onCancel}>Hủy</button>}
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Đang lưu...' : submitLabel}</button>
      </div>
    </form>
  );
}

function Input({ field, value, onChange }) {
  const common = { required: field.required, disabled: field.disabled, placeholder: field.placeholder };
  switch (field.type) {
    case 'textarea':
      return <textarea rows={3} {...common} value={value ?? ''} onChange={(e) => onChange(e.target.value)} />;
    case 'checkbox':
      return <input type="checkbox" disabled={field.disabled} checked={!!value} onChange={(e) => onChange(e.target.checked)} />;
    case 'select':
      return (
        <select {...common} value={value ?? ''} onChange={(e) => onChange(e.target.value)}>
          <option value="">{field.placeholder || '-- Chọn --'}</option>
          {field.options?.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      );
    case 'multiselect':
      return (
        <div className="checks">
          {field.options?.map((o) => (
            <label key={o.value} className="check">
              <input
                type="checkbox"
                checked={(value || []).includes(o.value)}
                onChange={(e) => onChange(e.target.checked ? [...(value || []), o.value] : (value || []).filter((x) => x !== o.value))}
              />
              {o.label}
            </label>
          ))}
        </div>
      );
    default:
      return <input type={field.type || 'text'} step={field.type === 'number' ? 'any' : undefined} {...common} value={value ?? ''} onChange={(e) => onChange(e.target.value)} />;
  }
}

/** Converts form strings to API types: numbers, null for empty selects. */
function normalize(fields, values) {
  const out = { ...values };
  for (const f of fields) {
    const v = out[f.name];
    if (f.type === 'number') out[f.name] = v === '' || v == null ? null : Number(v);
    else if ((f.type === 'select' || f.type === 'date') && v === '') out[f.name] = null;
    else if (f.type === 'select' && f.numeric && v != null) out[f.name] = Number(v);
  }
  return out;
}
