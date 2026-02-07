import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FaUser, FaIdCard, FaEnvelope, FaPhone, FaMapMarkerAlt, FaWallet, FaUniversity, FaCalendarAlt, FaEdit, FaTimes, FaSave, FaKey, FaArrowLeft } from 'react-icons/fa';
import '../assets/css/Profile.css';

function Profile() {
  const [userData, setUserData] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editData, setEditData] = useState({});
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  const navigate = useNavigate();

  const Exit = () => {
    navigate('/home');
  };

  const fetchUserData = async () => {
    try {
      const dni = localStorage.getItem("dni");
      const response = await axios.get(`http://localhost:8080/user/get/${dni}`);
      setUserData(response.data);
    } catch (error) {
      console.error('Error al obtener los datos del usuario', error);
    }
  };

  useEffect(() => {
    fetchUserData();
  }, []);

  const openEditModal = () => {
    setEditData({
      username: userData.username || '',
      nombre: userData.nombre || '',
      apellidos: userData.apellidos || '',
      email: userData.email || '',
      telefono: userData.telefono || '',
      direccion: userData.direccion || '',
      password: '',
    });
    setMessage({ text: '', type: '' });
    setShowEditModal(true);
  };

  const handleEditChange = (field, value) => {
    setEditData(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setMessage({ text: '', type: '' });
      const dni = localStorage.getItem("dni");
      const token = localStorage.getItem("token");

      // Enviar solo campos que tienen valor
      const payload = {};
      if (editData.username) payload.username = editData.username;
      if (editData.nombre !== undefined) payload.nombre = editData.nombre;
      if (editData.apellidos !== undefined) payload.apellidos = editData.apellidos;
      if (editData.email !== undefined) payload.email = editData.email;
      if (editData.telefono !== undefined) payload.telefono = editData.telefono;
      if (editData.direccion !== undefined) payload.direccion = editData.direccion;
      if (editData.password) payload.password = editData.password;

      const response = await axios.put(
        `http://localhost:8080/user/update/${dni}`,
        payload,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.data.success) {
        setMessage({ text: '✅ Datos actualizados correctamente', type: 'success' });
        // Actualizar localStorage si cambió el username
        if (payload.username) {
          localStorage.setItem('username', payload.username);
        }
        await fetchUserData();
        setTimeout(() => setShowEditModal(false), 1200);
      }
    } catch (err) {
      console.error('Error al guardar:', err);
      setMessage({
        text: '❌ ' + (err.response?.data?.error || 'Error al guardar los datos'),
        type: 'error'
      });
    } finally {
      setSaving(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('es-ES', {
      day: '2-digit', month: 'long', year: 'numeric'
    });
  };

  return (
    <div className="profile-page">
      <div className="profile-header-bar">
        <button className="back-btn" onClick={Exit}><FaArrowLeft /> Volver</button>
        <h2 className="profile-title">Mi Perfil</h2>
      </div>

      {userData ? (
        <div className="profile-content">

          {/* AVATAR + NOMBRE */}
          <div className="profile-avatar-section">
            <div className="profile-avatar">
              {(userData.nombre || userData.username || '?').charAt(0).toUpperCase()}
            </div>
            <h3 className="profile-fullname">
              {userData.nombre && userData.apellidos
                ? `${userData.nombre} ${userData.apellidos}`
                : userData.username}
            </h3>
            <span className="profile-role">Cliente Valencia Bank</span>
          </div>

          {/* DATOS PERSONALES */}
          <div className="profile-section">
            <h4 className="section-title">Datos Personales</h4>
            <div className="profile-grid">
              <div className="profile-field">
                <FaUser className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Usuario</span>
                  <span className="field-value">{userData.username}</span>
                </div>
              </div>
              <div className="profile-field">
                <FaIdCard className="field-icon" />
                <div className="field-content">
                  <span className="field-label">DNI</span>
                  <span className="field-value">{userData.dni}</span>
                </div>
              </div>
              <div className="profile-field">
                <FaUser className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Nombre</span>
                  <span className="field-value">{userData.nombre || <em className="empty-value">Sin especificar</em>}</span>
                </div>
              </div>
              <div className="profile-field">
                <FaUser className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Apellidos</span>
                  <span className="field-value">{userData.apellidos || <em className="empty-value">Sin especificar</em>}</span>
                </div>
              </div>
              <div className="profile-field">
                <FaEnvelope className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Email</span>
                  <span className="field-value">{userData.email || <em className="empty-value">Sin especificar</em>}</span>
                </div>
              </div>
              <div className="profile-field">
                <FaPhone className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Teléfono</span>
                  <span className="field-value">{userData.telefono || <em className="empty-value">Sin especificar</em>}</span>
                </div>
              </div>
              <div className="profile-field full-width">
                <FaMapMarkerAlt className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Dirección</span>
                  <span className="field-value">{userData.direccion || <em className="empty-value">Sin especificar</em>}</span>
                </div>
              </div>
            </div>
          </div>

          {/* DATOS BANCARIOS (solo lectura) */}
          <div className="profile-section">
            <h4 className="section-title">Datos Bancarios</h4>
            <div className="profile-grid">
              <div className="profile-field readonly">
                <FaUniversity className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Número de Cuenta</span>
                  <span className="field-value mono">{userData.account?.number || 'N/A'}</span>
                </div>
              </div>
              <div className="profile-field readonly">
                <FaWallet className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Saldo Disponible</span>
                  <span className="field-value highlight">{userData.account?.balance?.toFixed(2) || '0.00'} EUR</span>
                </div>
              </div>
              <div className="profile-field readonly">
                <FaCalendarAlt className="field-icon" />
                <div className="field-content">
                  <span className="field-label">Fecha de Apertura</span>
                  <span className="field-value">{formatDate(userData.account?.creationDate)}</span>
                </div>
              </div>
            </div>
          </div>

          {/* BOTÓN EDITAR */}
          <button className="edit-profile-btn" onClick={openEditModal}>
            <FaEdit /> Modificar Datos
          </button>

          {/* MODAL DE EDICIÓN */}
          {showEditModal && (
            <div className="edit-modal-overlay" onClick={() => setShowEditModal(false)}>
              <div className="edit-modal" onClick={e => e.stopPropagation()}>
                <div className="edit-modal-header">
                  <h3><FaEdit /> Editar Perfil</h3>
                  <button className="modal-close" onClick={() => setShowEditModal(false)}><FaTimes /></button>
                </div>
                <div className="edit-modal-body">
                  <div className="edit-field">
                    <label><FaUser /> Usuario</label>
                    <input type="text" value={editData.username}
                      onChange={e => handleEditChange('username', e.target.value)} />
                  </div>
                  <div className="edit-field">
                    <label><FaUser /> Nombre</label>
                    <input type="text" value={editData.nombre}
                      onChange={e => handleEditChange('nombre', e.target.value)}
                      placeholder="Tu nombre" />
                  </div>
                  <div className="edit-field">
                    <label><FaUser /> Apellidos</label>
                    <input type="text" value={editData.apellidos}
                      onChange={e => handleEditChange('apellidos', e.target.value)}
                      placeholder="Tus apellidos" />
                  </div>
                  <div className="edit-field">
                    <label><FaEnvelope /> Email</label>
                    <input type="email" value={editData.email}
                      onChange={e => handleEditChange('email', e.target.value)}
                      placeholder="correo@ejemplo.com" />
                  </div>
                  <div className="edit-field">
                    <label><FaPhone /> Teléfono</label>
                    <input type="tel" value={editData.telefono}
                      onChange={e => handleEditChange('telefono', e.target.value)}
                      placeholder="+34 600 000 000" />
                  </div>
                  <div className="edit-field full-width">
                    <label><FaMapMarkerAlt /> Dirección</label>
                    <input type="text" value={editData.direccion}
                      onChange={e => handleEditChange('direccion', e.target.value)}
                      placeholder="Calle, número, ciudad..." />
                  </div>
                  <div className="edit-field full-width">
                    <label><FaKey /> Nueva Contraseña <small>(dejar vacío para mantener)</small></label>
                    <input type="password" value={editData.password}
                      onChange={e => handleEditChange('password', e.target.value)}
                      placeholder="••••••••" />
                  </div>

                  {message.text && (
                    <div className={`edit-message ${message.type}`}>
                      {message.text}
                    </div>
                  )}
                </div>
                <div className="edit-modal-footer">
                  <button className="cancel-btn" onClick={() => setShowEditModal(false)}>Cancelar</button>
                  <button className="save-btn" onClick={handleSave} disabled={saving}>
                    {saving ? 'Guardando...' : <><FaSave /> Guardar</>}
                  </button>
                </div>
              </div>
            </div>
          )}

        </div>
      ) : (
        <p className="loading">Cargando datos...</p>
      )}
    </div>
  );
}

export default Profile;
