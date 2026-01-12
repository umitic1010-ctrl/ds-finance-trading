import React from 'react';
import { Container, Paper, Typography } from '@mui/material';
import Layout from '../components/Layout';
import TradingPanel from '../components/TradingPanel';
import { useAuth } from '../context/AuthContext';

const CustomerDashboard = () => {
  const { user } = useAuth();

  return (
    <Layout title="Kunden Dashboard">
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h5" gutterBottom>
            Willkommen, {user?.email}!
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Verwalten Sie Ihr Portfolio und handeln Sie mit Aktien.
          </Typography>
        </Paper>

        <TradingPanel isEmployee={false} customerNumber={user?.customerNumber} />
      </Container>
    </Layout>
  );
};

export default CustomerDashboard;

