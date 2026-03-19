import { createContext, useContext, useState } from 'react';

const SidebarContext = createContext();

export function SidebarProvider({ children }) {
  const [desktopOpen, setDesktopOpen] = useState(true);
  return (
    <SidebarContext.Provider value={{ desktopOpen, setDesktopOpen }}>
      {children}
    </SidebarContext.Provider>
  );
}

export function useSidebar() {
  return useContext(SidebarContext);
}
