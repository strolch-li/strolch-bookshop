const API_BASE = 'rest';
const AUTH_ENDPOINT = `${API_BASE}/strolch/authentication`;
const BOOKS_ENDPOINT = `${API_BASE}/books`;

let authToken = localStorage.getItem('authToken');
let offset = 0;
const limit = 10;
let searchQuery = '';

// DOM Elements
const loginSection = document.getElementById('login-section');
const booksSection = document.getElementById('books-section');
const bookFormSection = document.getElementById('book-form-section');
const userInfo = document.getElementById('user-info');
const usernameDisplay = document.getElementById('username-display');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const logoutBtn = document.getElementById('logout-btn');
const booksBody = document.getElementById('books-body');
const searchInput = document.getElementById('search-input');
const showAddBookBtn = document.getElementById('show-add-book-btn');
const bookForm = document.getElementById('book-form');
const formTitle = document.getElementById('form-title');
const prevBtn = document.getElementById('prev-btn');
const nextBtn = document.getElementById('next-btn');
const pageInfo = document.getElementById('page-info');
const cancelBookBtn = document.getElementById('cancel-book-btn');
const booksLoading = document.getElementById('books-loading');

// Initial setup
if (authToken) {
    showBooksSection();
} else {
    showLoginSection();
}

// Event Listeners
loginForm.addEventListener('submit', handleLogin);
logoutBtn.addEventListener('click', handleLogout);
searchInput.addEventListener('input', (e) => {
    searchQuery = e.target.value;
    offset = 0;
    fetchBooks();
});
showAddBookBtn.addEventListener('click', () => showBookForm());
bookForm.addEventListener('submit', handleSaveBook);
cancelBookBtn.addEventListener('click', () => showBooksSection());
prevBtn.addEventListener('click', () => {
    offset = Math.max(0, offset - limit);
    fetchBooks();
});
nextBtn.addEventListener('click', () => {
    offset += limit;
    fetchBooks();
});

// Functions
function showLoginSection() {
    loginSection.classList.remove('hidden');
    booksSection.classList.add('hidden');
    bookFormSection.classList.add('hidden');
    userInfo.classList.add('hidden');
}

function showBooksSection() {
    loginSection.classList.add('hidden');
    booksSection.classList.remove('hidden');
    bookFormSection.classList.add('hidden');
    userInfo.classList.remove('hidden');
    usernameDisplay.textContent = localStorage.getItem('username');
    fetchBooks();
}

function showBookForm(book = null) {
    loginSection.classList.add('hidden');
    booksSection.classList.add('hidden');
    bookFormSection.classList.remove('hidden');

    if (book) {
        formTitle.textContent = 'Edit Book';
        document.getElementById('book-id-orig').value = book.id;
        document.getElementById('book-id').value = book.id;
        document.getElementById('book-id').disabled = true;
        document.getElementById('book-name').value = book.name;
        document.getElementById('book-description').value = book.description || '';
        document.getElementById('book-quantity').value = book.quantity || 0;
    } else {
        formTitle.textContent = 'Add Book';
        bookForm.reset();
        document.getElementById('book-id-orig').value = '';
        document.getElementById('book-id').disabled = false;
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const passwordRaw = document.getElementById('password').value;
    const passwordEncoded = btoa(passwordRaw);

    try {
        const response = await fetch(AUTH_ENDPOINT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password: passwordEncoded })
        });

        const data = await response.json();

        if (response.ok) {
            authToken = data.authToken;
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('username', data.username);
            showBooksSection();
        } else {
            loginError.textContent = data.msg || 'Login failed';
        }
    } catch (err) {
        loginError.textContent = 'Connection error';
    }
}

async function handleLogout() {
    try {
        await fetch(`${AUTH_ENDPOINT}/${authToken}`, {
            method: 'DELETE',
            headers: { 'Authorization': authToken }
        });
    } catch (err) {
        console.error('Logout failed', err);
    } finally {
        authToken = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        showLoginSection();
    }
}

async function fetchBooks() {
    const url = `${BOOKS_ENDPOINT}?query=${encodeURIComponent(searchQuery)}&offset=${offset}&limit=${limit}`;
    booksLoading.classList.remove('hidden');
    booksBody.classList.add('hidden');
    try {
        const response = await fetch(url, {
            headers: { 'Authorization': authToken }
        });

        if (response.status === 401) {
            handleLogout();
            return;
        }

        const data = await response.json();
        renderBooks(data.data, data.offset, data.limit, data.size, data.dataSetSize);
    } catch (err) {
        console.error('Fetch books failed', err);
    } finally {
        booksLoading.classList.add('hidden');
        booksBody.classList.remove('hidden');
    }
}

function renderBooks(books, currentOffset, currentLimit, size, dataSetSize) {
    booksBody.innerHTML = '';
    books.forEach(book => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${book.id}</td>
            <td>${book.name}</td>
            <td>${book.description || ''}</td>
            <td>${book.quantity || 0}</td>
            <td>
                <button class="btn-edit" data-id="${book.id}">Edit</button>
                <button class="btn-delete" data-id="${book.id}">Delete</button>
            </td>
        `;
        booksBody.appendChild(tr);
    });

    // Add event listeners for edit and delete buttons
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const book = books.find(b => b.id === id);
            showBookForm(book);
        });
    });

    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', () => handleDeleteBook(btn.getAttribute('data-id')));
    });

    // Update pagination
    prevBtn.disabled = currentOffset === 0;
    nextBtn.disabled = currentOffset + currentLimit >= size;
    if (size === 0) {
        pageInfo.textContent = 'No books found';
    } else {
        const from = currentOffset + 1;
        const to = Math.min(currentOffset + currentLimit, size);
        let text = `Showing ${from} to ${to} of ${size}`;
        if (size < dataSetSize) {
            text += ` (filtered from ${dataSetSize} total)`;
        }
        pageInfo.textContent = text;
    }
}

async function handleDeleteBook(id) {
    if (!confirm(`Are you sure you want to delete book ${id}?`)) return;

    try {
        const response = await fetch(`${BOOKS_ENDPOINT}/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': authToken }
        });

        if (response.ok) {
            fetchBooks();
        } else {
            const data = await response.json();
            alert(data.msg || 'Delete failed');
        }
    } catch (err) {
        console.error('Delete failed', err);
    }
}

async function handleSaveBook(e) {
    e.preventDefault();
    const idOrig = document.getElementById('book-id-orig').value;
    const id = document.getElementById('book-id').value;
    const name = document.getElementById('book-name').value;
    const description = document.getElementById('book-description').value;
    const quantity = parseInt(document.getElementById('book-quantity').value);

    const bookData = { id, name, description, quantity };
    const method = idOrig ? 'PUT' : 'POST';
    const url = idOrig ? `${BOOKS_ENDPOINT}/${idOrig}` : BOOKS_ENDPOINT;

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': authToken
            },
            body: JSON.stringify(bookData)
        });

        if (response.ok) {
            showBooksSection();
        } else {
            const data = await response.json();
            alert(data.msg || 'Save failed');
        }
    } catch (err) {
        console.error('Save failed', err);
    }
}
