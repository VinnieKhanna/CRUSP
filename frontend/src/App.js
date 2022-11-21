import React from 'react';
import Main from './components/Main';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <div style={{ padding: "10px" }}>
          Data Management Platform
        </div>
      </header>
    <main>
        <Main/>
    </main>
    </div>
  );
}

export default App;