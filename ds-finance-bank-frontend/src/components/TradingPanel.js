import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  TextField,
  Button,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import SellIcon from '@mui/icons-material/Sell';
import { tradingService } from '../services/api';

const DEFAULT_CURRENCY = 'USD';

const toNumber = (value) => {
  if (value === null || value === undefined || value === '') {
    return 0;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

const formatCurrency = (value, currency = DEFAULT_CURRENCY) => {
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: currency || DEFAULT_CURRENCY,
    minimumFractionDigits: 2,
  }).format(toNumber(value));
};

const TradingPanel = ({ isEmployee, customerNumber }) => {
  const [stocks, setStocks] = useState([]);
  const [depot, setDepot] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [message, setMessage] = useState({ text: '', type: '' });
  const [tradeDialog, setTradeDialog] = useState({ open: false, type: '', stock: null });
  const [tradeData, setTradeData] = useState({
    customerNumber: isEmployee ? '' : (customerNumber || ''),
    stockSymbol: '',
    quantity: 1
  });

  useEffect(() => {
    if (!isEmployee && customerNumber) {
      setTradeData((prev) => ({ ...prev, customerNumber }));
      loadDepot(customerNumber);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerNumber, isEmployee]);

  // Auto-refresh depot every 10 seconds
  useEffect(() => {
    const custNum = effectiveCustomerNumber();
    if (!custNum) {
      console.log('[Auto-Refresh] No customer number, skipping');
      return;
    }

    console.log('[Auto-Refresh] Setting up interval for customer:', custNum);
    const intervalId = setInterval(() => {
      console.log('[Auto-Refresh] Refreshing depot for:', custNum);
      loadDepot(custNum);
    }, 10000); // 10 seconds

    return () => {
      console.log('[Auto-Refresh] Cleaning up interval');
      clearInterval(intervalId);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerNumber, tradeData.customerNumber, isEmployee]);

  const effectiveCustomerNumber = () => {
    if (isEmployee) {
      return (tradeData.customerNumber || '').trim();
    }
    return (customerNumber || '').trim();
  };

  const handleSearchStocks = async () => {
    try {
      const query = searchQuery.trim();
      const { data } = await tradingService.searchStocks(query);
      const list = Array.isArray(data) ? data : [];
      setStocks(list);
      setMessage({
        text: list.length ? `${list.length} Aktien gefunden` : 'Keine Aktien gefunden',
        type: list.length ? 'success' : 'warning'
      });
    } catch (error) {
      setMessage({ text: 'Fehler beim Laden der Aktien', type: 'error' });
    }
  };

  const loadDepot = async (custNumber) => {
    try {
      const customer = (custNumber || effectiveCustomerNumber()).trim();
      if (!customer) {
        setMessage({ text: 'Bitte geben Sie eine Kundennummer ein', type: 'warning' });
        return;
      }
      console.log('[LoadDepot] Loading depot for customer:', customer);
      const { data } = await tradingService.getDepot(customer);
      console.log('[LoadDepot] Depot loaded, total value:', data.totalValue);
      setDepot(data);
    } catch (error) {
      setMessage({ text: 'Fehler beim Laden des Depots', type: 'error' });
    }
  };

  const openTradeDialog = (type, stock) => {
    setTradeDialog({ open: true, type, stock });
    setTradeData((prev) => ({
      ...prev,
      stockSymbol: stock.symbol,
      quantity: 1
    }));
  };

  const handleTrade = async () => {
    try {
      const quantity = Number(tradeData.quantity);
      if (!Number.isInteger(quantity) || quantity <= 0) {
        setMessage({ text: 'Bitte geben Sie eine gültige Stückzahl an', type: 'warning' });
        return;
      }

      if (!tradeData.stockSymbol) {
        setMessage({ text: 'Bitte wählen Sie eine Aktie aus', type: 'warning' });
        return;
      }

      const payload = {
        stockSymbol: tradeData.stockSymbol,
        quantity
      };

      if (isEmployee) {
        const customer = effectiveCustomerNumber();
        if (!customer) {
          setMessage({ text: 'Bitte geben Sie eine Kundennummer ein', type: 'warning' });
          return;
        }
        payload.customerNumber = customer;
      }

      if (tradeDialog.type === 'buy') {
        await tradingService.buyStocks(payload);
        setMessage({ text: 'Aktien erfolgreich gekauft!', type: 'success' });
      } else {
        await tradingService.sellStocks(payload);
        setMessage({ text: 'Aktien erfolgreich verkauft!', type: 'success' });
      }
      setTradeDialog({ open: false, type: '', stock: null });
      if (payload.customerNumber || !isEmployee) {
        loadDepot(payload.customerNumber);
      }
    } catch (error) {
      setMessage({
        text: error.response?.data?.error || 'Fehler beim Handel',
        type: 'error'
      });
    }
  };

  return (
    <Box>
      <Grid container spacing={3}>
        {/* Stock Search */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Aktien suchen
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
              <TextField
                label="Suchbegriff (z.B. Apple)"
                variant="outlined"
                fullWidth
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <Button
                variant="contained"
                startIcon={<SearchIcon />}
                onClick={handleSearchStocks}
              >
                Suchen
              </Button>
            </Box>

            {message.text && (
              <Alert severity={message.type} sx={{ mb: 2 }} onClose={() => setMessage({ text: '', type: '' })}>
                {message.text}
              </Alert>
            )}

            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell><strong>Symbol</strong></TableCell>
                    <TableCell><strong>Name</strong></TableCell>
                    <TableCell align="right"><strong>Preis</strong></TableCell>
                    <TableCell align="center"><strong>Aktionen</strong></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {stocks.map((stock) => {
                    const price = toNumber(stock.currentPrice);
                    const currency = stock.currency || DEFAULT_CURRENCY;
                    return (
                      <TableRow key={stock.symbol}>
                        <TableCell>{stock.symbol}</TableCell>
                        <TableCell>{stock.name}</TableCell>
                        <TableCell align="right">{formatCurrency(price, currency)}</TableCell>
                      <TableCell align="center">
                        <Button
                          size="small"
                          variant="contained"
                          color="success"
                          startIcon={<ShoppingCartIcon />}
                          onClick={() => openTradeDialog('buy', stock)}
                        >
                          Kaufen
                        </Button>
                      </TableCell>
                      </TableRow>
                    );
                  })}
                  {stocks.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        <Typography color="text.secondary">
                          Keine Aktien gefunden. Verwenden Sie die Suche.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* Depot */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Depot
            </Typography>

            {isEmployee && (
              <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <TextField
                  label="Kundennummer"
                  variant="outlined"
                  fullWidth
                  value={tradeData.customerNumber}
                  onChange={(e) => setTradeData({ ...tradeData, customerNumber: e.target.value })}
                />
                <Button variant="contained" onClick={() => loadDepot()}>
                  Laden
                </Button>
              </Box>
            )}

            {depot && (
              <Card variant="outlined" sx={{ mb: 2, bgcolor: 'primary.light', color: 'white' }}>
                <CardContent>
                  <Typography variant="h6">Gesamtwert</Typography>
                  <Typography variant="h4">
                    {formatCurrency(depot?.totalValue)}
                  </Typography>
                  <Typography variant="caption">
                    {depot.positions?.length || 0} Positionen
                  </Typography>
                </CardContent>
              </Card>
            )}

            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell><strong>Aktie</strong></TableCell>
                    <TableCell align="right"><strong>Anzahl</strong></TableCell>
                    <TableCell align="right"><strong>Kurs</strong></TableCell>
                    <TableCell align="right"><strong>Wert</strong></TableCell>
                    <TableCell align="center"><strong>Aktion</strong></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {depot?.positions?.map((position) => {
                    return (
                      <TableRow key={position.stockSymbol}>
                        <TableCell>
                          <Typography variant="body2" fontWeight="bold">{position.stockSymbol}</Typography>
                          <Typography variant="caption" color="text.secondary">{position.stockName}</Typography>
                        </TableCell>
                        <TableCell align="right">{position.quantity}</TableCell>
                        <TableCell align="right">{formatCurrency(position.currentPrice)}</TableCell>
                        <TableCell align="right">{formatCurrency(position.totalValue)}</TableCell>
                        <TableCell align="center">
                          <Button
                            size="small"
                            variant="outlined"
                            color="error"
                            startIcon={<SellIcon />}
                            onClick={() => openTradeDialog('sell', { symbol: position.stockSymbol, name: position.stockName })}
                          >
                            Verkaufen
                          </Button>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                  {(!depot || depot.positions?.length === 0) && (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        <Typography color="text.secondary">
                          Keine Positionen im Depot
                        </Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>

      {/* Trade Dialog */}
      <Dialog open={tradeDialog.open} onClose={() => setTradeDialog({ open: false, type: '', stock: null })}>
        <DialogTitle>
          {tradeDialog.type === 'buy' ? 'Aktien kaufen' : 'Aktien verkaufen'}: {tradeDialog.stock?.symbol}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {tradeDialog.stock?.name}
          </Typography>
          {isEmployee && (
            <TextField
              margin="dense"
              label="Kundennummer"
              fullWidth
              variant="outlined"
              value={tradeData.customerNumber}
              onChange={(e) => setTradeData({ ...tradeData, customerNumber: e.target.value })}
            />
          )}
          <TextField
            margin="dense"
            label="Anzahl"
            type="number"
            fullWidth
            variant="outlined"
            value={tradeData.quantity}
            onChange={(e) => setTradeData({ ...tradeData, quantity: parseInt(e.target.value) })}
            inputProps={{ min: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTradeDialog({ open: false, type: '', stock: null })}>
            Abbrechen
          </Button>
          <Button onClick={handleTrade} variant="contained" color={tradeDialog.type === 'buy' ? 'success' : 'error'}>
            {tradeDialog.type === 'buy' ? 'Kaufen' : 'Verkaufen'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TradingPanel;

