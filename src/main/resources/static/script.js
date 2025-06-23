document.addEventListener('DOMContentLoaded', () => {
    // ===============================================
    // 設定
    // ===============================================
    const API_BASE_URL = '/api'; // 同居構成なので相対パス

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

    // ★★★ 編集モーダル関連の要素を取得 ★★★
    const editModal = document.getElementById('edit-modal');
    const editForm = document.getElementById('edit-form');
    const closeModalButton = document.getElementById('close-modal-button');
    const editPostIdInput = document.getElementById('edit-post-id');
    const editAuthorInput = document.getElementById('edit-author');
    const editContentInput = document.getElementById('edit-content');

    // ===============================================
    // UI更新関数
    // ===============================================

    function updateUI() { /* ...変更なし... */ }
    function showLoginForm() { /* ...変更なし... */ }
    function showSignupForm() { /* ...変更なし... */ }

    /** ★★★ 編集モーダルを開く ★★★ */
    function openEditModal(post) {
        editPostIdInput.value = post.id;
        editAuthorInput.value = post.author;
        editContentInput.value = post.content;
        editModal.classList.remove('hidden');
    }

    /** ★★★ 編集モーダルを閉じる ★★★ */
    function closeEditModal() {
        editModal.classList.add('hidden');
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

                // ... 投稿者名、メタ情報、内容の作成は変更なし ...
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

                // ★★★ 編集・削除ボタンのコンテナを追加 ★★★
                const actionsContainer = document.createElement('div');
                actionsContainer.classList.add('post-actions');

                const editButton = document.createElement('button');
                editButton.textContent = '編集';
                editButton.classList.add('edit-btn');
                editButton.addEventListener('click', () => openEditModal(post));

                const deleteButton = document.createElement('button');
                deleteButton.textContent = '削除';
                deleteButton.classList.add('delete-btn');
                deleteButton.addEventListener('click', () => handleDeleteClick(post.id));

                actionsContainer.appendChild(editButton);
                actionsContainer.appendChild(deleteButton);
                postElement.appendChild(actionsContainer);

                postsContainer.appendChild(postElement);
            });
        } catch (error) {
            console.error('投稿の読み込みに失敗しました:', error);
            postsContainer.innerHTML = '<p>投稿の読み込みに失敗しました。</p>';
        }
    }

    async function handleSignupSubmit(event) { /* ...変更なし... */ }
    async function handleLoginSubmit(event) { /* ...変更なし... */ }
    async function handlePostSubmit(event) { /* ...変更なし... */ }
    function handleLogout() { /* ...変更なし... */ }

    /** ★★★ 削除ボタンがクリックされた時の処理 ★★★ */
    async function handleDeleteClick(postId) {
        if (!confirm('本当にこの投稿を削除しますか？')) {
            return;
        }

        const token = localStorage.getItem('jwtToken');
        if (!token) {
            alert('操作にはログインが必要です。');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/posts/${postId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.status === 204) {
                alert('投稿を削除しました。');
                await fetchAndDisplayPosts(); // 投稿一覧を更新
            } else {
                throw new Error(`削除に失敗しました: ${response.statusText}`);
            }
        } catch (error) {
            console.error('削除エラー:', error);
            alert(error.message);
        }
    }

    /** ★★★ 編集フォームが送信された時の処理 ★★★ */
    async function handleEditSubmit(event) {
        event.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            alert('操作にはログインが必要です。');
            return;
        }

        const postId = editPostIdInput.value;
        const postData = {
            author: editAuthorInput.value,
            content: editContentInput.value
        };

        try {
            const response = await fetch(`${API_BASE_URL}/posts/${postId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(postData)
            });

            if (!response.ok) {
                throw new Error(`更新に失敗しました: ${response.statusText}`);
            }

            closeEditModal();
            alert('投稿を更新しました。');
            await fetchAndDisplayPosts(); // 投稿一覧を更新

        } catch (error) {
            console.error('更新エラー:', error);
            alert(error.message);
        }
    }

    // ===============================================
    // イベントリスナーと初期化
    // ===============================================
    loginForm.addEventListener('submit', handleLoginSubmit);
    signupForm.addEventListener('submit', handleSignupSubmit);
    postForm.addEventListener('submit', handlePostSubmit);
    logoutButton.addEventListener('click', handleLogout);
    showSignupLink.addEventListener('click', (e) => { e.preventDefault(); showSignupForm(); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showLoginForm(); });

    // ★★★ 編集モーダル関連のイベントリスナーを追加 ★★★
    editForm.addEventListener('submit', handleEditSubmit);
    closeModalButton.addEventListener('click', closeEditModal);
    // モーダルの外側をクリックした時に閉じる
    editModal.addEventListener('click', (event) => {
        if (event.target === editModal) {
            closeEditModal();
        }
    });

    // 初期化処理
    updateUI();
    fetchAndDisplayPosts();
});