import React from 'react'
import Sidebar from '../../components/Sidebar'
import { useTranslation } from 'react-i18next'

function AdminHome() {
  const { t } = useTranslation();
  return (
    <>
      <Sidebar/>
        <h1>{t('common.welcome')}</h1>
    </>
  )
}

export default AdminHome