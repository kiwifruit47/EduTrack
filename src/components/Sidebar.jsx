import React, { useState } from 'react';
import {
Box,
Drawer,
AppBar,
Toolbar,
List,
IconButton,
ListItem,
ListItemButton,
ListItemIcon,
ListItemText,
useTheme,
Collapse
} from '@mui/material';
import {
Menu as MenuIcon,
Home as HomeIcon,
BarChart as StatsIcon,
ManageAccounts,
CalendarMonth as ScheduleIcon,
Book as SubjectIcon,
Business as SchoolIcon,
Grade,
EventBusy as AbsenceIcon,
Report as ComplaintIcon,
Logout,
Work as Teacher,
EscalatorWarning as ParentIcon,
School as StudentIcon

} from '@mui/icons-material';

import logo from '../../public/EduTrack_logo_positive.svg';
import useAuth from '../hooks/useAuth';
import { useMediaQuery } from "@mui/material";
import { Link } from 'react-router-dom';
import { useStudent } from '../context/StudentContext';

const drawerWidth = 240;

const Sidebar = () => {
    const { user } = useAuth();
    const role = user?.role;

    const { students, setSelectedStudent, isParent } = useStudent();

    const [mobileOpen, setMobileOpen] = useState(false);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down("md"));

    const [openMenu, setOpenMenu] = useState(null);

    const handleParentEnter = (label) => {
        if (!isMobile) setOpenMenu(label);
    };

    const handleParentLeave = (label) => {
        if (!isMobile) setOpenMenu(null);
    };

    const handleParentClick = (label, hasSubmenu) => {
        if (isMobile && hasSubmenu) {
            setOpenMenu(openMenu === label ? null : label);
        }
    };

    const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
    };

    const menuItems = {
        ADMIN : [
            {label: "Home", icon: <HomeIcon />, to: "/"},
            {
                label: "Statistics", 
                icon: <StatsIcon />, 
                submenu: [
                    {label: "School Statistics", to: "/select/school/statistics"},
                    {label: "Teacher Statistics", to: "/select/teacher/statistics"},
                    {label: "Subject Statistics", to: "/select/subject/statistics"}
                ]
            },
            {label: "Manage users", icon: <ManageAccounts />, to: "/admin/manageUsers"},
            {label: "Schedules", icon: <ScheduleIcon />, to: "select/class/schedule"},
            {label: "Subjects", icon: <SubjectIcon />, to: "/admin/viewSubjects"},
            {label: "Schools", icon: <SchoolIcon />, to: "/admin/viewSchools"},
            {label: "Grades", icon: <Grade />, to: "/grades"},
            {label: "Absences", icon: <AbsenceIcon />, to: "/absences/"},
            {label: "Complaints", icon: <ComplaintIcon />, to: "/complaints"},
            {label: "Log out", icon: <Logout />, to: "/login"}
        ],
        HEADMASTER : [
            {label: "Home", icon: <HomeIcon />, to: "/"},
            {
                label: "Statistics", 
                icon: <StatsIcon />, 
                submenu: [
                    {label: "School Statistics", to: "/statistics/:schoolId"},
                    {label: "Teacher Statistics", to: "/statistics/:teacherId"},
                    {label: "Subject Statistics", to: "/statistics/:subjectId"}
                ]
            },
            {label: "Schedules", icon: <ScheduleIcon />, to: "/select/class/schedules"},
            {label: "Subjects", icon: <SubjectIcon />, to: "/admin/viewSubjects"},
            {label: "Teachers", icon: <Teacher />, to: "/headmaster/viewTeachers"},
            {label: "Parents", icon: <ParentIcon />, to: "/headmaster/viewParents"},
            {label: "Students", icon: <StudentIcon />, to: "/headmaster/viewStudents"},
            {label: "Grades", icon: <Grade />, to: "/grades/:schoolId"},
            {label: "Absences", icon: <AbsenceIcon />, to: "/absences/:schoolId"},
            {label: "Complaints", icon: <ComplaintIcon />, to: "/complaints/:schoolId"},
            {label: "Log out", icon: <Logout />, to: "/login"}
        ],
        TEACHER : [
            {label: "Home", icon: <HomeIcon />, to: "/"},
            {label: "Schedule", icon: <ScheduleIcon />, to: "/teacher/teacherSchedule/:teacherId"},
            {label: "Grades", icon: <Grade />, to: "/select/class/grades"},
            {label: "Absences", icon: <AbsenceIcon />, to: "/select/class/absences"},
            {label: "Complaints", icon: <ComplaintIcon />, to: "/select/class/complaints"},
            {label: "Log out", icon: <Logout />, to: "/login"}
        ],
        PARENT: [
            {label: "Home", icon: <HomeIcon />, to: "/"},
            {label: "Schedule", icon: <ScheduleIcon />, to: "/schedule/:classId"},
            {label: "Grades", icon: <Grade />, to: "/grades/:studentId"},
            {label: "Absences", icon: <AbsenceIcon />, to: "/absences/:studentId"},
            {label: "Complaints", icon: <ComplaintIcon />, to: "/complaints/:studentId"},
            {
                label: "View Student",
                icon: <StudentIcon />,
                submenu: students.map(s => ({
                    label: s.name,
                    key: s.id,
                    to: "#",
                    onClick: () => setSelectedStudent(s)
                }))
            },
            {label: "Log out", icon: <Logout />, to: "/login"}
        ]
    }

    const drawerContent = (
    <>
        <img src={logo} alt="logo" style={{height: "15vh", width: "auto", margin: "20px"}}/>
        <List>
            {menuItems[user.role].map((item) => (
                <Box
                    onMouseEnter={() => handleParentEnter(item.label)}
                    onMouseLeave={() => handleParentLeave(item.label)}
                >
                    <ListItem disablePadding>
                        <ListItemButton
                            component={item.to ? Link : "div"}
                            to={item.submenu ? undefined : item.to}
                            onClick={() => {
                                if (item.submenu) {
                                    handleParentClick(item.label, true);
                                } else {
                                    if (isMobile) {
                                        setMobileOpen(false);
                                    }
                                }
                            }}
                        >
                            <ListItemIcon>{item.icon}</ListItemIcon>
                            <ListItemText primary={item.label} />
                        </ListItemButton>
                    </ListItem>
                
                    {/* SUBMENU */}
                    {item.submenu && (
                        <Collapse in={openMenu === item.label} timeout="auto" unmountOnExit>
                            <List component="div" disablePadding>
                                {item.submenu.map((sub) => (
                                    <ListItem key={sub.key || sub.label} disablePadding sx={{ pl: 4 }}>
                                        <ListItemButton
                                            component={sub.to ? Link : "button"}
                                            to={sub.to}
                                            onClick={() => {
                                                sub.onClick?.();
                                                if (isMobile) {
                                                    setMobileOpen(false);
                                                    setOpenMenu(null);
                                                }
                                            }}
                                        >
                                            <ListItemText primary={sub.label} />
                                        </ListItemButton>
                                    </ListItem>
                    ))}
        </List>
    </Collapse>
)}

                </Box>            
            ))}
        </List>
    </>
    );

    return (
    <Box sx={{ display: 'flex' }}>
        <AppBar
        position="fixed"
        sx={{
            display: { xs: 'block', md: 'none' },
            width: { md: `calc(100% - ${drawerWidth}px)` },
            ml: { md: `${drawerWidth}px` },
        }}
        >
        <Toolbar>
            <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { md: 'none' } }}
            >
            <MenuIcon />
            </IconButton>
        </Toolbar>
        </AppBar>
        
        {/* Mobile drawer (temporary) */}
        <Box
        component="nav"
        sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
        >
        <Drawer
            variant="temporary"
            open={mobileOpen}
            onClose={handleDrawerToggle}
            ModalProps={{
            keepMounted: true,
            }}
            sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, backgroundColor: theme.palette.primary.main, color: theme.palette.primary.main },
            }}
        >
            {drawerContent}
        </Drawer>
        
        {/* Desktop drawer (permanent) */}
        <Drawer
            variant="permanent"
            sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, backgroundColor: theme.palette.primary.main, color: theme.palette.primary.main },
            }}
            open
        >
            {drawerContent}
        </Drawer>
        </Box>
    </Box>
    );
};

export default Sidebar;