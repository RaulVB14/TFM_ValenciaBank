import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import BalanceComponent from '../components/BalanceComponent';

function Profile() {

  const [userData, setUserData] = useState(null);
  const navigate = useNavigate();

  const Exit = () => {
    navigate('/home');
  };

  const fetchUserData = async () => {
    try {
      const dni = localStorage.getItem("dni");
      console.log( localStorage.getItem("account"))
      console.log("este es el dni", dni)
      console.log("este es el username", localStorage.getItem("username"))
      // Realizar la consulta al backend usando el DNI
      const response = await axios.get(`http://localhost:8080/user/get/${dni}`);
      console.log(response.data);
      console.log(response);
      setUserData(response.data);
 
      //console.log("balance", response.data.account.balance)
      //console.log("numero",response.data.account.number)
    } catch (error) {
      console.error('Error al obtener los datos del usuario', error);
    }
  };

  useEffect(() => {
    fetchUserData();
  }, []);

  return (
    <div className="profile-page">
      <h2 className="profile-title">VALENCIA BANK</h2>

      {userData ? (
        <div className="profile-card">
          <BalanceComponent 
            balance={userData.account.balance} 
            account={userData.account.number}
          />

          <div className="actions">
            <button className="btn exit-btn" onClick={Exit}>Salir</button>
          </div>
        </div>
      ) : (
        <p className="loading">Cargando datos...</p>
      )}
    </div>
  );
}

export default Profile;
