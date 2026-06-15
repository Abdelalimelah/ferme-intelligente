import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import DashboardLayout from './layouts/DashboardLayout';
import LoginPage from './pages/LoginPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ProfilePage from './pages/ProfilePage';
import OwnerDashboard from './pages/owner/OwnerDashboard';
import ManageManagers from './pages/owner/ManageManagers';
import ManageWorkers from './pages/owner/ManageWorkers';
import ReportsView from './pages/owner/ReportsView';
import AssignWorker from './pages/owner/AssignWorker';
import AssignManager from './pages/owner/AssignManager';
import ManagerDashboard from './pages/manager/ManagerDashboard';
import ManageParcels from './pages/manager/ManageParcels';
import AssignTasks from './pages/manager/AssignTasks';
import ManageSensors from './pages/manager/ManageSensors';
import AlertsPage from './pages/manager/AlertsPage';
import SubmitReport from './pages/manager/SubmitReport';
import HistoryPage from './pages/manager/HistoryPage';
import ParcelleDetail from './pages/manager/ParcelleDetail';
import DiseaseMap from './pages/manager/DiseaseMap';
import WorkerDashboard from './pages/worker/WorkerDashboard';
import MyTasks from './pages/worker/MyTasks';
import MyParcel from './pages/worker/MyParcel';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/reset-password" element={<ProtectedRoute><ResetPasswordPage /></ProtectedRoute>} />
          <Route path="/" element={<ProtectedRoute><DashboardLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/owner" replace />} />
            <Route path="profile" element={<ProfilePage />} />
            <Route path="owner" element={<OwnerDashboard />} />
            <Route path="owner/managers" element={<ManageManagers />} />
            <Route path="owner/workers" element={<ManageWorkers />} />
            <Route path="owner/reports" element={<ReportsView />} />
            <Route path="owner/assign-worker" element={<AssignWorker />} />
            <Route path="owner/assign-manager" element={<AssignManager />} />
            <Route path="manager" element={<ManagerDashboard />} />
            <Route path="manager/parcels" element={<ManageParcels />} />
            <Route path="manager/tasks" element={<AssignTasks />} />
            <Route path="manager/sensors" element={<ManageSensors />} />
            <Route path="manager/alerts" element={<AlertsPage />} />
            <Route path="manager/report" element={<SubmitReport />} />
            <Route path="manager/history" element={<HistoryPage />} />
            <Route path="manager/parcelle/:id" element={<ParcelleDetail />} />
            <Route path="manager/disease-map" element={<DiseaseMap />} />
            <Route path="worker" element={<WorkerDashboard />} />
            <Route path="worker/tasks" element={<MyTasks />} />
            <Route path="worker/parcel" element={<MyParcel />} />
          </Route>
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
