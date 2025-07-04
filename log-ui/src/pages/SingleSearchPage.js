
import { useState } from 'react';
import { Box, Paper, Typography, FormControl, InputLabel, Select, MenuItem, TextField, Button, Grid, CircularProgress, Alert, TableContainer, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import * as api from '../services/apiService';
import dayjs from 'dayjs';


const EventTable = ({ events }) => (
    <Box>
        <Typography variant="h6" gutterBottom>Kết quả tìm kiếm</Typography>
        <TableContainer component={Paper}>
            <Table>
                <TableHead><TableRow><TableCell>ID</TableCell><TableCell>Loại sự kiện</TableCell><TableCell>Đối tượng</TableCell><TableCell>Chủ thể</TableCell><TableCell>Thời gian</TableCell></TableRow></TableHead>
                <TableBody>
                    {events.length > 0 ? events.map((event) => (
                        <TableRow key={event.id}><TableCell sx={{wordBreak: 'break-all'}}>{event.id}</TableCell><TableCell>{event.type}</TableCell><TableCell>{`${event.targetType}: ${event.targetId}`}</TableCell><TableCell>{`${event.subjectType}: ${event.subjectId}`}</TableCell><TableCell>{dayjs(event.createdAt).format('HH:mm:ss DD/MM/YYYY')}</TableCell></TableRow>
                    )) : (<TableRow><TableCell colSpan={5} align="center">Không có dữ liệu hoặc chưa tìm kiếm.</TableCell></TableRow>)}
                </TableBody>
            </Table>
        </TableContainer>
    </Box>
);


// COMPONENT FORM
const SearchForm = ({ onSearch, loading }) => {
    const [searchType, setSearchType] = useState('fullText');
    const [formState, setFormState] = useState({ eventId: '', subjectType: '', subjectId: '', targetType: '', targetId: '', eventType: '', correlationId: '', searchText: '', fromTime: '', toTime: '' });

    const handleChange = (e) => setFormState({ ...formState, [e.target.name]: e.target.value });

    const handleSubmit = (e) => {
        e.preventDefault();
        const { eventId, subjectType, subjectId, targetType, targetId, eventType, correlationId, searchText, fromTime, toTime } = formState;
        
        let searchPromise;
        switch (searchType) {
            case 'eventId': searchPromise = api.searchByEventId(eventId); break;
            case 'subject': searchPromise = api.searchBySubject(subjectType, subjectId); break;
            case 'target': searchPromise = api.searchByTarget(targetType, targetId); break;
            case 'eventType': searchPromise = api.searchByEventType(eventType); break;
            case 'correlationId': searchPromise = api.searchByCorrelationId(correlationId); break;
            case 'fullText': searchPromise = api.searchFullText(searchText); break;
            case 'timeRange': searchPromise = api.searchByTimeRange(dayjs(fromTime).valueOf(), dayjs(toTime).valueOf()); break;
            default: return;
        }
        onSearch(searchPromise);
    };

    const renderInputs = () => {
        switch (searchType) {
            case 'eventId': return <TextField fullWidth name="eventId" label="Event ID" value={formState.eventId} onChange={handleChange} />;
            case 'fullText': return <TextField fullWidth name="searchText" label="Nội dung tìm kiếm" value={formState.searchText} onChange={handleChange} />;
            case 'eventType': return <TextField fullWidth name="eventType" label="Loại sự kiện" value={formState.eventType} onChange={handleChange} />;
            case 'correlationId': return <TextField fullWidth name="correlationId" label="Correlation ID" value={formState.correlationId} onChange={handleChange} />;
            case 'subject': return (<Grid container spacing={2}><Grid item xs={6}><TextField fullWidth name="subjectType" label="Loại chủ thể" value={formState.subjectType} onChange={handleChange} /></Grid><Grid item xs={6}><TextField fullWidth name="subjectId" label="ID chủ thể" value={formState.subjectId} onChange={handleChange} /></Grid></Grid>);
            case 'target': return (<Grid container spacing={2}><Grid item xs={6}><TextField fullWidth name="targetType" label="Loại đối tượng" value={formState.targetType} onChange={handleChange} /></Grid><Grid item xs={6}><TextField fullWidth name="targetId" label="ID đối tượng" value={formState.targetId} onChange={handleChange} /></Grid></Grid>);
            case 'timeRange': return (<Grid container spacing={2}><Grid item xs={6}><TextField fullWidth name="fromTime" label="Từ lúc" type="datetime-local" InputLabelProps={{ shrink: true }} value={formState.fromTime} onChange={handleChange} /></Grid><Grid item xs={6}><TextField fullWidth name="toTime" label="Đến lúc" type="datetime-local" InputLabelProps={{ shrink: true }} value={formState.toTime} onChange={handleChange} /></Grid></Grid>);
            default: return null;
        }
    };

    return (
        <Paper sx={{ p: 2, mb: 4 }}>
            <Typography variant="h6" gutterBottom>Tìm kiếm Event Log</Typography>
            <Grid container spacing={2} alignItems="center">
                <Grid item xs={12} md={4}><FormControl fullWidth><InputLabel>Loại tìm kiếm</InputLabel><Select value={searchType} label="Loại tìm kiếm" onChange={(e) => setSearchType(e.target.value)}><MenuItem value="fullText">Tìm kiếm Toàn văn</MenuItem><MenuItem value="timeRange">Khoảng thời gian</MenuItem><MenuItem value="target">Đối tượng (Target)</MenuItem><MenuItem value="subject">Chủ thể (Subject)</MenuItem><MenuItem value="eventType">Loại sự kiện</MenuItem><MenuItem value="correlationId">Correlation ID</MenuItem><MenuItem value="eventId">Event ID</MenuItem></Select></FormControl></Grid>
                <Grid item xs={12} md={8}>{renderInputs()}</Grid>
                <Grid item xs={12}><Button variant="contained" onClick={handleSubmit} disabled={loading}>{loading ? <CircularProgress size={24} /> : 'Tìm kiếm'}</Button></Grid>
            </Grid>
        </Paper>
    );
};


//COMPONENT TRANG CHÍNH
const SingleSearchPage = () => {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSearch = async (searchPromise) => {
        setLoading(true);
        setError(null);
        try {
            const results = await searchPromise;
            setEvents(results);
        } catch (err) {
            setError(err.message || 'Lỗi tìm kiếm');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box>
            <SearchForm onSearch={handleSearch} loading={loading} />
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            <EventTable events={events} />
        </Box>
    );
};

export default SingleSearchPage;