import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosInstance from '../api';

const fetchAbsences = async () => {
  const { data } = await axiosInstance.get('/absences');
  return data;
};

export const useAbsences = () => {
  return useQuery({queryKey: ['absences'],
  queryFn: fetchAbsences,});
};

const createAbsence = async (absence) => {
  const { data } = await axiosInstance.post('/absences', absence);
  return data;
};

export const useCreateAbsence = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createAbsence,
    onSuccess: () => {
      queryClient.invalidateQueries(['absences']);
    },
  });
};