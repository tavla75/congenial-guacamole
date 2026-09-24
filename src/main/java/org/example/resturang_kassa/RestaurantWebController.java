package org.example.resturang_kassa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RestaurantWebController {

    @GetMapping(value = "/", produces = "text/html")
    public String home() {
        return """
                <!DOCTYPE html>
                <html lang="sv">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Restaurangkassa</title>
                    <style>
                        body {
                            background: #f4f6f8;
                            color: #1f2933;
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 3rem;
                        }
                        main {
                            background: white;
                            border-radius: 12px;
                            margin: auto;
                            max-width: 720px;
                            padding: 2rem;
                            box-shadow: 0 4px 16px rgb(0 0 0 / 10%);
                        }
                        h1 { color: #176b87; }
                        .continue-button {
                            background: #176b87;
                            border-radius: 8px;
                            color: white;
                            display: inline-block;
                            font-weight: bold;
                            padding: 0.8rem 1.3rem;
                            text-decoration: none;
                        }
                        .admin-panel{
                            background: white;
                            border-radius: 10px;
                            margin-bottom: 1.5rem;
                            padding: 1rem;
                        }
                        .admin-panel h2 {
                            margin-top: 0;
                        }
                        .price-row{
                            align-items: center}
                    </style>
                </head>
                <body>
                    <main>
                        <h1>Välkommen till restaurangkassan</h1>
                        <p>Webbservern körs och är redo att ta emot beställningar.</p>
                        <p><a class="continue-button" href="/grid">Fortsätt</a></p>
                        <p>Status: <a href="/api/status">/api/status</a></p>
                    </main>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/grid", produces = "text/html")
    public String grid() {
        return """
                <!DOCTYPE html>
                <html lang="sv">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Produktgrid - Restaurangkassa</title>
                    <style>
                        :root { font-family: Arial, sans-serif; color: #1f2933; }
                        body { background: #f4f6f8; margin: 0; padding: 2rem; }
                        main { margin: auto; max-width: 1000px; }
                        header, .toolbar {
                            align-items: center;
                            display: flex;
                            gap: 1rem;
                            justify-content: space-between;
                        }
                        header { margin-bottom: 1rem; }
                        h1 { color: #176b87; }
                        .toolbar {
                            background: white;
                            border-radius: 10px;
                            margin-bottom: 1.5rem;
                            padding: 1rem;
                        }
                        button, select, input {
                            border: 1px solid #cbd5e1;
                            border-radius: 6px;
                            font: inherit;
                            padding: 0.6rem;
                        }
                        button { cursor: pointer; }
                        .primary { background: #176b87; color: white; }
                        .grid {
                            display: grid;
                            gap: 1rem;
                            grid-template-columns: repeat(4, minmax(0, 1fr));
                        }
                        .workspace {
                            align-items: start;
                            display: grid;
                            gap: 1.5rem;
                            grid-template-columns: minmax(0, 1fr) 280px;
                        }
                        .product {
                            align-items: stretch;
                            background: #176b87;
                            border: 0;
                            color: white;
                            display: flex;
                            flex-direction: column;
                            font-weight: bold;
                            min-height: 130px;
                            padding: 1rem;
                        }
                        .order-panel {
                            background: white;
                            border-radius: 10px;
                            padding: 1rem;
                            position: sticky;
                            top: 1rem;
                        }
                        .order-panel h2 { margin-top: 0; }
                        .order-list {
                            list-style: none;
                            margin: 0 0 1rem;
                            max-height: 300px;
                            overflow-y: auto;
                            padding: 0;
                        }
                        .order-item {
                            align-items: center;
                            border-bottom: 1px solid #e2e8f0;
                            display: flex;
                            gap: 0.5rem;
                            justify-content: space-between;
                            padding: 0.5rem 0;
                        }
                        .order-item input {
                            max-width: 90px;
                            padding: 0.35rem;
                        }
                        .order-item button { padding: 0.25rem 0.4rem; }
                        .order-total { font-weight: bold; }
                        .order-button { margin-top: 0.8rem; width: 100%; }
                        .admin-panel {
                            background: white;
                            border-radius: 10px;
                            margin-bottom: 1.5rem;
                            padding: 1rem;
                        }
                        .admin-panel h2 { margin-top: 0; }
                        .price-row {
                            align-items: center;
                            display: grid;
                            gap: 1rem;
                            grid-template-columns: 1fr 140px;
                            margin-bottom: 0.7rem;
                        }
                        .price-row input { width: 100%; }
                        .product input {
                            background: transparent;
                            border: 1px solid rgb(255 255 255 / 60%);
                            color: white;
                            margin-bottom: auto;
                        }
                        .product input::placeholder { color: rgb(255 255 255 / 80%); }
                        .remove {
                            align-self: flex-end;
                            background: rgb(0 0 0 / 20%);
                            border: 0;
                            color: white;
                            font-size: 0.8rem;
                            margin-top: 0.8rem;
                            padding: 0.3rem 0.5rem;
                        }
                        @media (max-width: 700px) {
                            .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
                            .workspace { grid-template-columns: 1fr; }
                            .order-panel { position: static; }
                            header, .toolbar { align-items: stretch; flex-direction: column; }
                        }
                    </style>
                </head>
                <body>
                    <main>
                        <header>
                            <h1>Produktgrid</h1>
                            <a href="/">Tillbaka</a>
                        </header>
                        <div class="toolbar">
                            <label>
                                Kolumner:
                                <select id="columns">
                                    <option value="2">2</option>
                                    <option value="3">3</option>
                                    <option value="4" selected>4</option>
                                    <option value="5">5</option>
                                </select>
                            </label>
                            <button class="primary" id="add-product" type="button">Lägg till produkt</button>
                            <button id="admin-button" type="button">Admin</button>
                        </div>
                        <section class="admin-panel" id="admin-panel" hidden>
                            <h2>Administrera priser</h2>
                            <div id="price-editor"></div>
                            <button class="primary" id="save-prices" type="button">Spara priser</button>
                        </section>
                        <div class="workspace">
                            <section aria-label="Produkter" class="grid" id="product-grid"></section>
                            <aside aria-label="Aktuell beställning" class="order-panel">
                                <h2>Aktuell beställning</h2>
                                <ul class="order-list" id="order-list">
                                    <li>Inga produkter valda</li>
                                </ul>
                                <div class="order-total" id="order-total">Totalt: 0,00 kr</div>
                            </aside>
                        </div>
                    </main>
                    <script>
                        const grid = document.getElementById('product-grid');
                        const columns = document.getElementById('columns');
                        const orderList = document.getElementById('order-list');
                        const orderTotal = document.getElementById('order-total');
                        const savedProducts = JSON.parse(localStorage.getItem('restaurant-products') || 'null');
                        const products = savedProducts || ['Hamburgare', 'Pizza', 'Sallad', 'Dryck'];
                        const savedPrices = JSON.parse(localStorage.getItem('restaurant-prices') || 'null');
                        const prices = savedPrices || [99, 119, 79, 25];
                        const order = [];

                        function save() {
                            localStorage.setItem('restaurant-products', JSON.stringify(products));
                        }

                        function savePrices() {
                            localStorage.setItem('restaurant-prices', JSON.stringify(prices));
                        }

                        function renderPriceEditor() {
                            const editor = document.getElementById('price-editor');
                            editor.replaceChildren();

                            products.forEach((product, index) => {
                                const row = document.createElement('div');
                                row.className = 'price-row';

                                const label = document.createElement('label');
                                label.textContent = product.trim() || 'Namnlös produkt';
                                label.htmlFor = `price-${index}`;

                                const input = document.createElement('input');
                                input.id = `price-${index}`;
                                input.type = 'number';
                                input.min = '0';
                                input.step = '0.01';
                                input.value = prices[index] || 0;
                                input.dataset.index = index;

                                row.append(label, input);
                                editor.append(row);
                            });
                        }

                        function saveEditedPrices() {
                            const inputs = document.querySelectorAll('#price-editor input');

                            inputs.forEach(input => {
                                const index = Number(input.dataset.index);
                                const newPrice = Number(input.value);

                                if (Number.isFinite(newPrice) && newPrice >= 0) {
                                    prices[index] = newPrice;
                                }
                            });

                            savePrices();
                            render();
                        }

                        function render() {
                            grid.style.gridTemplateColumns = `repeat(${columns.value}, minmax(0, 1fr))`;
                            grid.replaceChildren();
                            products.forEach((product, index) => {
                                const tile = document.createElement('article');
                                tile.className = 'product';
                                const input = document.createElement('input');
                                input.type = 'text';
                                input.value = product;
                                input.placeholder = 'Produktnamn';
                                input.addEventListener('input', () => {
                                    products[index] = input.value;
                                    save();
                                });
                                const orderButton = document.createElement('button');
                                orderButton.className = 'primary order-button';
                                orderButton.textContent = 'Beställ';
                                orderButton.type = 'button';
                                orderButton.addEventListener('click', () => {
                                    order.push({
                                        name: input.value.trim() || 'Namnlös produkt',
                                        price: prices[index] || 0
                                    });
                                    renderOrder();
                                });
                                const remove = document.createElement('button');
                                remove.className = 'remove';
                                remove.textContent = 'Ta bort';
                                remove.type = 'button';
                                remove.addEventListener('click', () => {
                                    products.splice(index, 1);
                                    prices.splice(index, 1);
                                    save();
                                    savePrices();
                                    renderPriceEditor();
                                    render();
                                });
                                tile.append(input, orderButton, remove);
                                grid.append(tile);
                            });
                        }

                        function renderOrder() {
                            orderList.replaceChildren();
                            if (order.length === 0) {
                                const empty = document.createElement('li');
                                empty.textContent = 'Inga produkter valda';
                                orderList.append(empty);
                            }

                            order.forEach((item, index) => {
                                const line = document.createElement('li');
                                line.className = 'order-item';
                                const text = document.createElement('span');
                                text.textContent = item.name;
                                const priceInput = document.createElement('input');
                                priceInput.type = 'number';
                                priceInput.min = '0';
                                priceInput.step = '0.01';
                                priceInput.value = item.price.toFixed(2);
                                priceInput.setAttribute('aria-label', `Pris för ${item.name}`);
                                priceInput.addEventListener('input', () => {
                                    const newPrice = Number(priceInput.value);
                                    if (Number.isFinite(newPrice) && newPrice >= 0) {
                                        item.price = newPrice;
                                        updateOrderTotal();
                                    }
                                });
                                const remove = document.createElement('button');
                                remove.textContent = 'Ta bort';
                                remove.type = 'button';
                                remove.addEventListener('click', () => {
                                    order.splice(index, 1);
                                    renderOrder();
                                });
                                line.append(text, priceInput, remove);
                                orderList.append(line);
                            });
                            updateOrderTotal();
                        }

                        function updateOrderTotal() {
                            const total = order.reduce((sum, item) => sum + item.price, 0);
                            orderTotal.textContent = `Totalt: ${total.toFixed(2).replace('.', ',')} kr`;
                        }

                        columns.addEventListener('change', render);
                        document.getElementById('admin-button').addEventListener('click', () => {
                            const adminPanel = document.getElementById('admin-panel');
                            adminPanel.hidden = !adminPanel.hidden;

                            if (!adminPanel.hidden) {
                                renderPriceEditor();
                            }
                        });
                        document.getElementById('save-prices').addEventListener('click', () => {
                            saveEditedPrices();
                            document.getElementById('admin-panel').hidden = true;
                        });
                        document.getElementById('add-product').addEventListener('click', () => {
                            products.push('Ny produkt');
                            prices.push(0);
                            save();
                            savePrices();
                            renderPriceEditor();
                            render();
                        });
                        render();
                        renderOrder();
                    </script>
                </body>
                </html>
                """;
    }

    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of(
                "application", "Resturang kassa",
                "status", "running"
        );
    }
}
