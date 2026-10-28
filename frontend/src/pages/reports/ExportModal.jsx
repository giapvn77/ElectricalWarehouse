import { useState } from 'react';
import { ReportApi } from '../../api/ReportApi';
import Modal from '../../components/Modal';
import { useToast } from '../../hooks/useToast';
import { downloadBlob } from '../../utils/format';

/** Lets the user download a report as Excel or PDF. report: 'inventory' | 'movement' | 'xnt'. */
export default function ExportModal({ open, onClose, report, params }) {
  const toast = useToast();
  const [format, setFormat] = useState('EXCEL');
  const [busy, setBusy] = useState(false);

  const download = async () => {
    setBusy(true);
    try {
      const { blob, fileName } = await ReportApi.exportFile(report, params, format);
      downloadBlob(blob, fileName);
      onClose();
    } catch (e) {
      toast.error(e);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal open={open} title="Xuất báo cáo" onClose={onClose} width={420}>
      <div className="checks">
        <label className="check"><input type="radio" checked={format === 'EXCEL'} onChange={() => setFormat('EXCEL')} /> Excel (.xlsx)</label>
        <label className="check"><input type="radio" checked={format === 'PDF'} onChange={() => setFormat('PDF')} /> PDF</label>
      </div>
      <div className="form-actions">
        <button className="btn" onClick={onClose}>Hủy</button>
        <button className="btn btn-primary" disabled={busy} onClick={download}>{busy ? 'Đang tạo file...' : 'Tải xuống'}</button>
      </div>
    </Modal>
  );
}
