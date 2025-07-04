
import { useState } from 'react';
import { Box, Paper, Typography, TextField, Button, Grid, CircularProgress, Alert } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { fetchTypeStatisticsByTime } from '../services/apiService';
import dayjs from 'dayjs';


const StatsForm = ({ onStatsFetch, loading }) => {
    const [fromTime, setFromTime] = useState('');
    const [toTime, setToTime] = useState('');
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!fromTime || !toTime) return alert('Vui lòng chọn đủ khoảng thời gian');
        onStatsFetch({ fromTime: dayjs(fromTime).valueOf(), toTime: dayjs(toTime).valueOf() });
    };
    return (
        <Paper sx={{ p: 2, mb: 4 }}>
            <Typography variant="h6" gutterBottom>Thống kê sự kiện theo khoảng thời gian</Typography>
            <Grid container spacing={2} alignItems="center">
                <Grid item xs={12} sm={5}><TextField fullWidth name="fromTime" label="Từ lúc" type="datetime-local" InputLabelProps={{ shrink: true }} value={fromTime} onChange={(e) => setFromTime(e.target.value)} /></Grid>
                <Grid item xs={12} sm={5}><TextField fullWidth name="toTime" label="Đến lúc" type="datetime-local" InputLabelProps={{ shrink: true }} value={toTime} onChange={(e) => setToTime(e.target.value)} /></Grid>
                <Grid item xs={12} sm={2}><Button fullWidth variant="contained" onClick={handleSubmit} disabled={loading}>{loading ? <CircularProgress size={24} /> : 'Xem'}</Button></Grid>
            </Grid>
        </Paper>
    );
};

// COMPONENT BIỂU ĐỒ
const StatsChart = ({ data }) => {
    const chartData = [];
    const allKeys = new Set([...Object.keys(data.subjectTypes), ...Object.keys(data.targetTypes)]);
    allKeys.forEach(key => { chartData.push({ name: key, 'Chủ thể (Subject)': data.subjectTypes[key] || 0, 'Đối tượng (Target)': data.targetTypes[key] || 0 }); });
    return (
        <Paper sx={{ p: 2, height: 500 }}>
             <Typography variant="h6" gutterBottom>Phân bổ theo Loại Chủ thể và Đối tượng</Typography>
            <ResponsiveContainer width="100%" height="90%"><BarChart data={chartData}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="name" /><YAxis /><Tooltip /><Legend /><Bar dataKey="Chủ thể (Subject)" fill="#8884d8" /><Bar dataKey="Đối tượng (Target)" fill="#82ca9d" /></BarChart></ResponsiveContainer>
        </Paper>
    );
};

// COMPONENT TRANG CHÍNH
const NewStatsPage = () => {
    const [statsData, setStatsData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleStatsFetch = async (params) => {
        setLoading(true);
        setError(null);
        try {
            const result = await fetchTypeStatisticsByTime(params);
            const subjectTypesMap = result.subjectTypes.reduce((acc, item) => { acc[item.key] = item.doc_count; return acc; }, {});
            const targetTypesMap = result.targetTypes.reduce((acc, item) => { acc[item.key] = item.doc_count; return acc; }, {});
            setStatsData({ subjectTypes: subjectTypesMap, targetTypes: targetTypesMap });
        } catch (err) { setError(err.message || "Lỗi lấy dữ liệu thống kê"); } 
        finally { setLoading(false); }
    };

    return (
        <Box>
            <StatsForm onStatsFetch={handleStatsFetch} loading={loading} />
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            {statsData && <StatsChart data={statsData} />}
        </Box>
    );
};

export default NewStatsPage;