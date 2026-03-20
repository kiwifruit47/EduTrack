import React, { useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Divider,
  TextField, Typography,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';
import useAuth from '../../hooks/useAuth';

const fieldSx = {
  InputProps: { sx: { color: 'black' } },
  InputLabelProps: { shrink: true, sx: { color: 'black' } },
};

function Profile() {
  const { t } = useTranslation();
  const { user } = useAuth();

  const [form, setForm]       = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [saving, setSaving]   = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError]     = useState(null);

  const handleSubmit = () => {
    setError(null);
    setSuccess(false);

    if (form.newPassword !== form.confirmPassword) {
      setError(t('profile.passwordMismatch'));
      return;
    }

    setSaving(true);
    api.put('/api/profile/password', {
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    })
      .then(() => {
        setSuccess(true);
        setForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      })
      .catch(err => setError(err.response?.data?.message || t('profile.changeError')))
      .finally(() => setSaving(false));
  };

  return (
    <Layout>
      <Box sx={{ p: 3, maxWidth: 500 }}>
        <Typography variant="h5" sx={{ mb: 3 }}>{t('profile.title')}</Typography>

        {/* User info */}
        <Card sx={{ mb: 3 }}>
          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography><strong>{t('users.firstName')}:</strong> {user?.name?.split(' ')[0]}</Typography>
            <Typography><strong>{t('users.lastName')}:</strong> {user?.name?.split(' ').slice(1).join(' ')}</Typography>
            <Typography><strong>{t('users.email')}:</strong> {user?.email}</Typography>
            <Typography><strong>{t('users.role')}:</strong> {user?.role}</Typography>
          </CardContent>
        </Card>

        <Divider sx={{ mb: 3 }} />

        {/* Change password */}
        <Typography variant="h6" sx={{ mb: 2 }}>{t('profile.changePassword')}</Typography>

        {success && <Alert severity="success" sx={{ mb: 2 }}>{t('profile.changeSuccess')}</Alert>}
        {error   && <Alert severity="error"   sx={{ mb: 2 }}>{error}</Alert>}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label={t('profile.currentPassword')}
            type="password"
            value={form.currentPassword}
            onChange={e => setForm(f => ({ ...f, currentPassword: e.target.value }))}
            fullWidth
            {...fieldSx}
          />
          <TextField
            label={t('profile.newPassword')}
            type="password"
            value={form.newPassword}
            onChange={e => setForm(f => ({ ...f, newPassword: e.target.value }))}
            fullWidth
            {...fieldSx}
          />
          <TextField
            label={t('profile.confirmPassword')}
            type="password"
            value={form.confirmPassword}
            onChange={e => setForm(f => ({ ...f, confirmPassword: e.target.value }))}
            fullWidth
            {...fieldSx}
          />
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={!form.currentPassword || !form.newPassword || !form.confirmPassword || saving}
          >
            {t('profile.changePassword')}
          </Button>
        </Box>
      </Box>
    </Layout>
  );
}

export default Profile;
