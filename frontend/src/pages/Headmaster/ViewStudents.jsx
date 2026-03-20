import React, { useEffect, useState } from 'react';
import {
  Alert, Box, CircularProgress, Paper,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import UserAvatar from '../../components/UserAvatar';
import api from '../../api/axiosInstance';

function ViewStudents() {
  const { t } = useTranslation();
  const [students, setStudents] = useState([]);
  const [schoolName, setSchoolName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get('/api/profile')
      .then(res => {
        const schoolId = res.data.schoolId;
        setSchoolName(res.data.schoolName || '');
        return api.get(`/api/users/students/school/${schoolId}`);
      })
      .then(res => setStudents(res.data))
      .catch(() => setError(t('users.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Typography variant="h5" sx={{ mb: 1 }}>{t('nav.students')}</Typography>
        {schoolName && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>{schoolName}</Typography>
        )}

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
                  <TableCell>{t('users.email')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {students.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} align="center">{t('users.noUsers')}</TableCell>
                  </TableRow>
                ) : (
                  students.map(s => (
                    <TableRow key={s.id} hover>
                      <TableCell>
                        <UserAvatar userId={s.id} name={`${s.firstName} ${s.lastName}`} size={36} />
                      </TableCell>
                      <TableCell>{s.firstName} {s.lastName}</TableCell>
                      <TableCell>{s.email}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Box>
    </Layout>
  );
}

export default ViewStudents;
