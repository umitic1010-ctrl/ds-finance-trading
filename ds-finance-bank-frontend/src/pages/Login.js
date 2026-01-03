import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert
} from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Bitte alle Felder ausfüllen');
      return;
    }

    try {
      const authData = await login(email, password);
      navigate(authData.role === 'employee' ? '/employee' : '/customer');
    } catch (err) {
      setError('Login fehlgeschlagen. Bitte Zugangsdaten prüfen.');
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
            <AccountBalanceIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
            <Typography component="h1" variant="h4" gutterBottom>
              DS Finance Bank
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Banking & Trading System
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="E-Mail"
              name="email"
              autoComplete="email"
              autoFocus
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Passwort"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
            >
              Anmelden
            </Button>
          </Box>

          <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
            <Typography variant="caption" display="block" gutterBottom>
              <strong>Test-Benutzer:</strong>
            </Typography>
            <Typography variant="caption" display="block">
              Mitarbeiter: example_e@banking.de / employeepass
            </Typography>
            <Typography variant="caption" display="block">
              Kunde: example_c@banking.de / customerpass
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;

