import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider, FormControl,
  IconButton, InputLabel, List, ListItem, MenuItem, Paper, Select,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';

const ENTRY_TYPES = ['LECTURE', 'BREAK', 'SPECIAL_EVENT'];
const ENTRY_TYPE_COLORS = { LECTURE: 'primary', BREAK: 'success', SPECIAL_EVENT: 'warning' };
const emptyEntry = { type: 'LECTURE', label: '', startTime: '', endTime: '', eventDate: '' };

const fieldProps = {
  InputProps: { sx: { color: 'black' } },
  InputLabelProps: { shrink: true, sx: { color: 'black', '&.Mui-focused': { color: 'black' } } },
};

function HeadmasterSchool() {
  const { t } = useTranslation();

  const [school, setSchool]           = useState(null);
  const [entries, setEntries]         = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState(null);
  const [showForm, setShowForm]       = useState(false);
  const [form, setForm]               = useState(emptyEntry);
  const [editingId, setEditingId]     = useState(null);
  const [saveError, setSaveError]     = useState(null);

  useEffect(() => {
    api.get('/api/profile')
      .then(res => {
        const schoolId = res.data.schoolId;
        if (!schoolId) throw new Error('No school');
        return Promise.all([
          api.get(`/api/schools/${schoolId}`),
          api.get(`/api/schools/${schoolId}/schedule`),
        ]);
      })
      .then(([schoolRes, schedRes]) => {
        setSchool(schoolRes.data);
        setEntries(schedRes.data);
      })
      .catch(() => setError(t('schools.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  const openAdd = () => { setEditingId(null); setForm(emptyEntry); setSaveError(null); setShowForm(true); };
  const openEdit = (e) => {
    setEditingId(e.id);
    setForm({ type: e.type, label: e.label, startTime: e.startTime, endTime: e.endTime, eventDate: e.eventDate || '' });
    setSaveError(null);
    setShowForm(true);
  };
  const closeForm = () => { setShowForm(false); setEditingId(null); setForm(emptyEntry); };

  const handleSave = () => {
    if (!form.label.trim() || !form.startTime || !form.endTime) return;
    setSaveError(null);
    const req = editingId
      ? api.put(`/api/schools/schedule/${editingId}`, form)
      : api.post(`/api/schools/${school.id}/schedule`, form);
    req
      .then(res => {
        setEntries(prev =>
          editingId
            ? prev.map(e => e.id === editingId ? res.data : e)
            : [...prev, res.data]
        );
        closeForm();
      })
      .catch(() => setSaveError(t('schools.addScheduleEntry') + ' ' + t('schools.fetchError')));
  };

  const handleDelete = (id) => {
    api.delete(`/api/schools/schedule/${id}`)
      .then(() => setEntries(prev => prev.filter(e => e.id !== id)))
      .catch(() => setError(t('schools.deleteError')));
  };

  if (loading) {
    return (
      <Layout>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>
      </Layout>
    );
  }

  if (error) {
    return <Layout><Box sx={{ p: 3 }}><Alert severity="error">{error}</Alert></Box></Layout>;
  }

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        {school && (
          <>
            <Typography variant="h5" sx={{ mb: 0.5 }}>{school.name}</Typography>
            {school.address && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>{school.address}</Typography>
            )}
            {school.type && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {t(`schoolTypes.${school.type}`)}
              </Typography>
            )}
          </>
        )}

        <Divider sx={{ my: 2 }} />

        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Typography variant="h6">{t('schools.dailySchedule')}</Typography>
          <Button size="small" startIcon={<AddIcon />} onClick={openAdd}>
            {t('schools.addScheduleEntry')}
          </Button>
        </Box>

        {saveError && <Alert severity="error" sx={{ mb: 1 }}>{saveError}</Alert>}

        {entries.length === 0 && !showForm && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {t('schools.noScheduleEntries')}
          </Typography>
        )}

        <List dense disablePadding>
          {entries.map(e => (
            <ListItem
              key={e.id}
              disablePadding
              sx={{ mb: 0.5 }}
              secondaryAction={
                <Box>
                  <IconButton size="small" onClick={() => openEdit(e)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleDelete(e.id)}>
                    <DeleteIcon fontSize="small" sx={{ color: 'red' }} />
                  </IconButton>
                </Box>
              }
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flex: 1, pr: 10, py: 0.5 }}>
                <Chip
                  label={t(`entryTypes.${e.type}`)}
                  size="small"
                  color={ENTRY_TYPE_COLORS[e.type]}
                  sx={{ minWidth: 90 }}
                />
                <Typography variant="body2" sx={{ fontWeight: 500, flexShrink: 0 }}>{e.label}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {e.startTime}–{e.endTime}
                  {e.eventDate ? ` · ${e.eventDate}` : ''}
                </Typography>
              </Box>
            </ListItem>
          ))}
        </List>

        {showForm && (
          <Box sx={{ mt: 1, p: 1.5, border: '1px solid', borderColor: 'divider', borderRadius: 1, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            <FormControl size="small" fullWidth>
              <InputLabel sx={{ color: 'black', '&.Mui-focused': { color: 'black' } }}>{t('schools.entryType')}</InputLabel>
              <Select
                value={form.type}
                onChange={e => setForm(f => ({ ...f, type: e.target.value }))}
                label={t('schools.entryType')} sx={{ color: 'black' }}
              >
                {ENTRY_TYPES.map(type => (
                  <MenuItem key={type} value={type}>{t(`entryTypes.${type}`)}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              size="small" label={t('schools.entryLabel')} value={form.label}
              onChange={e => setForm(f => ({ ...f, label: e.target.value }))}
              fullWidth {...fieldProps}
            />

            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField
                size="small" label={t('schools.startTime')} type="time" value={form.startTime}
                onChange={e => setForm(f => ({ ...f, startTime: e.target.value }))}
                fullWidth {...fieldProps}
              />
              <TextField
                size="small" label={t('schools.endTime')} type="time" value={form.endTime}
                onChange={e => setForm(f => ({ ...f, endTime: e.target.value }))}
                fullWidth {...fieldProps}
              />
            </Box>

            {form.type === 'SPECIAL_EVENT' && (
              <TextField
                size="small" label={t('schools.eventDate')} type="date" value={form.eventDate}
                onChange={e => setForm(f => ({ ...f, eventDate: e.target.value }))}
                fullWidth {...fieldProps}
              />
            )}

            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
              <Button size="small" onClick={closeForm}>{t('common.cancel')}</Button>
              <Button
                size="small" variant="contained"
                disabled={!form.label.trim() || !form.startTime || !form.endTime}
                onClick={handleSave}
              >
                {t('common.save')}
              </Button>
            </Box>
          </Box>
        )}
      </Box>
    </Layout>
  );
}

export default HeadmasterSchool;
