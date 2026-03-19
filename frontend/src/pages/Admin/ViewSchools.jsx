import React, { useEffect, useState } from 'react';
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  IconButton, MenuItem, Paper, Select, InputLabel, FormControl,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography, CircularProgress, Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';

const emptyForm = { name: '', address: '', headmasterId: '' };

const fieldProps = {
  InputProps: { sx: { color: 'black' } },
  InputLabelProps: { shrink: true, sx: { color: 'black' } },
};

function SchoolFormFields({ form, setForm, t, headmasters }) {
  return (
    <>
      <TextField
        label={t('schools.name')}
        value={form.name}
        onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
        required
        fullWidth
        {...fieldProps}
      />
      <TextField
        label={t('schools.address')}
        value={form.address}
        onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
        multiline
        rows={3}
        fullWidth
        {...fieldProps}
      />
      <FormControl fullWidth>
        <InputLabel sx={{ color: 'black' }}>{t('schools.headmaster')}</InputLabel>
        <Select
          value={form.headmasterId}
          onChange={e => setForm(f => ({ ...f, headmasterId: e.target.value }))}
          label={t('schools.headmaster')}
          sx={{ color: 'black' }}
        >
          <MenuItem value=""><em>{t('schools.noHeadmaster')}</em></MenuItem>
          {headmasters.map(h => (
            <MenuItem key={h.id} value={h.id}>{h.name}</MenuItem>
          ))}
        </Select>
      </FormControl>
    </>
  );
}

function ViewSchools() {
  const { t } = useTranslation();

  const [schools, setSchools]         = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState(null);
  const [saving, setSaving]           = useState(false);

  // Add dialog
  const [addOpen, setAddOpen]         = useState(false);
  const [addForm, setAddForm]         = useState(emptyForm);

  // Edit dialog
  const [editOpen, setEditOpen]       = useState(false);
  const [editId, setEditId]           = useState(null);
  const [editForm, setEditForm]       = useState(emptyForm);

  // Headmasters list for dropdown
  const [headmasters, setHeadmasters] = useState([]);

  // Delete confirm
  const [confirmId, setConfirmId]     = useState(null);
  const [confirmName, setConfirmName] = useState('');

  const fetchSchools = () => {
    setLoading(true);
    setError(null);
    api.get('/api/schools')
      .then(res => setSchools(res.data))
      .catch(() => setError(t('schools.fetchError')))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchSchools();
    api.get('/api/users/headmasters').then(res => setHeadmasters(res.data)).catch(() => {});
  }, []);

  const toPayload = form => ({
    name: form.name,
    address: form.address,
    headmasterId: form.headmasterId ? Number(form.headmasterId) : null,
  });

  const handleCreate = () => {
    setSaving(true);
    api.post('/api/schools', toPayload(addForm))
      .then(res => {
        setSchools(prev => [...prev, res.data]);
        setAddOpen(false);
        setAddForm(emptyForm);
      })
      .catch(() => setError(t('schools.createError')))
      .finally(() => setSaving(false));
  };

  const handleRowClick = (school) => {
    setEditId(school.id);
    const match = headmasters.find(h => h.name === school.headmasterName);
    setEditForm({
      name: school.name || '',
      address: school.address || '',
      headmasterId: match ? match.id : '',
    });
    setEditOpen(true);
  };

  const handleUpdate = () => {
    setSaving(true);
    api.put(`/api/schools/${editId}`, toPayload(editForm))
      .then(res => {
        setSchools(prev => prev.map(s => s.id === editId ? res.data : s));
        setEditOpen(false);
      })
      .catch(() => setError(t('schools.updateError')))
      .finally(() => setSaving(false));
  };

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

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('schools.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAddOpen(true)}>
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
                    <TableRow
                      key={school.id}
                      hover
                      onClick={() => handleRowClick(school)}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell>{school.id}</TableCell>
                      <TableCell>{school.name}</TableCell>
                      <TableCell>{school.address || '—'}</TableCell>
                      <TableCell>{school.headmasterName || '—'}</TableCell>
                      <TableCell align="center">
                        <IconButton onClick={e => { e.stopPropagation(); setConfirmId(school.id); setConfirmName(school.name); }}>
                          <DeleteIcon sx={{ color: 'red' }} />
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
      <Dialog open={addOpen} onClose={() => setAddOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('schools.addSchool')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <SchoolFormFields form={addForm} setForm={setAddForm} t={t} headmasters={headmasters} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddOpen(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!addForm.name.trim() || saving}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit School Dialog */}
      <Dialog open={editOpen} onClose={() => setEditOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('schools.editSchool')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <SchoolFormFields form={editForm} setForm={setEditForm} t={t} headmasters={headmasters} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditOpen(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={handleUpdate} disabled={!editForm.name.trim() || saving}>
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
