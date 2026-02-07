import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import SummaryComponent from "../components/SummaryComponent";

function Summary() {
  const [transactionData, setTransactionData] = useState(null);
  const [filteredData, setFilteredData] = useState(null); // Estado para las transacciones filtradas
  const [minAmount, setMinAmount] = useState(""); // Estado para el importe mínimo
  const [maxAmount, setMaxAmount] = useState(""); // Estado para el importe máximo
  const [startDate, setStartDate] = useState(""); // Estado para la fecha inicial
  const [endDate, setEndDate] = useState(""); // Estado para la fecha final
  const navigate = useNavigate();
  
  const convertDateFormat = (dateStr) => {
    return dateStr.replace(/-/g, '/');
  };

  useEffect(() => {
    // Al cargar el componente, intenta cargar los filtros previos desde localStorage
    const savedMinAmount = localStorage.getItem('minAmount');
    const savedMaxAmount = localStorage.getItem('maxAmount');
    const savedStartDate = localStorage.getItem('startDate');
    const savedEndDate = localStorage.getItem('endDate');

    if (savedMinAmount) setMinAmount(savedMinAmount);
    if (savedMaxAmount) setMaxAmount(savedMaxAmount);
    if (savedStartDate) setStartDate(savedStartDate);
    if (savedEndDate) setEndDate(savedEndDate);

    console.log('Cargando filtros previos:', savedMinAmount, savedMaxAmount, savedStartDate, savedEndDate);

    fetchTransactionData();
  }, []); // Solo se ejecuta una vez al cargar el componente


  const resetFilters = () => {
    // Restaurar a todas las transacciones y limpiar los inputs de filtro
    setFilteredData(transactionData || []);
    setMinAmount("");
    setMaxAmount("");
    setStartDate("");
    setEndDate("");
    localStorage.removeItem('minAmount');
    localStorage.removeItem('maxAmount');
    localStorage.removeItem('startDate');
    localStorage.removeItem('endDate');
  };

  const Reset = () => {
    resetFilters();
  }

  const Exit = () => {
    navigate('/home');
    
  
    // Lógica adicional para salir de la pantalla si es necesario
  };

  const fetchTransactionData = async () => {
    try {
      const dni = localStorage.getItem("dni");
      const response = await axios.get(`http://localhost:8080/user/get/${dni}`);

      const transactions = response.data.transactions;
  console.debug('fetchTransactionData: received transactions sample', transactions && transactions.length, transactions && transactions.slice(0,3));

      // Mantener fecha original en ISO para comparaciones y añadir una versión para mostrar
      const formattedTransactions = transactions.map(transaction => {
        const rawDate = transaction.date || transaction.dateISO || transaction.fecha || transaction.createdAt || transaction.timestamp;
        let dateISO = rawDate;
        // if timestamp is numeric, convert to ISO
        if (typeof rawDate === 'number') {
          dateISO = new Date(rawDate).toISOString();
        }
        let displayDate = 'N/A';
        try {
          const d = new Date(dateISO);
          displayDate = isNaN(d.getTime()) ? 'N/A' : d.toLocaleDateString();
        } catch (e) {
          displayDate = 'N/A';
        }

        return {
          ...transaction,
          dateISO,
          date: displayDate,
        };
      });

      setTransactionData(formattedTransactions);
      // Aplicar filtros actuales (si hay) sobre los datos recién obtenidos
      const filteredOnFetch = getFiltered(formattedTransactions);
      setFilteredData(filteredOnFetch);
    } catch (error) {
      console.error('Error al obtener los datos de la transacción', error);
      alert('Hubo un problema al obtener los datos de la transacción');
    }
  };

  const handleFilter = () => {
    // Guardar los filtros en localStorage
    localStorage.setItem('minAmount', minAmount);
    localStorage.setItem('maxAmount', maxAmount);
    localStorage.setItem('startDate', startDate);
    localStorage.setItem('endDate', endDate);

    // Si ya hay datos cargados, aplicar filtros sobre ellos
    if (transactionData) {
      const filtered = getFiltered(transactionData);
      setFilteredData(filtered);
    } else {
      // Si no hay datos aún, dejamos que fetchTransactionData aplique los filtros cuando obtenga los datos
      setFilteredData([]);
    }
  };

  // Función auxiliar que aplica los filtros sobre un array de transacciones
  const getFiltered = (transactionsArray) => {
    if (!transactionsArray) return [];
    let result = [...transactionsArray];
    console.debug('getFiltered: initial count', result.length);

    if (minAmount !== "") {
      const min = parseFloat(minAmount);
      result = result.filter(t => {
        const amt = parseFloat(t.amount ?? t.monto ?? t.value ?? 0);
        return !isNaN(amt) && amt >= min;
      });
      console.debug('after minAmount filter', result.length);
    }
    if (maxAmount !== "") {
      const max = parseFloat(maxAmount);
      result = result.filter(t => {
        const amt = parseFloat(t.amount ?? t.monto ?? t.value ?? 0);
        return !isNaN(amt) && amt <= max;
      });
      console.debug('after maxAmount filter', result.length);
    }
    if (startDate !== "") {
      const filterStartDate = new Date(startDate);
      result = result.filter(t => {
        const td = new Date(t.dateISO);
        if (isNaN(td.getTime())) return false; // no date -> exclude
        return td >= filterStartDate;
      });
      console.debug('after startDate filter', result.length);
    }
    if (endDate !== "") {
      const filterEndDate = new Date(endDate);
      filterEndDate.setHours(23,59,59,999);
      result = result.filter(t => {
        const td = new Date(t.dateISO);
        if (isNaN(td.getTime())) return false;
        return td <= filterEndDate;
      });
      console.debug('after endDate filter', result.length);
    }

    return result;
  };



  return (
    <div className="summary-container">
      <h2 className="profile-title">VALENCIA BANK</h2>

      {/* Filtros */}
      <div className="filters">
        <label>Importe mínimo: </label>
        <input
          type="number"
          value={minAmount}
          onChange={(e) => setMinAmount(e.target.value)}
          placeholder="Ingrese importe mínimo"
        />
        <label>Importe máximo: </label>
        <input
          type="number"
          value={maxAmount}
          onChange={(e) => setMaxAmount(e.target.value)}
          placeholder="Ingrese importe máximo"
        />
        <label>Fecha desde: </label>
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
        />
        <label>Fecha hasta: </label>
        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
        />
        <div className="filters-actions">
          <button className="btn" onClick={handleFilter}>Aplicar Filtros</button>
          <button className="btn" onClick={resetFilters}>Restablecer</button>
        </div>
      </div>

      {/* Mostrar transacciones filtradas */}
      <div className="transactions-container">
        {filteredData ? (
          filteredData.length > 0 ? (
            filteredData.map((transaction, index) => (
              <SummaryComponent
                key={index}
                id={transaction.id}
                originAccount={transaction.originAccount}
                destinationAccount={transaction.destinationAccount}
                amount={transaction.amount}
                date={transaction.date}
              />
            ))
          )
           : (
            <p>No se encontraron transacciones con los filtros seleccionados.</p>
          )
        ) : (
          <p>No se ha cargado ninguna información de la transacción.</p>
        )}
        <button className="btn" onClick={Exit}>Salir</button>
      </div>
    </div>
  );
}

export default Summary;
