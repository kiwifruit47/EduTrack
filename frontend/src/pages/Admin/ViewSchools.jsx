import React, { useEffect, useState } from 'react';
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  IconButton, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography, CircularProgress, Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';

function ViewSchools() {
  const { t } = useTranslation();

  const [schools, setSchools]     = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm]           = useState({ name: '', address: '' });
  const [saving, setSaving]       = useState(false);

  const fetchSchools = () => {
    setLoading(true);
    setError(null);
    api.get('/api/schools')
      .then(res => setSchools(res.data))
      .catch(() => setError(t('schools.fetchError')))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchSchools(); }, []);

  const handleDelete = (id) => {
    api.delete(`/api/schools/${id}`)
      .then(() => setSchools(prev => prev.filter(s => s.id !== id)))
      .catch(() => setError(t('schools.deleteError')));
  };

  const handleCreate = () => {
    setSaving(true);
    api.post('/api/schools', form)
      .then(res => {
        setSchools(prev => [...prev, res.data]);
        setDialogOpen(false);
        setForm({ name: '', address: '' });
      })
      .catch(() => setError(t('schools.createError')))
      .finally(() => setSaving(false));
  };

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('schools.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>
            {t('schools.addSchool')}
          </Button>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>{t('schools.name')}</TableCell>
                  <TableCell>{t('schools.address')}</TableCell>
                  <TableCell>{t('schools.director')}</TableCell>
                  <TableCell align="center">{t('schools.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {schools.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">{t('schools.noSchools')}</TableCell>
                  </TableRow>
                ) : (
                  schools.map(school => (
                    <TableRow key={school.id} hover>
                      <TableCell>{school.id}</TableCell>
                      <TableCell>{school.name}</TableCell>
                      <TableCell>{school.address || '—'}</TableCell>
                      <TableCell>{school.directorName || '—'}</TableCell>
                      <TableCell align="center">
                        <IconButton color="error" onClick={() => handleDelete(school.id)}>
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Box>

      {/* Add School Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('schools.addSchool')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label={t('schools.name')}
            value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
            required
            fullWidth
            InputProps={{ sx: { color: 'black' } }}
            InputLabelProps={{ sx: { color: 'black' } }}
          />
          <TextField
            label={t('schools.address')}
            value={form.address}
            onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
            multiline
            rows={3}
            fullWidth
            InputProps={{ sx: { color: 'black' } }}
            InputLabelProps={{ sx: { color: 'black' } }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>{t('common.cancel')}</Button>
          <Button
            variant="contained"
            onClick={handleCreate}
            disabled={!form.name.trim() || saving}
          >
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}

export default ViewSchools;
