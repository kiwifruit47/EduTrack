import React from 'react';
import { Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import useAuth from '../hooks/useAuth';

function WelcomeBanner() {
  const { t } = useTranslation();
  const { user } = useAuth();

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
      <Typography variant="h4">
        {t(`welcome.${user.role}`, { name: user.name })}
      </Typography>
    </Box>
  );
}

export default WelcomeBanner;
