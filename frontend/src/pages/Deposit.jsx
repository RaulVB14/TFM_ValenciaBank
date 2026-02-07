import "../assets/css/Profile.css";
import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';

function Deposit() {
  const [amount, setAmount] = useState('');
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const dni = localStorage.getItem("dni");
        console.log("este es el dni", dni);
        // Realizar la consulta al backend usando el DNI
        const response = await fetch(`http://localhost:8080/user/get/${dni}`);
        
        if (!response.ok) {
          throw new Error('Error al obtener los datos del usuario');
        }
        
        const data = await response.json();
        setUserData(data);
        console.log(data.account.balance);
      } catch (err) {
        console.error('Error al obtener datos del usuario', err);
        setError('Hubo un problema al cargar los datos del usuario.');
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  const handleDeposit = async () => {
    if (!userData) return;

    try {
      const transactionData = {
        originAccount: userData.account.number,
        destinationAccount: userData.account.number,
        amount: parseFloat(amount),
        date: new Date().toISOString(),
      };
      console.log("datos del transaction data: ") //pilla bien la cantidad que se quiere ingresar
      console.log(transactionData.amount, transactionData.date, transactionData.destinationAccount, transactionData.originAccount)

      // Enviar ambos objetos al backend
      const response = await fetch('http://localhost:8080/transactions/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          transaction: transactionData,
          user: userData.dni
        }),
      });

      if (!response.ok) {
        throw new Error('Error al realizar el ingreso');
      }

      const responseData = await response.json();
      console.log('Ingreso realizado con éxito', responseData);
      alert('Ingreso realizado con éxito');
      navigate('/home');

    } catch (error) {
      console.error('Error al realizar el ingreso', error);
      alert('Hubo un problema al realizar el ingreso');
    }
  };

  const Exit = () => {
    navigate('/home');
  };

  if (loading) {
    return <p>Cargando datos...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div className="deposit-page">
      <h2 className="profile-title">VALENCIA BANK</h2>
      {userData ? (
        <div className="deposit-card">
          <div className="transfer-inputs">
            <label>
              Monto a ingresar:
              <input 
                type="number" 
                value={amount} 
                onChange={(e) => setAmount(e.target.value)} 
                placeholder="Monto" 
              />
            </label>
          </div>

          <div className="actions">
            <button className="btn action-btn" onClick={handleDeposit}>Realizar Ingreso</button>
            <button className="btn exit-btn" onClick={Exit}>Salir</button>
          </div>
        </div>
      ) : (
        <p className="loading">Cargando datos...</p>
      )}
    </div>
  );
}

export default Deposit;
