import { AuthProvider } from './context/AuthProvider';
import AppRouter from './router/AppRouter';
import { ThemeProvider } from '@emotion/react';
import theme from './theme';

function App() {

  return (
    <>
    <ThemeProvider theme={theme}>
      <AuthProvider>
        <AppRouter/>
      </AuthProvider>
    </ThemeProvider>
    </>
  )
}

export default App
