import React, { useState, useEffect, useMemo } from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Button,
  Tabs,
  Tab
} from '@mui/material';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import PeopleIcon from '@mui/icons-material/People';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import Layout from '../components/Layout';
import CustomerManagement from '../components/CustomerManagement';
import TradingPanel from '../components/TradingPanel';
import { bankService } from '../services/api';

const DEFAULT_CURRENCY = 'USD';

const formatCurrency = (value, currency = DEFAULT_CURRENCY) => {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  }).format(amount);
};

const EmployeeDashboard = () => {
  const [tabValue, setTabValue] = useState(0);
  const [bankVolume, setBankVolume] = useState(null);

  useEffect(() => {
    loadBankVolume();
  }, []);

  const loadBankVolume = async () => {
    try {
      const response = await bankService.getVolume();
      setBankVolume(response.data);
    } catch (error) {
      console.error('Error loading bank volume:', error);
    }
  };

  const handleInitBank = async () => {
    try {
      await bankService.initialize();
      loadBankVolume();
      alert('Bank erfolgreich initialisiert!');
    } catch (error) {
      alert('Fehler bei der Initialisierung: ' + (error.response?.data?.error || error.message));
    }
  };

  const bankVolumeDisplay = useMemo(() => {
    if (!bankVolume) {
      return { available: 'Laden...', currency: DEFAULT_CURRENCY };
    }
    const currency = bankVolume.currency || DEFAULT_CURRENCY;
    return {
      available: formatCurrency(bankVolume.availableVolume, currency),
      currency,
    };
  }, [bankVolume]);

  return (
    <Layout title="Mitarbeiter Dashboard">
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        {/* Dashboard Cards */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <AccountBalanceWalletIcon sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
                  <Typography variant="h6">Bank-Volumen</Typography>
                </Box>
                <Typography variant="h4">
                  {bankVolumeDisplay.available}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Verfügbares Kapital ({bankVolumeDisplay.currency})
                </Typography>
                {bankVolume && (
                  <Typography variant="caption" color="text.secondary">
                    Startkapital: {formatCurrency(bankVolume.initialVolume, bankVolumeDisplay.currency)}
                  </Typography>
                )}
                <Button
                  variant="outlined"
                  size="small"
                  sx={{ mt: 2 }}
                  onClick={handleInitBank}
                >
                  Bank initialisieren
                </Button>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <PeopleIcon sx={{ fontSize: 40, color: 'success.main', mr: 2 }} />
                  <Typography variant="h6">Kunden</Typography>
                </Box>
                <Typography variant="h4">Verwalten</Typography>
                <Typography variant="body2" color="text.secondary">
                  Kunden anlegen & suchen
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <TrendingUpIcon sx={{ fontSize: 40, color: 'warning.main', mr: 2 }} />
                  <Typography variant="h6">Trading</Typography>
                </Box>
                <Typography variant="h4">Aktiv</Typography>
                <Typography variant="body2" color="text.secondary">
                  Aktien kaufen & verkaufen
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Main Content Tabs */}
        <Paper sx={{ width: '100%' }}>
          <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} centered>
            <Tab label="Kundenverwaltung" />
            <Tab label="Trading" />
          </Tabs>

          <Box sx={{ p: 3 }}>
            {tabValue === 0 && <CustomerManagement />}
            {tabValue === 1 && <TradingPanel isEmployee={true} />}
          </Box>
        </Paper>
      </Container>
    </Layout>
  );
};

export default EmployeeDashboard;

