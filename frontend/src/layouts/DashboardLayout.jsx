import { Outlet } from 'react-router-dom';

import Sidebar from '../Components/common/Sidebar';
import Navbar from '../Components/common/Navbar';

const DashboardLayout = () => {
  return (
    <div
      style={{
        display: 'flex',
        minHeight: '100vh',
        width: '100%',
        backgroundColor: '#f3f4f6',
      }}
    >

      {/* ======================= */}
      {/* SIDEBAR                 */}
      {/* ======================= */}

      <Sidebar />


      {/* ======================= */}
      {/* PARTIE DROITE           */}
      {/* ======================= */}

      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          minWidth: 0,
        }}
      >

        {/* NAVBAR */}

        <Navbar />


        {/* CONTENU */}

        <main
          style={{
            flex: 1,
            padding: '25px',
            boxSizing: 'border-box',
          }}
        >
          <Outlet />
        </main>

      </div>

    </div>
  );
};

export default DashboardLayout;