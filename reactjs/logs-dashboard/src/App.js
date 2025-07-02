import React, { useEffect, useState } from 'react';
import { fetchDashboardData, fetchLogs } from './opensearch';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip,
  ResponsiveContainer, LineChart, Line
} from 'recharts';
import Modal from 'react-modal';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import './App.css';

Modal.setAppElement('#root');

function App() {
  const [targetStats, setTargetStats] = useState([]);
  const [subjectStats, setSubjectStats] = useState([]);
  const [logs, setLogs] = useState([]);
  const [searchTarget, setSearchTarget] = useState('');
  const [searchSubject, setSearchSubject] = useState('');
  const [searchType, setSearchType] = useState('');
  const [searchAction, setSearchAction] = useState('');
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  const [quickRange, setQuickRange] = useState('');
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [refreshInterval, setRefreshInterval] = useState(10);
  const [loading, setLoading] = useState(false);
  const [osStatus, setOsStatus] = useState('ok');
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedLog, setSelectedLog] = useState(null);
  const [showFilters, setShowFilters] = useState(false);
  const [sortOrder, setSortOrder] = useState('desc');
  const logsPerPage = 10;
  const [logLimit, setLogLimit] = useState(500);
  const [selectedDateForHourStats, setSelectedDateForHourStats] = useState(new Date());

  // Reset filters
  const resetFilters = () => {
    setSearchTarget('');
    setSearchSubject('');
    setSearchType('');
    setSearchAction('');
    setStartDate(null);
    setEndDate(null);
    setQuickRange('');
    setSortOrder('desc');
  };

  // Load data
  const loadData = async () => {
    setLoading(true);
    try {
      const dashboard = await fetchDashboardData();
      const logsData = await fetchLogs(logLimit);
      setTargetStats(dashboard.aggregations.by_target_type.buckets);
      setSubjectStats(dashboard.aggregations.by_subject_type.buckets);
      setLogs(logsData.hits.hits.map(h => h._source));
      setOsStatus('ok');
    } catch (err) {
      console.error("Lỗi kết nối OpenSearch:", err);
      setOsStatus('error');
    }
    setLoading(false);
  };

  useEffect(() => { loadData(); }, []);

  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(() => loadData(), refreshInterval * 1000);
    return () => clearInterval(interval);
  }, [autoRefresh, refreshInterval]);

  // Quick range handler
  const handleQuickRange = (value) => {
    const now = new Date();
    setQuickRange(value);
    if (value === 'today') {
      setStartDate(new Date(now.setHours(0, 0, 0, 0)));
      setEndDate(new Date());
    } else if (value === '7days') {
      setStartDate(new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000));
      setEndDate(new Date());
    } else if (value === '30days') {
      setStartDate(new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000));
      setEndDate(new Date());
    } else {
      setStartDate(null);
      setEndDate(null);
    }
  };

  // Unique values
  const getUniqueValues = (field) => [...new Set(logs.map(log => log[field]).filter(Boolean))];
  const getUniqueValuesFromData = (field) => [...new Set(logs.map(log => log.data?.[field]).filter(Boolean))];

  // Filtered logs
  const filteredLogs = logs.filter(log => {
    const logTime = new Date(Number(log.createdAt));
    return (
      (!searchTarget || log.targetType === searchTarget) &&
      (!searchSubject || log.subjectType === searchSubject) &&
      (!searchType || log.type === searchType) &&
      (!searchAction || log.data?.action === searchAction) &&
      (!startDate || logTime >= startDate) &&
      (!endDate || logTime <= endDate)
    );
  }).sort((a, b) => {
    return sortOrder === 'asc'
      ? new Date(Number(a.createdAt)) - new Date(Number(b.createdAt))
      : new Date(Number(b.createdAt)) - new Date(Number(a.createdAt));
  });

  // Pagination
  const indexOfLastLog = currentPage * logsPerPage;
  const indexOfFirstLog = indexOfLastLog - logsPerPage;
  const currentLogs = filteredLogs.slice(indexOfFirstLog, indexOfLastLog);
  const totalPages = Math.ceil(filteredLogs.length / logsPerPage);

  // Logs by hour (for selected date)
  const logsByHour = logs.reduce((acc, log) => {
    const dateObj = new Date(Number(log.createdAt));
    if (dateObj.toDateString() !== selectedDateForHourStats.toDateString()) return acc;
    const hour = dateObj.getHours().toString().padStart(2, '0') + ':00';
    acc[hour] = (acc[hour] || 0) + 1;
    return acc;
  }, {});
  const logHourlyStats = Object.entries(logsByHour)
    .map(([hour, count]) => ({ hour, count }))
    .sort((a, b) => a.hour.localeCompare(b.hour));

  // Logs by date
  const logsByDate = logs.reduce((acc, log) => {
    const date = new Date(Number(log.createdAt)).toLocaleDateString();
    acc[date] = (acc[date] || 0) + 1;
    return acc;
  }, {});
  const logTrends = Object.entries(logsByDate)
    .map(([date, count]) => ({ date, count }))
    .sort((a, b) => new Date(a.date) - new Date(b.date));

  // Export CSV
  const exportCSV = () => {
    const rows = [['createdAt', 'id', 'targetType', 'targetId', 'subjectType', 'subjectId', 'type', 'action', 'correlationId']];
    filteredLogs.forEach(log => {
      rows.push([
        new Date(Number(log.createdAt)).toLocaleString(),
        log.id,
        log.targetType,
        log.targetId,
        log.subjectType,
        log.subjectId,
        log.type,
        log.data?.action,
        log.correlationId
      ]);
    });
    const csvContent = "data:text/csv;charset=utf-8," + rows.map(e => e.join(",")).join("\n");
    const link = document.createElement("a");
    link.href = csvContent;
    link.download = "logs.csv";
    link.click();
  };

  // Export JSON
  const exportJSON = () => {
    const jsonContent = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(filteredLogs, null, 2));
    const link = document.createElement("a");
    link.href = jsonContent;
    link.download = "logs.json";
    link.click();
  };

  return (
    <div className="App">
      <h1>📊 Logs Dashboard</h1>
      {osStatus === 'error' && <p style={{ color: 'red' }}>⚠️ Không thể kết nối đến OpenSearch!</p>}

      {/* Filters */}
      <div className="filters-panel">
        <button onClick={() => setShowFilters(!showFilters)} className="filter-toggle-btn">
          {showFilters ? '🔽 Ẩn bộ lọc' : '🔍 Hiện bộ lọc'}
        </button>

        {showFilters && (
          <div className="filter-group">
            <div className="filter-row">
              <select value={searchTarget} onChange={e => setSearchTarget(e.target.value)}>
                <option value="">🎯 Target Type</option>
                {getUniqueValues('targetType').map(v => <option key={v} value={v}>{v}</option>)}
              </select>
              <select value={searchSubject} onChange={e => setSearchSubject(e.target.value)}>
                <option value="">👤 Subject Type</option>
                {getUniqueValues('subjectType').map(v => <option key={v} value={v}>{v}</option>)}
              </select>
              <select value={searchType} onChange={e => setSearchType(e.target.value)}>
                <option value="">⚡ Type</option>
                {getUniqueValues('type').map(v => <option key={v} value={v}>{v}</option>)}
              </select>
              <select value={searchAction} onChange={e => setSearchAction(e.target.value)}>
                <option value="">📝 Action</option>
                {getUniqueValuesFromData('action').map(v => <option key={v} value={v}>{v}</option>)}
              </select>
            </div>

            <div className="filter-row">
              <select value={quickRange} onChange={e => handleQuickRange(e.target.value)}>
                <option value="">⏱ Nhanh</option>
                <option value="today">📅 Hôm nay</option>
                <option value="7days">🗓 7 ngày qua</option>
                <option value="30days">📆 30 ngày qua</option>
              </select>
              <DatePicker selected={startDate} onChange={setStartDate} placeholderText="Từ ngày" />
              <DatePicker selected={endDate} onChange={setEndDate} placeholderText="Đến ngày" />
              <select value={sortOrder} onChange={e => setSortOrder(e.target.value)}>
                <option value="desc">↓ Mới nhất</option>
                <option value="asc">↑ Cũ nhất</option>
              </select>
              <button onClick={resetFilters}>🔁 Reset</button>
            </div>
          </div>
        )}
      </div>

      {/* Auto refresh */}
      <div>
        <label>
          <input
            type="checkbox"
            checked={autoRefresh}
            onChange={e => setAutoRefresh(e.target.checked)}
          /> Auto Refresh
        </label>
        <input
          type="number"
          value={refreshInterval}
          onChange={e => setRefreshInterval(Number(e.target.value))}
          disabled={!autoRefresh}
          min="1"
        /> giây
      </div>

      {/* Charts */}
      <div className="charts-container">
        <div className="chart-card">
          <h3>🕒 Logs theo giờ</h3>
          <DatePicker
            selected={selectedDateForHourStats}
            onChange={setSelectedDateForHourStats}
            dateFormat="yyyy-MM-dd"
          />
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={logHourlyStats}>
              <XAxis dataKey="hour" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="count" fill="#f08a24" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <LineChartCard title="📆 Logs theo ngày" data={logTrends} dataKeyX="date" dataKeyY="count" />
        <BarChartCard title="🎯 Target Type" data={targetStats} dataKeyX="key" dataKeyY="doc_count" color="#82ca9d" />
        <BarChartCard title="👤 Subject Type" data={subjectStats} dataKeyX="key" dataKeyY="doc_count" color="#8884d8" />
      </div>

      {/* Export */}
      <div>
        <button onClick={exportCSV}>💾 Export CSV</button>
        <button onClick={exportJSON} style={{ marginLeft: 8 }}>💾 Export JSON</button>
      </div>

      {/* Table */}
      <div>
        <h2>📄 Danh sách Logs</h2>
        <table>
          <thead>
            <tr>
              <th>🕒 Thời gian</th>
              <th>🆔 ID</th>
              <th>🎯 Target</th>
              <th>👤 Subject</th>
              <th>🌐 IP</th>
              <th>📝 Action</th>
              <th>🔗 Correlation ID</th>
            </tr>
          </thead>
          <tbody>
            {currentLogs.map((log, i) => (
              <tr key={i} onClick={() => setSelectedLog(log)}>
                <td>{new Date(Number(log.createdAt)).toLocaleString()}</td>
                <td>{log.id}</td>
                <td>{log.targetType}:{log.targetId}</td>
                <td>{log.subjectType}:{log.subjectId}</td>
                <td>{log.data?.ip || '-'}</td>
                <td>{log.data?.action || '-'}</td>
                <td>{log.correlationId}</td>
              </tr>
            ))}
            {currentLogs.length === 0 && (
              <tr><td colSpan="7">Không có log nào phù hợp</td></tr>
            )}
          </tbody>
        </table>

        <div>
          <span>Trang {currentPage}/{totalPages}</span>
          <button onClick={() => setCurrentPage(p => Math.max(p - 1, 1))} disabled={currentPage === 1}>◀ Trước</button>
          <button onClick={() => setCurrentPage(p => Math.min(p + 1, totalPages))} disabled={currentPage === totalPages}>Sau ▶</button>
        </div>
      </div>

      {/* Modal */}
      <Modal
        isOpen={!!selectedLog}
        onRequestClose={() => setSelectedLog(null)}
        contentLabel="Chi tiết Log"
        className="small-modal"
        overlayClassName="overlay"
      >
        {selectedLog && (
          <div>
            <button className="close-btn" onClick={() => setSelectedLog(null)}>❌</button>
            <h2>📋 Chi tiết Log</h2>
            <pre>{JSON.stringify(selectedLog, null, 2)}</pre>
          </div>
        )}
      </Modal>

    </div>
  );
}

function BarChartCard({ title, data, dataKeyX, dataKeyY, color }) {
  return (
    <div className="chart-card">
      <h3>{title}</h3>
      <ResponsiveContainer width="100%" height={200}>
        <BarChart data={data}>
          <XAxis dataKey={dataKeyX} />
          <YAxis />
          <Tooltip />
          <Bar dataKey={dataKeyY} fill={color} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

function LineChartCard({ title, data, dataKeyX, dataKeyY }) {
  return (
    <div className="chart-card">
      <h3>{title}</h3>
      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={data}>
          <XAxis dataKey={dataKeyX} />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey={dataKeyY} stroke="#ff7300" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export default App;
