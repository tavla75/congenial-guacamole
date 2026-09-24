import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const currency = new Intl.NumberFormat('sv-SE', {
  style: 'currency',
  currency: 'SEK'
});

async function api(path, options) {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  if (!response.ok) {
    throw new Error(await response.text() || 'Något gick fel');
  }
  return response.status === 204 ? null : response.json();
}

function App() {
  const [page, setPage] = useState('welcome');
  const [products, setProducts] = useState([]);
  const [order, setOrder] = useState([]);
  const [columns, setColumns] = useState(4);
  const [message, setMessage] = useState('');
  const total = useMemo(
    () => order.reduce((sum, item) => sum + item.price, 0),
    [order]
  );

  useEffect(() => {
    api('/api/products')
      .then(setProducts)
      .catch(error => setMessage(error.message));
  }, []);

  function addToOrder(product) {
    setOrder(current => [...current, {
      name: product.name,
      price: Number(product.price)
    }]);
  }

  function updateProduct(id, field, value) {
    setProducts(current => current.map(product =>
      product.id === id
        ? { ...product, [field]: field === 'price' ? Number(value) : value }
        : product
    ));
  }

  async function saveProduct(product) {
    try {
      const saved = await api(`/api/products/${product.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          name: product.name,
          price: Number(product.price)
        })
      });
      setProducts(current => current.map(item =>
        item.id === saved.id ? saved : item
      ));
      setMessage('Produkt sparad.');
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function addProduct() {
    try {
      const product = await api('/api/products', {
        method: 'POST',
        body: JSON.stringify({ name: 'Ny produkt', price: 0 })
      });
      setProducts(current => [...current, product]);
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function pay() {
    if (!order.length) return;
    try {
      const savedOrder = await api('/api/orders', {
        method: 'POST',
        body: JSON.stringify({
          items: order.map(item => ({
            productName: item.name,
            quantity: 1,
            unitPrice: item.price
          }))
        })
      });
      const payment = await api('/api/payments', {
        method: 'POST',
        body: JSON.stringify({ orderId: savedOrder.id })
      });
      await api(`/api/payments/${payment.paymentReference}/complete-test-payment`, {
        method: 'POST'
      });
      setOrder([]);
      setMessage(`Betalning klar: ${currency.format(savedOrder.total)}`);
    } catch (error) {
      setMessage(error.message);
    }
  }

  if (page === 'welcome') {
    return (
      <main className="welcome">
        <h1>Välkommen till restaurangkassan</h1>
        <p>React-gränssnittet är anslutet till Spring Boot och databasen.</p>
        <button onClick={() => setPage('grid')}>Fortsätt</button>
      </main>
    );
  }

  return (
    <main className="app">
      <header>
        <h1>Produktgrid</h1>
        <button className="secondary" onClick={() => setPage('welcome')}>
          Tillbaka
        </button>
      </header>
      <div className="toolbar">
        <label>
          Kolumner
          <select value={columns} onChange={event => setColumns(Number(event.target.value))}>
            {[2, 3, 4, 5].map(value => <option key={value}>{value}</option>)}
          </select>
        </label>
        <button onClick={addProduct}>Lägg till produkt</button>
        <button className="secondary" onClick={() => setPage('admin')}>Admin</button>
      </div>
      {page === 'admin' ? (
        <section className="admin">
          <h2>Administrera priser och produkter</h2>
          {products.map(product => (
            <div className="admin-row" key={product.id}>
              <input
                value={product.name}
                onChange={event => updateProduct(product.id, 'name', event.target.value)}
              />
              <input
                type="number"
                min="0"
                step="0.01"
                value={product.price}
                onChange={event => updateProduct(product.id, 'price', event.target.value)}
              />
              <button onClick={() => saveProduct(product)}>Spara</button>
            </div>
          ))}
          <button className="secondary" onClick={() => setPage('grid')}>Till beställning</button>
        </section>
      ) : (
        <div className="workspace">
          <section
            className="product-grid"
            style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
          >
            {products.map(product => (
              <article className="product" key={product.id}>
                <h2>{product.name}</h2>
                <strong>{currency.format(product.price)}</strong>
                <button onClick={() => addToOrder(product)}>Beställ</button>
              </article>
            ))}
          </section>
          <aside className="order">
            <h2>Aktuell beställning</h2>
            {order.map((item, index) => (
              <div className="order-row" key={`${item.name}-${index}`}>
                <span>{item.name}</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={item.price}
                  onChange={event => setOrder(current => current.map((entry, itemIndex) =>
                    itemIndex === index
                      ? { ...entry, price: Number(event.target.value) }
                      : entry
                  ))}
                />
                <button onClick={() => setOrder(current => current.filter((_, itemIndex) => itemIndex !== index))}>
                  Ta bort
                </button>
              </div>
            ))}
            {!order.length && <p>Inga produkter valda</p>}
            <strong>Totalt: {currency.format(total)}</strong>
            <button disabled={!order.length} onClick={pay}>Betala</button>
          </aside>
        </div>
      )}
      {message && <p className="message">{message}</p>}
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
