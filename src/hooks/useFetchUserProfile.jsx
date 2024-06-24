import { useEffect, useState } from 'react';
import axios from 'axios';

const useFetchUserProfile = (userId) => {
  const [userData, setUserData] = useState(null);
  const [additionalData, setAdditionalData] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        // da si opravq grozniq kod!!!!!
        const response = await axios.get('http://localhost:4000/users');
        const data = response.data;
        console.log(data);

        const user = data.find(user => user.id === userId);
        console.log(user);
        setUserData(user);

        let additionalInfo = null;

        switch (user.roleType) {
          case 'student':
            additionalInfo = await fetchStudentData(user.studentId);
            break;
          case 'parent':
            additionalInfo = await fetchParentData(user.studentId);
            break;
          case 'teacher':
            additionalInfo = await fetchTeacherData(user.teacherId);
            break;
          case 'headmaster':
            additionalInfo = await fetchHeadmasterData(user.headmasterId);
            break;
          default:
            break;
        }

        setAdditionalData(additionalInfo);
      } catch (error) {
        console.error('Error fetching user data:', error);
      }
    };

    const fetchStudentData = async (studentId) => {
      const { data: student } = await axios.get(`http://localhost:4000/students`)
      .then(() => data.find(student => student.id === studentId));
      const { data: parent } = await axios.get(`http://localhost:4000/parents}`)
      .then(() => data.find(parent => student.parentId));
      return { ...student, parent };
    };

    const fetchParentData = async (studentId) => {
      const { data: student } = await axios.get(`http://localhost:4000/students/${studentId}`);
      const { data: parent } = await axios.get(`http://localhost:4000/parents/${student.parentId}`);
      return { student, parent };
    };

    const fetchTeacherData = async (teacherId) => {
      const { data } = await axios.get(`http://localhost:4000/teachers/${teacherId}`);
      return data;
    };

    const fetchHeadmasterData = async (headmasterId) => {
      const { data } = await axios.get(`http://localhost:4000/headmasters/${headmasterId}`);
      return data;
    };

    fetchUserData();
  }, [userId]);

  return { userData, additionalData };
};

export default useFetchUserProfile;

