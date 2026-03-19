import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, Divider, FormControl, IconButton,
  InputLabel, MenuItem, Paper, Select, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';
import useAuth from '../../hooks/useAuth';

const selectSx = { color: 'black' };
const labelSx  = { color: 'black' };

function TermTable({ entries, canEdit, onDelete, t }) {
  return (
    <TableContainer component={Paper} sx={{ mb: 1 }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>{t('schedule.subject')}</TableCell>
            <TableCell>{t('schedule.teacher')}</TableCell>
            {canEdit && <TableCell align="center" width={60} />}
          </TableRow>
        </TableHead>
        <TableBody>
          {entries.length === 0 ? (
            <TableRow>
              <TableCell colSpan={canEdit ? 3 : 2} align="center">
                {t('schedule.noEntries')}
              </TableCell>
            </TableRow>
          ) : (
            entries.map(e => (
              <TableRow key={e.id} hover>
                <TableCell>{e.subjectName}</TableCell>
                <TableCell>{e.teacherName}</TableCell>
                {canEdit && (
                  <TableCell align="center">
                    <IconButton size="small" onClick={() => onDelete(e.id)}>
                      <DeleteIcon fontSize="small" sx={{ color: 'red' }} />
                    </IconButton>
                  </TableCell>
                )}
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

function Schedule() {
  const { t }              = useTranslation();
  const { classId }        = useParams();
  const { user }           = useAuth();
  const canEdit            = user?.role === 'ADMIN' || user?.role === 'HEADMASTER';

  const [classInfo, setClassInfo]   = useState(null);
  const [entries, setEntries]       = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState(null);

  // Add dialog
  const [dialogOpen, setDialogOpen] = useState(false);
  const [subjects, setSubjects]     = useState([]);
  const [teachers, setTeachers]     = useState([]);
  const [form, setForm]             = useState({ subjectId: '', teacherId: '', term: '' });
  const [saving, setSaving]         = useState(false);

  useEffect(() => {
    Promise.all([
      api.get(`/api/classes/${classId}`),
      api.get(`/api/schedules/class/${classId}`),
    ])
      .then(([clsRes, schedRes]) => {
        setClassInfo(clsRes.data);
        setEntries(schedRes.data);
      })
      .catch(() => setError(t('schedule.fetchError')))
      .finally(() => setLoading(false));
  }, [classId]);

  const openAddDialog = () => {
    if (!classInfo) return;
    Promise.all([
      api.get('/api/subjects'),
      api.get(`/api/users/teachers/school/${classInfo.schoolId}`),
    ]).then(([subRes, teachRes]) => {
      setSubjects(subRes.data);
      setTeachers(teachRes.data);
      setDialogOpen(true);
    });
  };

  const handleAdd = () => {
    setSaving(true);
    api.post('/api/schedules', {
      classId:   Number(classId),
      subjectId: Number(form.subjectId),
      teacherId: Number(form.teacherId),
      term:      Number(form.term),
    })
      .then(res => {
        setEntries(prev => [...prev, res.data]);
        setDialogOpen(false);
        setForm({ subjectId: '', teacherId: '', term: '' });
      })
      .catch(() => setError(t('schedule.createError')))
      .finally(() => setSaving(false));
  };

  const handleDelete = (id) => {
    api.delete(`/api/schedules/${id}`)
      .then(() => setEntries(prev => prev.filter(e => e.id !== id)))
      .catch(() => setError(t('schedule.deleteError')));
  };

  const term1 = entries.filter(e => e.term === 1);
  const term2 = entries.filter(e => e.term === 2);

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Box>
                <Typography variant="h5">
                  {t('schedule.title')} — {classInfo?.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {classInfo?.schoolName} · {classInfo?.schoolYear}
                </Typography>
              </Box>
              {canEdit && (
                <Button variant="contained" startIcon={<AddIcon />} onClick={openAddDialog}>
                  {t('schedule.addEntry')}
                </Button>
              )}
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Typography variant="h6" sx={{ mb: 1 }}>{t('schedule.term1')}</Typography>
            <TermTable entries={term1} canEdit={canEdit} onDelete={handleDelete} t={t} />

            <Divider sx={{ my: 3 }} />

            <Typography variant="h6" sx={{ mb: 1 }}>{t('schedule.term2')}</Typography>
            <TermTable entries={term2} canEdit={canEdit} onDelete={handleDelete} t={t} />
          </>
        )}
      </Box>

      {/* Add Entry Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('schedule.addEntry')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <FormControl fullWidth required>
            <InputLabel sx={labelSx}>{t('schedule.subject')}</InputLabel>
            <Select value={form.subjectId} onChange={e => setForm(f => ({ ...f, subjectId: e.target.value }))}
              label={t('schedule.subject')} sx={selectSx}>
              {subjects.map(s => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl fullWidth required>
            <InputLabel sx={labelSx}>{t('schedule.teacher')}</InputLabel>
            <Select value={form.teacherId} onChange={e => setForm(f => ({ ...f, teacherId: e.target.value }))}
              label={t('schedule.teacher')} sx={selectSx}>
              {teachers.map(t => <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl fullWidth required>
            <InputLabel sx={labelSx}>{t('schedule.term')}</InputLabel>
            <Select value={form.term} onChange={e => setForm(f => ({ ...f, term: e.target.value }))}
              label={t('schedule.term')} sx={selectSx}>
              <MenuItem value={1}>{t('schedule.term1')}</MenuItem>
              <MenuItem value={2}>{t('schedule.term2')}</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={handleAdd}
            disabled={!form.subjectId || !form.teacherId || !form.term || saving}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}

export default Schedule;
