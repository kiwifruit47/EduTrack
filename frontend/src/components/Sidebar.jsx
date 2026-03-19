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
Collapse,
ToggleButtonGroup,
ToggleButton,
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
import { useTranslation } from 'react-i18next';

const drawerWidth = 240;

const Sidebar = () => {
    const { user } = useAuth();
    const role = user?.role;
    const { t, i18n } = useTranslation();

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
            {key: "home", label: t('nav.home'), icon: <HomeIcon />, to: "/"},
            {
                key: "statistics",
                label: t('nav.statistics'),
                icon: <StatsIcon />,
                submenu: [
                    {key: "schoolStats", label: t('nav.schoolStatistics'), to: "/select/school/statistics"},
                    {key: "teacherStats", label: t('nav.teacherStatistics'), to: "/select/teacher/statistics"},
                    {key: "subjectStats", label: t('nav.subjectStatistics'), to: "/select/subject/statistics"}
                ]
            },
            {key: "manageUsers", label: t('nav.manageUsers'), icon: <ManageAccounts />, to: "/admin/manageUsers"},
            {key: "schedules", label: t('nav.schedules'), icon: <ScheduleIcon />, to: "select/class/schedule"},
            {key: "subjects", label: t('nav.subjects'), icon: <SubjectIcon />, to: "/admin/viewSubjects"},
            {key: "schools", label: t('nav.schools'), icon: <SchoolIcon />, to: "/admin/viewSchools"},
            {key: "grades", label: t('nav.grades'), icon: <Grade />, to: "/grades"},
            {key: "absences", label: t('nav.absences'), icon: <AbsenceIcon />, to: "/absences/"},
            {key: "complaints", label: t('nav.complaints'), icon: <ComplaintIcon />, to: "/complaints"},
            {key: "logout", label: t('nav.logout'), icon: <Logout />, to: "/login"}
        ],
        HEADMASTER : [
            {key: "home", label: t('nav.home'), icon: <HomeIcon />, to: "/"},
            {
                key: "statistics",
                label: t('nav.statistics'),
                icon: <StatsIcon />,
                submenu: [
                    {key: "schoolStats", label: t('nav.schoolStatistics'), to: "/statistics/:schoolId"},
                    {key: "teacherStats", label: t('nav.teacherStatistics'), to: "/statistics/:teacherId"},
                    {key: "subjectStats", label: t('nav.subjectStatistics'), to: "/statistics/:subjectId"}
                ]
            },
            {key: "schedules", label: t('nav.schedules'), icon: <ScheduleIcon />, to: "/select/class/schedules"},
            {key: "subjects", label: t('nav.subjects'), icon: <SubjectIcon />, to: "/admin/viewSubjects"},
            {key: "teachers", label: t('nav.teachers'), icon: <Teacher />, to: "/headmaster/viewTeachers"},
            {key: "parents", label: t('nav.parents'), icon: <ParentIcon />, to: "/headmaster/viewParents"},
            {key: "students", label: t('nav.students'), icon: <StudentIcon />, to: "/headmaster/viewStudents"},
            {key: "grades", label: t('nav.grades'), icon: <Grade />, to: "/grades/:schoolId"},
            {key: "absences", label: t('nav.absences'), icon: <AbsenceIcon />, to: "/absences/:schoolId"},
            {key: "complaints", label: t('nav.complaints'), icon: <ComplaintIcon />, to: "/complaints/:schoolId"},
            {key: "logout", label: t('nav.logout'), icon: <Logout />, to: "/login"}
        ],
        TEACHER : [
            {key: "home", label: t('nav.home'), icon: <HomeIcon />, to: "/"},
            {key: "schedule", label: t('nav.schedule'), icon: <ScheduleIcon />, to: "/teacher/teacherSchedule/:teacherId"},
            {key: "grades", label: t('nav.grades'), icon: <Grade />, to: "/select/class/grades"},
            {key: "absences", label: t('nav.absences'), icon: <AbsenceIcon />, to: "/select/class/absences"},
            {key: "complaints", label: t('nav.complaints'), icon: <ComplaintIcon />, to: "/select/class/complaints"},
            {key: "logout", label: t('nav.logout'), icon: <Logout />, to: "/login"}
        ],
        PARENT: [
            {key: "home", label: t('nav.home'), icon: <HomeIcon />, to: "/"},
            {key: "schedule", label: t('nav.schedule'), icon: <ScheduleIcon />, to: "/schedule/:classId"},
            {key: "grades", label: t('nav.grades'), icon: <Grade />, to: "/grades/:studentId"},
            {key: "absences", label: t('nav.absences'), icon: <AbsenceIcon />, to: "/absences/:studentId"},
            {key: "complaints", label: t('nav.complaints'), icon: <ComplaintIcon />, to: "/complaints/:studentId"},
            {
                key: "viewStudent",
                label: t('nav.viewStudent'),
                icon: <StudentIcon />,
                submenu: students.map(s => ({
                    label: s.name,
                    key: s.id,
                    to: "#",
                    onClick: () => setSelectedStudent(s)
                }))
            },
            {key: "logout", label: t('nav.logout'), icon: <Logout />, to: "/login"}
        ]
    }

    const drawerContent = (
    <>
        <img src={logo} alt="logo" style={{height: "15vh", width: "auto", margin: "20px"}}/>
        <List>
            {menuItems[user.role].map((item) => (
                <Box
                    key={item.key}
                    onMouseEnter={() => handleParentEnter(item.key)}
                    onMouseLeave={() => handleParentLeave(item.key)}
                >
                    <ListItem disablePadding>
                        <ListItemButton
                            component={item.to ? Link : "div"}
                            to={item.submenu ? undefined : item.to}
                            onClick={() => {
                                if (item.submenu) {
                                    handleParentClick(item.key, true);
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
                        <Collapse in={openMenu === item.key} timeout="auto" unmountOnExit>
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

        <Box sx={{ mt: 'auto', mb: 2, display: 'flex', justifyContent: 'center' }}>
            <ToggleButtonGroup
                value={i18n.language}
                exclusive
                size="small"
                onChange={(_, lng) => { if (lng) i18n.changeLanguage(lng); }}
            >
                <ToggleButton value="en">EN</ToggleButton>
                <ToggleButton value="bg">BG</ToggleButton>
            </ToggleButtonGroup>
        </Box>
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
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, backgroundColor: theme.palette.primary.main, color: theme.palette.primary.main, display: 'flex', flexDirection: 'column' },
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
