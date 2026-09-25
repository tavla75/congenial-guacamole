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
  const [selectedCategory, setSelectedCategory] = useState('Mat');
  const [message, setMessage] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);
  const [savingAll, setSavingAll] = useState(false);
  const categories = useMemo(
    () => [...new Set(['Mat', 'Dryck', ...products.map(product => product.category || 'Mat')])],
    [products]
  );
  const visibleProducts = products.filter(
    product => (product.category || 'Mat') === selectedCategory
  );
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
          price: Number(product.price),
          category: product.category || 'Mat'
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

  async function saveAllProducts() {
    setSavingAll(true);
    setMessage('');
    const savedProducts = [];
    const failedProducts = [];

    for (const product of products) {
      try {
        const saved = await api(`/api/products/${product.id}`, {
          method: 'PUT',
          body: JSON.stringify({
            name: product.name,
            price: Number(product.price),
            category: product.category || 'Mat'
          })
        });
        savedProducts.push(saved);
      } catch {
        failedProducts.push(product.name || `Produkt ${product.id}`);
      }
    }

    if (savedProducts.length > 0) {
      const savedById = new Map(savedProducts.map(product => [product.id, product]));
      setProducts(current => current.map(product => savedById.get(product.id) || product));
    }

    if (failedProducts.length > 0) {
      setMessage(
        `Sparade ${savedProducts.length} av ${products.length}. Kunde inte spara: ${failedProducts.join(', ')}.`
      );
    } else {
      setMessage(`Alla ${savedProducts.length} produkter sparades.`);
    }
    setSavingAll(false);
  }

  async function addProduct() {
    try {
      const product = await api('/api/products', {
        method: 'POST',
        body: JSON.stringify({ name: 'Ny produkt', price: 0, category: 'Mat' })
      });
      setProducts(current => [...current, product]);
      setSelectedCategory('Mat');
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
        <div className="header-title">
          <button
            className="menu-button"
            aria-label="Öppna meny"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen(current => !current)}
          >
            <span />
            <span />
            <span />
          </button>
          <h1>{page === 'admin' ? 'Admin' : 'Produktgrid'}</h1>
        </div>
        <button className="secondary" onClick={() => setPage('welcome')}>
          Tillbaka
        </button>
      </header>
      {menuOpen && (
        <nav className="menu" aria-label="Huvudmeny">
          <button
            onClick={() => {
              setPage('grid');
              setMenuOpen(false);
            }}
          >
            Beställning
          </button>
          <button
            onClick={() => {
              setPage('admin');
              setMenuOpen(false);
            }}
          >
            Admin
          </button>
          <button
            onClick={() => {
              setPage('welcome');
              setMenuOpen(false);
            }}
          >
            Startsida
          </button>
        </nav>
      )}
      <div className="toolbar">
        <label>
          Kolumner
          <select value={columns} onChange={event => setColumns(Number(event.target.value))}>
            {[2, 3, 4, 5].map(value => <option key={value}>{value}</option>)}
          </select>
        </label>
        <button onClick={addProduct}>Lägg till produkt</button>
      </div>
      {page === 'admin' ? (
        <section className="admin">
          <div className="admin-header">
            <h2>Administrera priser och produkter</h2>
            <button onClick={saveAllProducts} disabled={savingAll || products.length === 0}>
              {savingAll ? 'Sparar...' : 'Spara alla'}
            </button>
          </div>
          {products.map(product => (
            <div className="admin-row" key={product.id}>
              <input
                aria-label={`Kategori för ${product.name}`}
                value={product.category || 'Mat'}
                onChange={event => updateProduct(product.id, 'category', event.target.value)}
                placeholder="Kategori"
              />
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
              <button
                className="danger"
                onClick={async () => {
                  if (!window.confirm(`Ta bort produkten "${product.name}"?`)) return;
                  try {
                    await api(`/api/products/${product.id}`, { method: 'DELETE' });
                    setProducts(current => current.filter(item => item.id !== product.id));
                    setMessage('Produkten togs bort.');
                  } catch (error) {
                    setMessage(error.message);
                  }
                }}
              >
                Ta bort
              </button>
            </div>
          ))}
          <button className="secondary" onClick={() => setPage('grid')}>Till beställning</button>
        </section>
      ) : (
        <>
          <nav className="category-tabs" aria-label="Produktkategorier">
            {categories.map(category => (
              <button
                className={category === selectedCategory ? 'active' : 'secondary'}
                key={category}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </nav>
          <div className="workspace">
            <section
              className="product-grid"
              style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
            >
              {visibleProducts.map(product => (
                <article className="product" key={product.id}>
                  <h2>{product.name}</h2>
                  <strong>{currency.format(product.price)}</strong>
                  <button onClick={() => addToOrder(product)}>Beställ</button>
                </article>
              ))}
              {!visibleProducts.length && <p>Inga produkter i denna kategori.</p>}
            </section>
          <aside className="order">
            <h2>Aktuell beställning</h2>
            {order.map((item, index) => (
              <div className="order-row" key={`${item.name}-${index}`}>
                <span>{item.name}</span>
                <span>{currency.format(item.price)}</span>
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
        </>
      )}
      {message && <p className="message">{message}</p>}
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
