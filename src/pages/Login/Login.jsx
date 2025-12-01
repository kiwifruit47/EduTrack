import { Grid, Paper, TextField, Box, Button } from '@mui/material'
import React from 'react'
import logo from '../../../public/EduTrack_logo_positive.svg'
import useAuth from '../../hooks/useAuth'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'

function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    const result = await login(email, password);

    if (result.success) {
      navigate("/");
    } else {
      setError(result.error);
    }
  };

  const paperStyle = {
    bgcolor: 'primary.main', 
    padding: { xs: 3, sm: 4 },
    height: 'auto',
    minHeight: '60vh',
    width: { xs: '90%', sm: '60%', md: '30%', lg: 350 },
    maxWidth: 400,
    margin: "0 auto", 
    display: "flex", 
    flexDirection: "column", 
    alignItems: "center"
  }

  return (
    <Grid
      container
      justifyContent="center"
      alignItems="center"
      sx={{
        bgcolor: 'secondary.main', 
        width: '100vw', 
        minHeight: '100vh',
        padding: { xs: 2, sm: 4, md: 6 }
      }}
      >
      <Paper elevation={5} sx={paperStyle}>
        <img src={logo} alt="logo" style={{height: "15vh", width: "auto", margin: "20px"}}/>
        
        <Box
          component="form"
          autoComplete='off'
          sx={{
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center', 
            justifyContent: 'space-between', 
            width: '100%',
            gap: { xs: 3, sm: 4 },
            paddingTop: 1
          }}
          onSubmit={handleLogin}
        >
          <TextField
            required
            id="standard-required"
            label="Email Address"
            variant="standard"
            type="email"
            fullWidth
            InputLabelProps={{ shrink: true }}
            sx={{
              '& .MuiInput-root': {
                  color: 'secondary.main',
              }
            }}
            onChange={(e) => setEmail(e.target.value)}
          />

          <TextField
          required
          id="standard-password-input"
          label="Password"
          type="password"
          autoComplete="current-password"
          variant="standard"
          fullWidth
          InputLabelProps={{ shrink: true }}
          sx={{
            '& .MuiInput-root': {
                color: 'secondary.main',
            }
          }}
          onChange={(e) => setPassword(e.target.value)}
          />

          {error && (
            <Typography color="error" sx={{ mb: 2 }}>
              {error}
            </Typography>
          )}

          <Button color='secondary' fullWidth variant='contained' sx={{ mt: 2 }}>
            Sign In
          </Button>
        </Box>
        

      </Paper>
    </Grid>
  )
}

export default Login