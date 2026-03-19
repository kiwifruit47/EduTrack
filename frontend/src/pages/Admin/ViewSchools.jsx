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
  const [dialogOpen, setDialogOpen]         = useState(false);
  const [form, setForm]                     = useState({ name: '', address: '' });
  const [saving, setSaving]                 = useState(false);
  const [confirmId, setConfirmId]           = useState(null);
  const [confirmName, setConfirmName]       = useState('');

  const fetchSchools = () => {
    setLoading(true);
    setError(null);
    api.get('/api/schools')
      .then(res => setSchools(res.data))
      .catch(() => setError(t('schools.fetchError')))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchSchools(); }, []);

  const handleDeleteConfirm = () => {
    api.delete(`/api/schools/${confirmId}`)
      .then(() => {
        setSchools(prev => prev.filter(s => s.id !== confirmId));
        setConfirmId(null);
      })
      .catch(() => {
        setError(t('schools.deleteError'));
        setConfirmId(null);
      });
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
                  <TableCell>{t('schools.headmaster')}</TableCell>
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
                        <IconButton color="error" onClick={() => { setConfirmId(school.id); setConfirmName(school.name); }}>
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
            InputLabelProps={{sx: { color: 'black' } }}
          />
          <TextField
              label={t('schools.headmaster')}
              value={form.address}
              onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
              multiline
              rows={3}
              fullWidth
              InputProps={{ sx: { color: 'black' } }}
              InputLabelProps={{sx: { color: 'black' } }}
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
      {/* Delete Confirmation Dialog */}
      <Dialog open={confirmId !== null} onClose={() => setConfirmId(null)}>
        <DialogTitle>{t('schools.deleteConfirmTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('schools.deleteConfirmMessage', { name: confirmName })}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmId(null)}>{t('common.cancel')}</Button>
          <Button variant="contained" color="error" onClick={handleDeleteConfirm}>
            {t('common.delete')}
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}

export default ViewSchools;
