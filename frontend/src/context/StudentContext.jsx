import { createContext, useContext, useState, useEffect } from "react";
import useAuth from "../hooks/useAuth";

const StudentContext = createContext();

// for parent view - parent can change the student they want to view. the student with name which is alphabetically first is being provided prior to action from the user's side

export function StudentProvider({ children }) {
    const { user } = useAuth();
    const isParent = user?.role === "PARENT";

    const [students, setStudents] = useState([]);
    const [selectedStudent, setSelectedStudent] = useState(null);

    useEffect(() => {
        if (!isParent) {
            setStudents([]);
            setSelectedStudent(null);
            return;
        }

        async function load() {
            const res = await fetch("/parent/students", { credentials: "include" });
            const data = await res.json();

            if (Array.isArray(data) && data.length > 0) {
                const sorted = data.sort((a, b) => a.name.localeCompare(b.name));
                setStudents(sorted);
                setSelectedStudent(sorted[0]);
            }
        }

        load();
    }, [isParent]);

    return (
        <StudentContext.Provider 
            value={{ students, selectedStudent, setSelectedStudent, isParent }}
        >
            {children}
        </StudentContext.Provider>
    );
}

export function useStudent() {
    return useContext(StudentContext);
}

