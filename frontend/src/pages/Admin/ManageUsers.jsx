import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, FormControl, IconButton, InputLabel,
  MenuItem, Paper, Select, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';

const ROLES = ['ADMIN', 'HEADMASTER', 'TEACHER', 'STUDENT', 'PARENT'];

const ROLE_COLORS = {
  ADMIN:      'error',
  HEADMASTER: 'warning',
  TEACHER:    'primary',
  STUDENT:    'success',
  PARENT:     'secondary',
};

const emptyForm = { firstName: '', lastName: '', email: '', password: '', role: '' };

const fieldSx = {
  InputProps: { sx: { color: 'black' } },
  InputLabelProps: { shrink: true, sx: { color: 'black' } },
};

function UserFormFields({ form, setForm, t, isEdit }) {
  return (
    <>
      <Box sx={{ display: 'flex', gap: 2 }}>
        <TextField
          label={t('users.firstName')}
          value={form.firstName}
          onChange={e => setForm(f => ({ ...f, firstName: e.target.value }))}
          required fullWidth {...fieldSx}
        />
        <TextField
          label={t('users.lastName')}
          value={form.lastName}
          onChange={e => setForm(f => ({ ...f, lastName: e.target.value }))}
          required fullWidth {...fieldSx}
        />
      </Box>
      <TextField
        label={t('users.email')}
        value={form.email}
        onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
        type="email" required fullWidth {...fieldSx}
      />
      <TextField
        label={isEdit ? t('users.passwordOptional') : t('users.password')}
        value={form.password}
        onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
        type="password"
        required={!isEdit}
        fullWidth
        {...fieldSx}
      />
      <FormControl fullWidth required>
        <InputLabel sx={{ color: 'black' }}>{t('users.role')}</InputLabel>
        <Select
          value={form.role}
          onChange={e => setForm(f => ({ ...f, role: e.target.value }))}
          label={t('users.role')}
          sx={{ color: 'black' }}
        >
          {ROLES.map(r => <MenuItem key={r} value={r}>{r}</MenuItem>)}
        </Select>
      </FormControl>
    </>
  );
}

function ManageUsers() {
  const { t } = useTranslation();

  const [users, setUsers]             = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState(null);
  const [saving, setSaving]           = useState(false);
  const [roleFilter, setRoleFilter]   = useState('ALL');

  const [addOpen, setAddOpen]         = useState(false);
  const [addForm, setAddForm]         = useState(emptyForm);

  const [editOpen, setEditOpen]       = useState(false);
  const [editId, setEditId]           = useState(null);
  const [editForm, setEditForm]       = useState(emptyForm);

  const [confirmId, setConfirmId]     = useState(null);
  const [confirmName, setConfirmName] = useState('');

  useEffect(() => {
    api.get('/api/users')
      .then(res => setUsers(res.data))
      .catch(() => setError(t('users.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  const filtered = roleFilter === 'ALL' ? users : users.filter(u => u.role === roleFilter);

  const handleCreate = () => {
    setSaving(true);
    api.post('/api/users', addForm)
      .then(res => {
        setUsers(prev => [...prev, res.data]);
        setAddOpen(false);
        setAddForm(emptyForm);
      })
      .catch(err => setError(err.response?.data?.message || t('users.createError')))
      .finally(() => setSaving(false));
  };

  const handleRowClick = (user) => {
    setEditId(user.id);
    setEditForm({ firstName: user.firstName, lastName: user.lastName, email: user.email, password: '', role: user.role });
    setEditOpen(true);
  };

  const handleUpdate = () => {
    setSaving(true);
    api.put(`/api/users/${editId}`, editForm)
      .then(res => {
        setUsers(prev => prev.map(u => u.id === editId ? res.data : u));
        setEditOpen(false);
      })
      .catch(err => setError(err.response?.data?.message || t('users.updateError')))
      .finally(() => setSaving(false));
  };

  const handleDeleteConfirm = () => {
    api.delete(`/api/users/${confirmId}`)
      .then(() => {
        setUsers(prev => prev.filter(u => u.id !== confirmId));
        setConfirmId(null);
      })
      .catch(() => {
        setError(t('users.deleteError'));
        setConfirmId(null);
      });
  };

  const isAddValid = addForm.firstName && addForm.lastName && addForm.email && addForm.password && addForm.role;
  const isEditValid = editForm.firstName && editForm.lastName && editForm.email && editForm.role;

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5">{t('users.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAddOpen(true)}>
            {t('users.addUser')}
          </Button>
        </Box>

        {/* Role filter */}
        <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
          {['ALL', ...ROLES].map(r => (
            <Chip
              key={r}
              label={r}
              color={roleFilter === r ? 'primary' : 'default'}
              onClick={() => setRoleFilter(r)}
              clickable
            />
          ))}
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
                  <TableCell>{t('users.firstName')}</TableCell>
                  <TableCell>{t('users.lastName')}</TableCell>
                  <TableCell>{t('users.email')}</TableCell>
                  <TableCell>{t('users.role')}</TableCell>
                  <TableCell align="center">{t('schools.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filtered.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">{t('users.noUsers')}</TableCell>
                  </TableRow>
                ) : (
                  filtered.map(user => (
                    <TableRow key={user.id} hover onClick={() => handleRowClick(user)} sx={{ cursor: 'pointer' }}>
                      <TableCell>{user.id}</TableCell>
                      <TableCell>{user.firstName}</TableCell>
                      <TableCell>{user.lastName}</TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>
                        <Chip label={user.role} color={ROLE_COLORS[user.role] || 'default'} size="small" />
                      </TableCell>
                      <TableCell align="center">
                        <IconButton onClick={e => { e.stopPropagation(); setConfirmId(user.id); setConfirmName(`${user.firstName} ${user.lastName}`); }}>
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

      {/* Add User Dialog */}
      <Dialog open={addOpen} onClose={() => setAddOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('users.addUser')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <UserFormFields form={addForm} setForm={setAddForm} t={t} isEdit={false} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddOpen(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!isAddValid || saving}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit User Dialog */}
      <Dialog open={editOpen} onClose={() => setEditOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('users.editUser')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <UserFormFields form={editForm} setForm={setEditForm} t={t} isEdit={true} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditOpen(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={handleUpdate} disabled={!isEditValid || saving}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation */}
      <Dialog open={confirmId !== null} onClose={() => setConfirmId(null)}>
        <DialogTitle>{t('users.deleteConfirmTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('users.deleteConfirmMessage', { name: confirmName })}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmId(null)}>{t('common.cancel')}</Button>
          <Button variant="contained" color="error" onClick={handleDeleteConfirm}>{t('common.delete')}</Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}

export default ManageUsers;
