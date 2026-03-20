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

function ViewTeachers() {
  const { t } = useTranslation();
  const [teachers, setTeachers] = useState([]);
  const [schoolName, setSchoolName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get('/api/profile')
      .then(res => {
        const schoolId = res.data.schoolId;
        setSchoolName(res.data.schoolName || '');
        return api.get(`/api/users/teachers/school/${schoolId}`);
      })
      .then(res => setTeachers(res.data))
      .catch(() => setError(t('users.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Typography variant="h5" sx={{ mb: 1 }}>{t('nav.teachers')}</Typography>
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
                </TableRow>
              </TableHead>
              <TableBody>
                {teachers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={2} align="center">{t('users.noUsers')}</TableCell>
                  </TableRow>
                ) : (
                  teachers.map(t => (
                    <TableRow key={t.id} hover>
                      <TableCell>
                        <UserAvatar userId={t.id} name={t.name} size={36} />
                      </TableCell>
                      <TableCell>{t.name}</TableCell>
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

export default ViewTeachers;
