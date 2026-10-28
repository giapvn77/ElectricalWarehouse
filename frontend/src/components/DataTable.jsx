/**
 * columns: [{ key, title, render?(row), align?, width? }]
 * page: PagedResult from the API (optional) -> shows pagination.
 */
export default function DataTable({ columns, rows, loading, error, page, onPageChange, rowKey = 'id', emptyText = 'Không có dữ liệu', onRowClick }) {
  return (
    <div className="table-wrap">
      {error && <div className="alert alert-error">{error}</div>}
      <table className="table">
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key} style={{ width: c.width, textAlign: c.align }}>{c.title}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr><td colSpan={columns.length} className="muted center">Đang tải...</td></tr>
          )}
          {!loading && rows.length === 0 && (
            <tr><td colSpan={columns.length} className="muted center">{emptyText}</td></tr>
          )}
          {!loading && rows.map((row, idx) => (
            <tr key={row[rowKey] ?? idx} onClick={onRowClick ? () => onRowClick(row) : undefined} className={onRowClick ? 'clickable' : ''}>
              {columns.map((c) => (
                <td key={c.key} style={{ textAlign: c.align }}>{c.render ? c.render(row, idx) : row[c.key]}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {page && page.totalPages > 1 && (
        <div className="pagination">
          <span className="muted">Tổng {page.totalElements} dòng</span>
          <button className="btn btn-sm" disabled={page.page === 0} onClick={() => onPageChange(page.page - 1)}>‹ Trước</button>
          <span>Trang {page.page + 1}/{page.totalPages}</span>
          <button className="btn btn-sm" disabled={page.page + 1 >= page.totalPages} onClick={() => onPageChange(page.page + 1)}>Sau ›</button>
        </div>
      )}
    </div>
  );
}
