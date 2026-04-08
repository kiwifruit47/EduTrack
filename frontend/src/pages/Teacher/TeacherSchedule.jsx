import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert, Box, Chip, CircularProgress, Paper,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Layout from '../../components/Layout';
import api from '../../api/axiosInstance';

// ── Term-activity helpers ─────────────────────────────────────────────────────

const DEFAULT_CONFIG = {
  startDate:       '09-15',
  term2Start:      '02-01',
  elementaryEnd:   '06-01',
  progymnasiumEnd: '06-15',
  gymnasiumEnd:    '07-01',
};

/** Parse a "MM-dd" string into a Date for the given year. */
const parseMMDD = (mmdd, year) => {
  const [m, d] = mmdd.split('-').map(Number);
  return new Date(year, m - 1, d);
};

/**
 * Return the calendar year in which the current school year STARTED.
 * School years straddle two calendar years (e.g. 2024/2025 starts in 2024).
 */
const schoolYearStartYear = (today, startDate) => {
  const [m, d] = startDate.split('-').map(Number);
  const y = today.getFullYear();
  return today >= new Date(y, m - 1, d) ? y : y - 1;
};

/**
 * Return 1, 2, or null (null = not in session).
 * term2Start and end dates are always in startYear+1 (the "spring" half).
 */
const getActiveTerm = (today, cfg) => {
  const sy = schoolYearStartYear(today, cfg.startDate);
  const start = parseMMDD(cfg.startDate, sy);
  const t2    = parseMMDD(cfg.term2Start, sy + 1);
  if (today < start) return null;
  if (today < t2)    return 1;
  return 2;
};

/** True if the entry belongs to the active term and its grade level hasn't ended yet. */
const isEntryActive = (entry, today, cfg, activeTerm) => {
  if (activeTerm === null || entry.term !== activeTerm) return false;
  const grade = parseInt(entry.className.match(/^(\d+)/)?.[1] ?? '0');
  const endStr = grade <= 4 ? cfg.elementaryEnd
               : grade <= 7 ? cfg.progymnasiumEnd
               : cfg.gymnasiumEnd;
  const sy  = schoolYearStartYear(today, cfg.startDate);
  const end = parseMMDD(endStr, sy + 1);
  return today <= end;
};

// ── Component ─────────────────────────────────────────────────────────────────

function TeacherSchedule() {
  const { t }    = useTranslation();
  const navigate = useNavigate();

  const [entries, setEntries] = useState([]);
  const [config,  setConfig]  = useState(DEFAULT_CONFIG);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    Promise.all([
      api.get('/api/schedules/teacher/me'),
      api.get('/api/profile'),
    ])
      .then(([schedRes, profileRes]) => {
        setEntries(schedRes.data);
        const schoolId = profileRes.data.schoolId;
        if (schoolId) {
          return api.get(`/api/schools/${schoolId}/term-config`)
            .then(res => setConfig(res.data))
            .catch(() => {}); // keep defaults on error
        }
      })
      .catch(() => setError(t('schedule.fetchError')))
      .finally(() => setLoading(false));
  }, []);

  const today      = useMemo(() => new Date(), []);
  const activeTerm = useMemo(() => getActiveTerm(today, config), [today, config]);

  const activeEntries = useMemo(
    () => entries.filter(e => isEntryActive(e, today, config, activeTerm)),
    [entries, today, config, activeTerm]
  );

  const termLabel = activeTerm === 1 ? t('schedule.term1')
                  : activeTerm === 2 ? t('schedule.term2')
                  : null;

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <Typography variant="h5">{t('schedule.mySchedule')}</Typography>
          {termLabel && (
            <Chip label={termLabel} color="success" size="small" />
          )}
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        ) : activeTerm === null ? (
          <Typography color="text.secondary">{t('schedule.notInSession')}</Typography>
        ) : activeEntries.length === 0 ? (
          <Typography color="text.secondary">{t('schedule.noEntries')}</Typography>
        ) : (
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{t('schedule.class')}</TableCell>
                  <TableCell>{t('schedule.subject')}</TableCell>
                  <TableCell>{t('schedule.school')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {activeEntries.map(e => (
                  <TableRow
                    key={e.id}
                    hover
                    sx={{ cursor: 'pointer', bgcolor: '#f1f8e9' }}
                    onClick={() => navigate(`/schedule/${e.classId}`)}
                  >
                    <TableCell sx={{ color: 'success.dark', fontWeight: 500 }}>{e.className}</TableCell>
                    <TableCell sx={{ color: 'success.dark' }}>{e.subjectName}</TableCell>
                    <TableCell sx={{ color: 'success.dark' }}>{e.schoolName}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Box>
    </Layout>
  );
}

export default TeacherSchedule;
