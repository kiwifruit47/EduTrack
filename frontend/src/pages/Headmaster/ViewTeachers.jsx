import React, { useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, IconButton, List, ListItem, ListItemText, Paper,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Typography,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import UserAvatar from '../../components/UserAvatar';
import api from '../../api/axiosInstance';

function ViewTeachers() {
  const { t } = useTranslation();

  const [teachers,    setTeachers]    = useState([]);
  const [schoolId,    setSchoolId]    = useState(null);
  const [schoolName,  setSchoolName]  = useState('');
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState(null);

  // Hire dialog
  const [hireOpen,     setHireOpen]     = useState(false);
  const [available,    setAvailable]    = useState([]);
  const [availLoading, setAvailLoading] = useState(false);
  const [availError,   setAvailError]   = useState(null);
  const [hireError,    setHireError]    = useState(null);

  // Fire dialog
  const [fireTarget, setFireTarget] = useState(null); // { id, name }
  const [fireError,  setFireError]  = useState(null);

  useEffect(() => {
    api.get('/api/profile')
      .then(res => {
        const sid = res.data.schoolId;
        setSchoolId(sid);
        setSchoolName(res.data.schoolName || '');
        return api.get(`/api/users/teachers/school/${sid}`);
      })
      .then(res => setTeachers(res.data))
      .catch(() => setError(t('users.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  // ── Hire ──────────────────────────────────────────────────────────────────

  const openHire = () => {
    setHireOpen(true);
    setAvailError(null);
    setHireError(null);
    setAvailLoading(true);
    api.get('/api/teachers/available')
      .then(res => setAvailable(res.data))
      .catch(() => setAvailError(t('teachers.fetchAvailableError')))
      .finally(() => setAvailLoading(false));
  };

  const handleHire = (userId, name) => {
    setHireError(null);
    api.post(`/api/teachers/${userId}/hire`)
      .then(() => {
        setAvailable(prev => prev.filter(u => u.id !== userId));
        setTeachers(prev => [...prev, { id: userId, name }]);
      })
      .catch(() => setHireError(t('teachers.hireError')));
  };

  // ── Fire ──────────────────────────────────────────────────────────────────

  const handleFire = () => {
    if (!fireTarget) return;
    setFireError(null);
    api.delete(`/api/teachers/${fireTarget.id}/fire`)
      .then(() => {
        setTeachers(prev => prev.filter(t => t.id !== fireTarget.id));
        setFireTarget(null);
      })
      .catch(() => setFireError(t('teachers.fireError')));
  };

  // ─────────────────────────────────────────────────────────────────────────

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Box>
            <Typography variant="h5">{t('nav.teachers')}</Typography>
            {schoolName && (
              <Typography variant="body2" color="text.secondary">{schoolName}</Typography>
            )}
          </Box>
          <Button size="small" startIcon={<PersonAddIcon />} onClick={openHire}>
            {t('teachers.hire')}
          </Button>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>
        ) : (
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ width: 56 }} />
                  <TableCell>{t('users.firstName')} {t('users.lastName')}</TableCell>
                  <TableCell sx={{ width: 56 }} />
                </TableRow>
              </TableHead>
              <TableBody>
                {teachers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} align="center">{t('users.noUsers')}</TableCell>
                  </TableRow>
                ) : (
                  teachers.map(teacher => (
                    <TableRow key={teacher.id} hover>
                      <TableCell>
                        <UserAvatar userId={teacher.id} name={teacher.name} size={36} />
                      </TableCell>
                      <TableCell>{teacher.name}</TableCell>
                      <TableCell>
                        <IconButton
                          size="small"
                          title={t('teachers.fire')}
                          onClick={() => { setFireTarget({ id: teacher.id, name: teacher.name }); setFireError(null); }}
                        >
                          <BlockIcon fontSize="small" sx={{ color: 'error.main' }} />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {/* ── Hire dialog ────────────────────────────────────────────────── */}
        <Dialog open={hireOpen} onClose={() => setHireOpen(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{t('teachers.hireTitle')}</DialogTitle>
          <DialogContent>
            {hireError  && <Alert severity="error"   sx={{ mb: 1 }}>{hireError}</Alert>}
            {availError && <Alert severity="error"   sx={{ mb: 1 }}>{availError}</Alert>}
            {availLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}><CircularProgress /></Box>
            ) : available.length === 0 ? (
              <Typography variant="body2" color="text.secondary">{t('teachers.noAvailable')}</Typography>
            ) : (
              <List dense disablePadding>
                {available.map(u => (
                  <ListItem
                    key={u.id}
                    disablePadding
                    sx={{ mb: 0.5 }}
                    secondaryAction={
                      <Button size="small" variant="contained" onClick={() => handleHire(u.id, `${u.firstName} ${u.lastName}`)}>
                        {t('teachers.hire')}
                      </Button>
                    }
                  >
                    <ListItemText primary={`${u.firstName} ${u.lastName}`} secondary={u.email} />
                  </ListItem>
                ))}
              </List>
            )}
          </DialogContent>
        </Dialog>

        {/* ── Fire confirmation dialog ────────────────────────────────────── */}
        <Dialog open={!!fireTarget} onClose={() => setFireTarget(null)} maxWidth="xs" fullWidth>
          <DialogTitle>{t('teachers.fireTitle')}</DialogTitle>
          <DialogContent>
            {fireError && <Alert severity="error" sx={{ mb: 1 }}>{fireError}</Alert>}
            <Typography sx={{ mb: 2 }}>
              {t('teachers.fireConfirm', { name: fireTarget?.name })}
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
              <Button size="small" onClick={() => setFireTarget(null)}>{t('common.cancel')}</Button>
              <Button size="small" variant="contained" color="error" onClick={handleFire}>
                {t('teachers.fire')}
              </Button>
            </Box>
          </DialogContent>
        </Dialog>
      </Box>
    </Layout>
  );
}

export default ViewTeachers;
