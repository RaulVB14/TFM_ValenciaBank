import React from "react";
import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import Profile from "./pages/Profile"
import Summary from "./pages/Summary"
import Deposit from "./pages/Deposit"
import Transfer from "./pages/Transfer"
import Portfolio from "./pages/Portfolio"

//DE AQUI SALEN TODAS LAS RUTAS DE LA PAGINA WEB
export function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainContent />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/home" element={<Home/>}/>
        <Route path="/home/Profile" element={<Profile />} />
        <Route path ="/home/Summary" element={<Summary/>}/>
        <Route path ="/home/Deposit" element={<Deposit/>}/>
        <Route path ="/home/Transfer" element={<Transfer/>}/>
        <Route path ="/home/Portfolio" element={<Portfolio/>}/>
      </Routes>
    </Router>
  );
}

//funncion en la que digo a donde quiero que vaya si a una pantalla u a otra
function MainContent() {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate('/login'); // Redirige a la página de Login
  };

  const handleRegister = () => {
    navigate('/register'); // Redirige a la página de Registro
  };

  //Siempre el return sera el html con sus componentes
  return (
    <div className="home-root">
      <div className="button-container">
        <button className="btn" onClick={handleLogin}>Login</button>
        <button className="btn" onClick={handleRegister}>Registro</button>
      </div>
    </div>
  );
}


export default App;
