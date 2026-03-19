import React, { useEffect, useState } from 'react';
import {
  Alert, Box, CircularProgress, Divider, Paper,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';

function TeacherSchedule() {
  const { t }        = useTranslation();
  const navigate     = useNavigate();

  const [entries, setEntries]   = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState(null);

  useEffect(() => {
    api.get('/api/schedules/teacher/me')
      .then(res => setEntries(res.data))
      .catch(() => setError(t('schedule.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  const term1 = entries.filter(e => e.term === 1);
  const term2 = entries.filter(e => e.term === 2);

  const TermSection = ({ termEntries }) => (
    <TableContainer component={Paper} sx={{ mb: 1 }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>{t('schedule.class')}</TableCell>
            <TableCell>{t('schedule.subject')}</TableCell>
            <TableCell>{t('schedule.school')}</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {termEntries.length === 0 ? (
            <TableRow>
              <TableCell colSpan={3} align="center">{t('schedule.noEntries')}</TableCell>
            </TableRow>
          ) : (
            termEntries.map(e => (
              <TableRow
                key={e.id}
                hover
                sx={{ cursor: 'pointer' }}
                onClick={() => navigate(`/schedule/${e.classId}`)}
              >
                <TableCell>{e.className}</TableCell>
                <TableCell>{e.subjectName}</TableCell>
                <TableCell>{e.schoolName}</TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Typography variant="h5" sx={{ mb: 3 }}>{t('schedule.mySchedule')}</Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Typography variant="h6" sx={{ mb: 1 }}>{t('schedule.term1')}</Typography>
            <TermSection termEntries={term1} />

            <Divider sx={{ my: 3 }} />

            <Typography variant="h6" sx={{ mb: 1 }}>{t('schedule.term2')}</Typography>
            <TermSection termEntries={term2} />
          </>
        )}
      </Box>
    </Layout>
  );
}

export default TeacherSchedule;
