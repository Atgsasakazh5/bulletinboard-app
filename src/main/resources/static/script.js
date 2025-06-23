document.addEventListener('DOMContentLoaded', () => {
    // ===============================================
    // 設定
    // ===============================================
    const API_BASE_URL = '/api';

    // ===============================================
    // DOM要素の取得
    // ===============================================
    const postsContainer = document.getElementById('posts-container');
    const postForm = document.getElementById('post-form');
    const loginForm = document.getElementById('login-form');
    const logoutButton = document.getElementById('logout-button');
    const signupForm = document.getElementById('signup-form');
    const loginSection = document.getElementById('login-section');
    const signupSection = document.getElementById('signup-section');
    const authStatusSection = document.getElementById('auth-status-section');
    const postFormSection = document.getElementById('post-form-section');
    const userInfo = document.getElementById('user-info');
    const showSignupLink = document.getElementById('show-signup-link');
    const showLoginLink = document.getElementById('show-login-link');
    const postAuthorInput = document.getElementById('post-author');
    const postContentInput = document.getElementById('post-content');

    // ===============================================
    // UI更新関数
    // ===============================================

    /** UIをログイン状態に応じて更新する */
    function updateUI() {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            loginSection.classList.add('hidden');
            signupSection.classList.add('hidden');
            authStatusSection.classList.remove('hidden');
            postFormSection.classList.remove('hidden');
            userInfo.textContent = 'ようこそ、認証済みユーザーさん';
        } else {
            showLoginForm();
            authStatusSection.classList.add('hidden');
            postFormSection.classList.add('hidden');
        }
    }

    /** ログインフォームを表示する */
    function showLoginForm() {
        loginSection.classList.remove('hidden');
        signupSection.classList.add('hidden');
    }

    /** 新規登録フォームを表示する */
    function showSignupForm() {
        loginSection.classList.add('hidden');
        signupSection.classList.remove('hidden');
    }

    // ===============================================
    // API通信関数
    // ===============================================
    
    /** 投稿を全て取得して画面に表示する */
    async function fetchAndDisplayPosts() {
        try {
            const response = await fetch(`${API_BASE_URL}/posts`);
            if (!response.ok) throw new Error(`API Error: ${response.statusText}`);
            const posts = await response.json();
            postsContainer.innerHTML = '';
            if (posts.length === 0) {
                postsContainer.innerHTML = '<p>まだ投稿はありません。</p>';
                return;
            }
            posts.forEach(post => {
                const postElement = document.createElement('div');
                postElement.classList.add('post');
                const postAuthor = document.createElement('h3');
                postAuthor.textContent = post.author;
                const postMeta = document.createElement('p');
                postMeta.classList.add('post-meta');
                const formattedDate = new Date(post.createdAt).toLocaleString('ja-JP');
                postMeta.textContent = `投稿日時: ${formattedDate}`;
                const postContent = document.createElement('p');
                postContent.textContent = post.content;
                postElement.appendChild(postAuthor);
                postElement.appendChild(postMeta);
                postElement.appendChild(postContent);
                postsContainer.appendChild(postElement);
            });
        } catch (error) {
            console.error('投稿の読み込みに失敗しました:', error);
            postsContainer.innerHTML = '<p>投稿の読み込みに失敗しました。</p>';
        }
    }

    /** 新規登録フォームの送信を処理する */
    async function handleSignupSubmit(event) {
        event.preventDefault();
        const username = signupForm.username.value;
        const password = signupForm.password.value;

        try {
            const response = await fetch(`${API_BASE_URL}/auth/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'ユーザー登録に失敗しました。');
            }
            
            alert('ユーザー登録が完了しました。ログインしてください。');
            signupForm.reset();
            showLoginForm();

        } catch (error) {
            console.error('サインアップエラー:', error);
            alert(error.message);
        }
    }

    /** ログインフォームの送信を処理する */
    async function handleLoginSubmit(event) {
        event.preventDefault();
        const username = loginForm.username.value;
        const password = loginForm.password.value;

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error('ログインに失敗しました。ユーザー名またはパスワードを確認してください。');
            }

            const data = await response.json();
            localStorage.setItem('jwtToken', data.accessToken);
            loginForm.reset();
            updateUI();

        } catch (error) {
            console.error('ログインエラー:', error);
            alert(error.message);
        }
    }

    /** 新規投稿フォームの送信を処理する */
    async function handlePostSubmit(event) {
        event.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            alert('投稿するにはログインが必要です。');
            return;
        }

        const postData = {
            author: postAuthorInput.value,
            content: postContentInput.value
        };

        try {
            const response = await fetch(`${API_BASE_URL}/posts`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(postData)
            });

            if (!response.ok) {
                throw new Error(`投稿に失敗しました: ${response.statusText}`);
            }

            postForm.reset();
            await fetchAndDisplayPosts();

        } catch (error) {
            console.error('投稿エラー:', error);
            alert(error.message);
        }
    }
    
    /** ログアウト処理 */
    function handleLogout() {
        localStorage.removeItem('jwtToken');
        updateUI();
    }

    // ===============================================
    // イベントリスナーと初期化
    // ===============================================
    loginForm.addEventListener('submit', handleLoginSubmit);
    signupForm.addEventListener('submit', handleSignupSubmit);
    postForm.addEventListener('submit', handlePostSubmit);
    logoutButton.addEventListener('click', handleLogout);
    
    showSignupLink.addEventListener('click', (e) => {
        e.preventDefault();
        showSignupForm();
    });
    showLoginLink.addEventListener('click', (e) => {
        e.preventDefault();
        showLoginForm();
    });

    // ページ読み込み時の初期化処理
    updateUI();
    fetchAndDisplayPosts();
});