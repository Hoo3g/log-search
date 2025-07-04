
import { useState } from 'react';
import { Box, Tab, Tabs, Typography, CssBaseline, Container } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import DashboardIcon from '@mui/icons-material/Dashboard';
import SingleSearchPage from './pages/SingleSearchPage';
import NewStatsPage from './pages/NewStatsPage';

function TabPanel(props) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

function App() {
  const [value, setValue] = useState(0);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
      <Container maxWidth="xl" sx={{ mt: 2 }}>
        <CssBaseline />
        <Typography variant="h4" component="h1" gutterBottom align="center">
          Hệ thống giám sát Event Log
        </Typography>
        <Box sx={{ width: '100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={value} onChange={handleChange} centered>
              <Tab icon={<SearchIcon />} iconPosition="start" label="Tìm kiếm" />
              <Tab icon={<DashboardIcon />} iconPosition="start" label="Thống kê" />
            </Tabs>
          </Box>
          <TabPanel value={value} index={0}>
            <SingleSearchPage />
          </TabPanel>
          <TabPanel value={value} index={1}>
            <NewStatsPage />
          </TabPanel>
        </Box>
      </Container>
  );
}

export default App;