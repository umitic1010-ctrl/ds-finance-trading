import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert
} from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SearchIcon from '@mui/icons-material/Search';
import { customerService } from '../services/api';

const CustomerManagement = () => {
  const [customers, setCustomers] = useState([]);
  const [searchName, setSearchName] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });

  const [newCustomer, setNewCustomer] = useState({
    customerNumber: '',
    firstName: '',
    lastName: '',
    address: '',
    email: '',
    phoneNumber: '',
    password: ''
  });

  const handleSearch = async () => {
    const term = searchName.trim();
    try {
      if (!term) {
        const { data } = await customerService.getAll();
        setCustomers(Array.isArray(data) ? data : []);
        setMessage({ text: `${data.length || 0} Kunden gefunden`, type: 'success' });
        return;
      }

      const resultsMap = new Map();

      try {
        const { data } = await customerService.getByNumber(term);
        if (data) {
          resultsMap.set(data.customerNumber, data);
        }
      } catch (error) {
        if (error.response?.status !== 404) {
          throw error;
        }
      }

      const { data: searchResults } = await customerService.search(term);
      if (Array.isArray(searchResults)) {
        searchResults.forEach((customer) => {
          if (customer?.customerNumber) {
            resultsMap.set(customer.customerNumber, customer);
          }
        });
      }

      const merged = Array.from(resultsMap.values());
      setCustomers(merged);
      setMessage({ text: `${merged.length} Kunden gefunden`, type: merged.length ? 'success' : 'warning' });
    } catch (error) {
      setMessage({ text: error.response?.data?.error || 'Fehler beim Laden der Kunden', type: 'error' });
    }
  };

  const handleCreateCustomer = async () => {
    try {
      await customerService.create(newCustomer);
      setMessage({ text: 'Kunde erfolgreich angelegt!', type: 'success' });
      setOpenDialog(false);
      setNewCustomer({
        customerNumber: '',
        firstName: '',
        lastName: '',
        address: '',
        email: '',
        phoneNumber: '',
        password: ''
      });
      handleSearch();
    } catch (error) {
      setMessage({ text: error.response?.data?.error || 'Fehler beim Anlegen des Kunden', type: 'error' });
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
        <TextField
          label="Kunde suchen (Name)"
          variant="outlined"
          value={searchName}
          onChange={(e) => setSearchName(e.target.value)}
          sx={{ flexGrow: 1 }}
        />
        <Button
          variant="contained"
          startIcon={<SearchIcon />}
          onClick={handleSearch}
        >
          Suchen
        </Button>
        <Button
          variant="contained"
          color="success"
          startIcon={<PersonAddIcon />}
          onClick={() => setOpenDialog(true)}
        >
          Neuer Kunde
        </Button>
      </Box>

      {message.text && (
        <Alert severity={message.type} sx={{ mb: 2 }} onClose={() => setMessage({ text: '', type: '' })}>
          {message.text}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Kundennummer</strong></TableCell>
              <TableCell><strong>Name</strong></TableCell>
              <TableCell><strong>Adresse</strong></TableCell>
              <TableCell><strong>Email</strong></TableCell>
              <TableCell><strong>Telefon</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {customers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  <Typography color="text.secondary">
                    Keine Kunden gefunden. Verwenden Sie die Suche oder legen Sie einen neuen Kunden an.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              customers.map((customer) => (
                <TableRow key={customer.id}>
                  <TableCell>{customer.customerNumber}</TableCell>
                  <TableCell>{customer.firstName} {customer.lastName}</TableCell>
                  <TableCell>{customer.address}</TableCell>
                  <TableCell>{customer.email}</TableCell>
                  <TableCell>{customer.phoneNumber}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create Customer Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Neuen Kunden anlegen</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Kundennummer"
            fullWidth
            variant="outlined"
            value={newCustomer.customerNumber}
            onChange={(e) => setNewCustomer({ ...newCustomer, customerNumber: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Vorname"
            fullWidth
            variant="outlined"
            value={newCustomer.firstName}
            onChange={(e) => setNewCustomer({ ...newCustomer, firstName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Nachname"
            fullWidth
            variant="outlined"
            value={newCustomer.lastName}
            onChange={(e) => setNewCustomer({ ...newCustomer, lastName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Adresse"
            fullWidth
            variant="outlined"
            value={newCustomer.address}
            onChange={(e) => setNewCustomer({ ...newCustomer, address: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Email"
            type="email"
            fullWidth
            variant="outlined"
            value={newCustomer.email}
            onChange={(e) => setNewCustomer({ ...newCustomer, email: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Initiales Passwort"
            type="password"
            fullWidth
            variant="outlined"
            value={newCustomer.password}
            onChange={(e) => setNewCustomer({ ...newCustomer, password: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Telefon"
            fullWidth
            variant="outlined"
            value={newCustomer.phoneNumber}
            onChange={(e) => setNewCustomer({ ...newCustomer, phoneNumber: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Abbrechen</Button>
          <Button onClick={handleCreateCustomer} variant="contained">Anlegen</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CustomerManagement;

