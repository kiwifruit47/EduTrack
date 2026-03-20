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

const GRADE_COLOR = v => {
  const n = parseFloat(v);
  if (n >= 5.5) return 'success';
  if (n >= 4.5) return 'primary';
  if (n >= 3.5) return 'warning';
  return 'error';
};

function ClassGrades() {
  const { classId } = useParams();
  const { t } = useTranslation();
  const { user } = useAuth();
  const canEdit = ['ADMIN', 'HEADMASTER', 'TEACHER'].includes(user?.role);

  const [classInfo, setClassInfo]   = useState(null);
  const [grades, setGrades]         = useState([]);
  const [schedules, setSchedules]   = useState([]);
  const [students, setStudents]     = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState(null);
  const [termTab, setTermTab]       = useState(0); // 0=All, 1=Term1, 2=Term2

  const [addOpen, setAddOpen]       = useState(false);
  const [addForm, setAddForm]       = useState({ studentId: '', scheduleId: '', value: '' });
  const [saving, setSaving]         = useState(false);

  useEffect(() => {
    Promise.all([
      api.get(`/api/classes/${classId}`),
      api.get(`/api/grades/class/${classId}`),
      api.get(`/api/schedules/class/${classId}`),
      api.get(`/api/classes/${classId}/students`),
    ])
      .then(([ci, gr, sc, st]) => {
        setClassInfo(ci.data);
        setGrades(gr.data);
        setSchedules(sc.data);
        setStudents(st.data);
      })
      .catch(() => setError(t('grades.fetchError')))
      .finally(() => setLoading(false));
  }, [classId]);

  const termFilter = termTab === 0 ? null : termTab;
  const filtered = termFilter ? grades.filter(g => g.term === termFilter) : grades;

  const handleAdd = () => {
    setSaving(true);
    api.post('/api/grades', {
      studentId: Number(addForm.studentId),
      scheduleId: Number(addForm.scheduleId),
      value: parseFloat(addForm.value),
    })
      .then(res => {
        setGrades(prev => [...prev, res.data]);
        setAddOpen(false);
        setAddForm({ studentId: '', scheduleId: '', value: '' });
      })
      .catch(() => setError(t('grades.createError')))
      .finally(() => setSaving(false));
  };

  const handleDelete = (id) => {
    api.delete(`/api/grades/${id}`)
      .then(() => setGrades(prev => prev.filter(g => g.id !== id)))
      .catch(() => setError(t('grades.deleteError')));
  };

  const isAddValid = addForm.studentId && addForm.scheduleId
    && addForm.value >= 2 && addForm.value <= 6;

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
                  {t('grades.title')} — {classInfo?.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">{classInfo?.schoolName}</Typography>
              </Box>
              {canEdit && (
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAddOpen(true)}>
                  {t('grades.addGrade')}
                </Button>
              )}
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Tabs value={termTab} onChange={(_, v) => setTermTab(v)} sx={{ mb: 2 }}>
              <Tab label={t('grades.all')} />
              <Tab label={t('schedule.term1')} />
              <Tab label={t('schedule.term2')} />
            </Tabs>

            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>{t('grades.student')}</TableCell>
                    <TableCell>{t('grades.subject')}</TableCell>
                    <TableCell>{t('grades.teacher')}</TableCell>
                    <TableCell>{t('grades.term')}</TableCell>
                    <TableCell>{t('grades.value')}</TableCell>
                    <TableCell>{t('grades.date')}</TableCell>
                    {canEdit && <TableCell align="center">{t('schools.actions')}</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filtered.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={canEdit ? 7 : 6} align="center">{t('grades.noGrades')}</TableCell>
                    </TableRow>
                  ) : (
                    filtered.map(g => (
                      <TableRow key={g.id} hover>
                        <TableCell>{g.studentName}</TableCell>
                        <TableCell>{g.subjectName}</TableCell>
                        <TableCell>{g.teacherName}</TableCell>
                        <TableCell>{g.term}</TableCell>
                        <TableCell>
                          <Chip label={g.value} size="small" color={GRADE_COLOR(g.value)} />
                        </TableCell>
                        <TableCell>{g.createdAt?.slice(0, 10)}</TableCell>
                        {canEdit && (
                          <TableCell align="center">
                            <IconButton size="small" onClick={() => handleDelete(g.id)}>
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
        <DialogTitle>{t('grades.addGrade')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <FormControl fullWidth>
            <InputLabel sx={{ color: 'black', '&.Mui-focused': { color: 'black' } }}>{t('grades.student')}</InputLabel>
            <Select value={addForm.studentId} onChange={e => setAddForm(f => ({ ...f, studentId: e.target.value }))}
              label={t('grades.student')} sx={{ color: 'black' }}>
              {students.map(s => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel sx={{ color: 'black', '&.Mui-focused': { color: 'black' } }}>{t('grades.subject')}</InputLabel>
            <Select value={addForm.scheduleId} onChange={e => setAddForm(f => ({ ...f, scheduleId: e.target.value }))}
              label={t('grades.subject')} sx={{ color: 'black' }}>
              {schedules.map(s => (
                <MenuItem key={s.id} value={s.id}>
                  {s.subjectName} — {t('schedule.term')} {s.term}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label={t('grades.gradeValue')}
            type="number"
            inputProps={{ min: 2, max: 6, step: 0.5 }}
            value={addForm.value}
            onChange={e => setAddForm(f => ({ ...f, value: e.target.value }))}
            fullWidth {...fieldSx}
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

export default ClassGrades;
