document.addEventListener('DOMContentLoaded', () => {
    // ... 設定とDOM要素取得部分は変更なし ...
    const API_BASE_URL = '/api';
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
    const editModal = document.getElementById('edit-modal');
    const editForm = document.getElementById('edit-form');
    const closeModalButton = document.getElementById('close-modal-button');
    const editPostIdInput = document.getElementById('edit-post-id');
    const editAuthorInput = document.getElementById('edit-author');
    const editContentInput = document.getElementById('edit-content');

    /** UIをログイン状態に応じて更新する */
    function updateUI() {
        const token = localStorage.getItem('jwtToken');
        const username = localStorage.getItem('username'); // ★★★ ユーザー名も取得 ★★★

        if (token && username) {
            // ログイン状態
            loginSection.classList.add('hidden');
            signupSection.classList.add('hidden');
            authStatusSection.classList.remove('hidden');
            postFormSection.classList.remove('hidden');
            userInfo.textContent = `ようこそ、${username}さん`;
            postAuthorInput.value = username; // ★★★ 投稿者名フォームに自動入力 ★★★
            postAuthorInput.readOnly = true; // ★★★ 投稿者名を変更不可に ★★★
        } else {
            // ログアウト状態
            showLoginForm();
            authStatusSection.classList.add('hidden');
            postFormSection.classList.add('hidden');
            postAuthorInput.value = ''; // フォームをクリア
            postAuthorInput.readOnly = false; // readonlyを解除
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
            localStorage.setItem('username', data.username); // ★★★ ユーザー名もlocalStorageに保存 ★★★
            loginForm.reset();
            updateUI();

        } catch (error) {
            console.error('ログインエラー:', error);
            alert(error.message);
        }
    }

    /** ログアウト処理 */
    function handleLogout() {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('username'); // ★★★ ユーザー名もlocalStorageから削除 ★★★
        updateUI();
    }


    // 以下、変更のない全コード
    function showLoginForm() { loginSection.classList.remove('hidden'); signupSection.classList.add('hidden'); }
    function showSignupForm() { loginSection.classList.add('hidden'); signupSection.classList.remove('hidden'); }
    function openEditModal(post) { editPostIdInput.value = post.id; editAuthorInput.value = post.author; editContentInput.value = post.content; editModal.classList.remove('hidden'); }
    function closeEditModal() { editModal.classList.add('hidden'); }
/** 投稿を全て取得して画面に表示する */
async function fetchAndDisplayPosts() {
    try {
        const response = await fetch(`${API_BASE_URL}/posts`);
        if (!response.ok) throw new Error(`API Error: ${response.statusText}`);
        const posts = await response.json(); // ここで受け取るpostsはPostResponseの配列になる

        postsContainer.innerHTML = '';
        if (posts.length === 0) {
            postsContainer.innerHTML = '<p>まだ投稿はありません。</p>';
            return;
        }

        // ★★★ ログイン中のユーザー名を取得 ★★★
        const loggedInUsername = localStorage.getItem('username');

        posts.forEach(post => {
            const postElement = document.createElement('div');
            postElement.classList.add('post');

            const postAuthor = document.createElement('h3');
            // ★★★ DTOのauthorUsernameフィールドを使う ★★★
            postAuthor.textContent = post.authorUsername;

            const postMeta = document.createElement('p');
            postMeta.classList.add('post-meta');
            const formattedDate = new Date(post.createdAt).toLocaleString('ja-JP');
            postMeta.textContent = `投稿日時: ${formattedDate}`;

            const postContent = document.createElement('p');
            postContent.textContent = post.content;

            postElement.appendChild(postAuthor);
            postElement.appendChild(postMeta);
            postElement.appendChild(postContent);

            // ★★★ ログイン中のユーザーと投稿の作者が一致する場合のみ、ボタンを追加 ★★★
            if (loggedInUsername && loggedInUsername === post.authorUsername) {
                const actionsContainer = document.createElement('div');
                actionsContainer.classList.add('post-actions');

                const editButton = document.createElement('button');
                editButton.textContent = '編集';
                editButton.classList.add('edit-btn');
                // postオブジェクト全体を渡せるように修正
                const editPostData = {id: post.id, author: post.authorUsername, content: post.content};
                editButton.addEventListener('click', () => openEditModal(editPostData));

                const deleteButton = document.createElement('button');
                deleteButton.textContent = '削除';
                deleteButton.classList.add('delete-btn');
                deleteButton.addEventListener('click', () => handleDeleteClick(post.id));

                actionsContainer.appendChild(editButton);
                actionsContainer.appendChild(deleteButton);
                postElement.appendChild(actionsContainer);
            }

            postsContainer.appendChild(postElement);
        });
    } catch (error) {
        console.error('投稿の読み込みに失敗しました:', error);
        postsContainer.innerHTML = '<p>投稿の読み込みに失敗しました。</p>';
    }
}
    async function handleSignupSubmit(event) { event.preventDefault(); const username = signupForm.username.value; const password = signupForm.password.value; try { const response = await fetch(`${API_BASE_URL}/auth/signup`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) }); const data = await response.json(); if (!response.ok) { throw new Error(data.message || 'ユーザー登録に失敗しました。'); } alert('ユーザー登録が完了しました。ログインしてください。'); signupForm.reset(); showLoginForm(); } catch (error) { console.error('サインアップエラー:', error); alert(error.message); } }
    async function handlePostSubmit(event) { event.preventDefault(); const token = localStorage.getItem('jwtToken'); if (!token) { alert('投稿するにはログインが必要です。'); return; } const postData = { author: postAuthorInput.value, content: postContentInput.value }; try { const response = await fetch(`${API_BASE_URL}/posts`, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }, body: JSON.stringify(postData) }); if (!response.ok) { throw new Error(`投稿に失敗しました: ${response.statusText}`); } postForm.reset(); await fetchAndDisplayPosts(); } catch (error) { console.error('投稿エラー:', error); alert(error.message); } }
    async function handleDeleteClick(postId) { if (!confirm('本当にこの投稿を削除しますか？')) { return; } const token = localStorage.getItem('jwtToken'); if (!token) { alert('操作にはログインが必要です。'); return; } try { const response = await fetch(`${API_BASE_URL}/posts/${postId}`, { method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` } }); if (response.status === 204) { alert('投稿を削除しました。'); await fetchAndDisplayPosts(); } else { throw new Error(`削除に失敗しました: ${response.statusText}`); } } catch (error) { console.error('削除エラー:', error); alert(error.message); } }
    async function handleEditSubmit(event) { event.preventDefault(); const token = localStorage.getItem('jwtToken'); if (!token) { alert('操作にはログインが必要です。'); return; } const postId = editPostIdInput.value; const postData = { author: editAuthorInput.value, content: editContentInput.value }; try { const response = await fetch(`${API_BASE_URL}/posts/${postId}`, { method: 'PUT', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }, body: JSON.stringify(postData) }); if (!response.ok) { throw new Error(`更新に失敗しました: ${response.statusText}`); } closeEditModal(); alert('投稿を更新しました。'); await fetchAndDisplayPosts(); } catch (error) { console.error('更新エラー:', error); alert(error.message); } }
    loginForm.addEventListener('submit', handleLoginSubmit);
    signupForm.addEventListener('submit', handleSignupSubmit);
    postForm.addEventListener('submit', handlePostSubmit);
    logoutButton.addEventListener('click', handleLogout);
    showSignupLink.addEventListener('click', (e) => { e.preventDefault(); showSignupForm(); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showLoginForm(); });
    editForm.addEventListener('submit', handleEditSubmit);
    closeModalButton.addEventListener('click', closeEditModal);
    editModal.addEventListener('click', (event) => { if (event.target === editModal) { closeEditModal(); } });
    updateUI();
    fetchAndDisplayPosts();
});