import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, FormControl, IconButton, InputLabel,
  MenuItem, Paper, Select, Tab, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Tabs, TextField, Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';
import useAuth from '../../hooks/useAuth';

const fieldSx = {
  InputProps: { sx: { color: 'black' } },
  InputLabelProps: { shrink: true, sx: { color: 'black', '&.Mui-focused': { color: 'black' } } },
};

function ClassComplaints() {
  const { classId } = useParams();
  const { t } = useTranslation();
  const { user } = useAuth();
  const canEdit = ['ADMIN', 'HEADMASTER', 'TEACHER'].includes(user?.role);

  const [classInfo, setClassInfo]   = useState(null);
  const [complaints, setComplaints] = useState([]);
  const [schedules, setSchedules]   = useState([]);
  const [students, setStudents]     = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState(null);
  const [termTab, setTermTab]       = useState(0);

  const [addOpen, setAddOpen] = useState(false);
  const [addForm, setAddForm] = useState({ studentId: '', scheduleId: '', date: '', description: '' });
  const [saving, setSaving]   = useState(false);

  useEffect(() => {
    Promise.all([
      api.get(`/api/classes/${classId}`),
      api.get(`/api/complaints/class/${classId}`),
      api.get(`/api/schedules/class/${classId}`),
      api.get(`/api/classes/${classId}/students`),
    ])
      .then(([ci, cp, sc, st]) => {
        setClassInfo(ci.data);
        setComplaints(cp.data);
        setSchedules(sc.data);
        setStudents(st.data);
      })
      .catch(() => setError(t('complaints.fetchError')))
      .finally(() => setLoading(false));
  }, [classId]);

  const termFilter = termTab === 0 ? null : termTab;
  const filtered = termFilter
    ? complaints.filter(c => c.term === termFilter)
    : complaints;

  const handleAdd = () => {
    setSaving(true);
    api.post('/api/complaints', {
      studentId:   Number(addForm.studentId),
      scheduleId:  Number(addForm.scheduleId),
      date:        addForm.date,
      description: addForm.description,
    })
      .then(res => {
        setComplaints(prev => [...prev, res.data]);
        setAddOpen(false);
        setAddForm({ studentId: '', scheduleId: '', date: '', description: '' });
      })
      .catch(() => setError(t('complaints.createError')))
      .finally(() => setSaving(false));
  };

  const handleDelete = (id) => {
    api.delete(`/api/complaints/${id}`)
      .then(() => setComplaints(prev => prev.filter(c => c.id !== id)))
      .catch(() => setError(t('complaints.deleteError')));
  };

  const isAddValid = addForm.studentId && addForm.scheduleId && addForm.date && addForm.description.trim();

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>
        ) : (
          <>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
              <Box>
                <Typography variant="h5">
                  {t('complaints.title')} — {classInfo?.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">{classInfo?.schoolName}</Typography>
              </Box>
              {canEdit && (
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAddOpen(true)}>
                  {t('complaints.addComplaint')}
                </Button>
              )}
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Tabs value={termTab} onChange={(_, v) => setTermTab(v)} sx={{ mb: 1 }}>
              <Tab label={t('grades.all')} />
              <Tab label={t('schedule.term1')} />
              <Tab label={t('schedule.term2')} />
            </Tabs>

            <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
              <Chip label={`${t('complaints.total')}: ${filtered.length}`} />
            </Box>

            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>{t('complaints.student')}</TableCell>
                    <TableCell>{t('complaints.subject')}</TableCell>
                    <TableCell>{t('complaints.teacher')}</TableCell>
                    <TableCell>{t('complaints.term')}</TableCell>
                    <TableCell>{t('complaints.date')}</TableCell>
                    <TableCell>{t('complaints.description')}</TableCell>
                    {canEdit && <TableCell align="center">{t('schools.actions')}</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filtered.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={canEdit ? 7 : 6} align="center">{t('complaints.noComplaints')}</TableCell>
                    </TableRow>
                  ) : (
                    filtered.map(c => (
                      <TableRow key={c.id} hover>
                        <TableCell>{c.studentName}</TableCell>
                        <TableCell>{c.subjectName}</TableCell>
                        <TableCell>{c.teacherName}</TableCell>
                        <TableCell>{c.term}</TableCell>
                        <TableCell>{c.date}</TableCell>
                        <TableCell>{c.description}</TableCell>
                        {canEdit && (
                          <TableCell align="center">
                            <IconButton size="small" onClick={() => handleDelete(c.id)}>
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
          </>
        )}
      </Box>

      <Dialog open={addOpen} onClose={() => setAddOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('complaints.addComplaint')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <FormControl fullWidth>
            <InputLabel sx={{ color: 'black', '&.Mui-focused': { color: 'black' } }}>{t('complaints.student')}</InputLabel>
            <Select value={addForm.studentId} onChange={e => setAddForm(f => ({ ...f, studentId: e.target.value }))}
              label={t('complaints.student')} sx={{ color: 'black' }}>
              {students.map(s => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel sx={{ color: 'black', '&.Mui-focused': { color: 'black' } }}>{t('complaints.subject')}</InputLabel>
            <Select value={addForm.scheduleId} onChange={e => setAddForm(f => ({ ...f, scheduleId: e.target.value }))}
              label={t('complaints.subject')} sx={{ color: 'black' }}>
              {schedules.map(s => (
                <MenuItem key={s.id} value={s.id}>
                  {s.subjectName} — {t('schedule.term')} {s.term}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label={t('complaints.date')} type="date" value={addForm.date}
            onChange={e => setAddForm(f => ({ ...f, date: e.target.value }))}
            fullWidth {...fieldSx}
          />
          <TextField
            label={t('complaints.description')} value={addForm.description}
            onChange={e => setAddForm(f => ({ ...f, description: e.target.value }))}
            multiline rows={3} fullWidth {...fieldSx}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddOpen(false)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={handleAdd} disabled={!isAddValid || saving}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}

export default ClassComplaints;
