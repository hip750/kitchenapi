// API Configuration
var API_BASE = '/api';
var token = localStorage.getItem('token');
var currentUser = null;
var isSignupMode = false;

// Axios configuration
axios.defaults.baseURL = API_BASE;
axios.interceptors.request.use(function(config) {
    var token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = 'Bearer ' + token;
    }
    return config;
}, function(error) {
    return Promise.reject(error);
});

axios.interceptors.response.use(
    function(response) { return response; },
    function(error) {
        if (error.response && error.response.status === 401) {
            logout();
        }
        return Promise.reject(error);
    }
);

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    console.log('アプリ初期化完了');
    checkAuth();
});

function checkAuth() {
    var token = localStorage.getItem('token');
    var user = localStorage.getItem('user');

    if (token && user) {
        try {
            currentUser = JSON.parse(user);
            if (currentUser && currentUser.name) {
                showMainApp();
                showDashboard();
                return;
            }
        } catch (e) {
            console.error('localStorageに無効なユーザーデータがあります:', e);
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        }
    }
    showLoginPage();
}

function showLoginPage() {
    document.getElementById('login-page').classList.remove('hidden');
    document.getElementById('navbar').classList.add('hidden');
    document.getElementById('main-content').classList.add('hidden');
}

function showMainApp() {
    document.getElementById('login-page').classList.add('hidden');
    document.getElementById('navbar').classList.remove('hidden');
    document.getElementById('main-content').classList.remove('hidden');
    document.getElementById('user-name').textContent = 'ようこそ、' + currentUser.name;
}

function toggleSignupMode() {
    console.log('新規登録モード切り替えクリック');
    isSignupMode = !isSignupMode;
    var nameField = document.getElementById('name-field');
    var emailField = document.querySelector('.email-field');
    var loginModeText = document.getElementById('login-mode-text');
    var loginButtonText = document.getElementById('login-button-text');
    var signupToggleText = document.getElementById('signup-toggle-text');
    
    if (isSignupMode) {
        nameField.classList.remove('hidden');
        nameField.querySelector('input').required = true;
        emailField.classList.remove('rounded-t-md');
        loginModeText.textContent = 'アカウントを作成';
        loginButtonText.textContent = '新規登録';
        signupToggleText.textContent = 'すでにアカウントをお持ちですか？ ログイン';
    } else {
        nameField.classList.add('hidden');
        nameField.querySelector('input').required = false;
        emailField.classList.add('rounded-t-md');
        loginModeText.textContent = 'アカウントにログイン';
        loginButtonText.textContent = 'ログイン';
        signupToggleText.textContent = 'アカウントをお持ちでないですか？ 新規登録';
    }
    console.log('新規登録モード:', isSignupMode);
}

function handleLogin(event) {
    event.preventDefault();
    console.log('ログインフォーム送信');
    var errorDiv = document.getElementById('login-error');
    errorDiv.classList.add('hidden');

    var email = document.getElementById('email').value;
    var password = document.getElementById('password').value;
    var name = document.getElementById('name').value;

    if (isSignupMode) {
        console.log('新規登録を試行中...');
        axios.post('/auth/signup', { email: email, password: password, name: name })
            .then(function() {
                return axios.post('/auth/login', { email: email, password: password });
            })
            .then(function(response) {
                localStorage.setItem('token', response.data.token);
                var user = {
                    id: response.data.userId,
                    email: response.data.email,
                    name: response.data.name
                };
                localStorage.setItem('user', JSON.stringify(user));
                currentUser = user;
                token = response.data.token;
                showMainApp();
                showDashboard();
            })
            .catch(function(error) {
                console.error('認証エラー:', error);
                var message = (error.response && error.response.data && error.response.data.detail) || '認証に失敗しました';
                errorDiv.textContent = message;
                errorDiv.classList.remove('hidden');
            });
    } else {
        console.log('ログインを試行中...');
        axios.post('/auth/login', { email: email, password: password })
            .then(function(response) {
                localStorage.setItem('token', response.data.token);
                var user = {
                    id: response.data.userId,
                    email: response.data.email,
                    name: response.data.name
                };
                localStorage.setItem('user', JSON.stringify(user));
                currentUser = user;
                token = response.data.token;
                showMainApp();
                showDashboard();
            })
            .catch(function(error) {
                console.error('認証エラー:', error);
                var message = (error.response && error.response.data && error.response.data.detail) || '認証に失敗しました';
                errorDiv.textContent = message;
                errorDiv.classList.remove('hidden');
            });
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    token = null;
    currentUser = null;
    showLoginPage();
}

function showDashboard() {
    hideAllPages();
    document.getElementById('dashboard-page').classList.remove('hidden');
    
    axios.get('/recipes', { params: { page: 0, size: 5 } })
        .then(function(recipesRes) {
            return axios.get('/pantry', { params: { page: 0, size: 5 } })
                .then(function(pantryRes) {
                    return { recipes: recipesRes, pantry: pantryRes };
                });
        })
        .then(function(data) {
            document.getElementById('recipe-count').textContent = data.recipes.data.totalElements || 0;
            document.getElementById('pantry-count').textContent = data.pantry.data.totalElements || 0;
            
            var items = data.pantry.data.content || [];
            var expiringCount = items.filter(function(item) {
                if (!item.expiresOn) return false;
                var expireDate = new Date(item.expiresOn);
                var today = new Date();
                var diffDays = Math.ceil((expireDate - today) / (1000 * 60 * 60 * 24));
                return diffDays <= 7 && diffDays >= 0;
            }).length;
            
            document.getElementById('expiring-count').textContent = expiringCount;
        })
        .catch(function(error) {
            console.error('ダッシュボードデータの取得に失敗しました:', error);
        });
}

function showRecipes() {
    hideAllPages();
    document.getElementById('recipes-page').classList.remove('hidden');
    loadRecipes();
}

function loadRecipes() {
    axios.get('/recipes')
        .then(function(response) {
            var recipes = response.data.content || [];
            var listDiv = document.getElementById('recipe-list');
            
            if (recipes.length === 0) {
                listDiv.innerHTML = '<div class="col-span-3 text-center text-gray-500 py-12">レシピが見つかりません。最初のレシピを作成しましょう！</div>';
            } else {
                var html = recipes.map(function(recipe) {
                    var ingredients = (recipe.ingredients || []).map(function(ing) {
                        return '<li>' + ing.name + ' - ' + ing.quantity + '</li>';
                    }).join('');

                    var steps = (recipe.steps || '').split('\n').filter(function(s) { return s.trim(); }).map(function(step) {
                        return '<li class="mb-2 block">' + step.trim() + '</li>';
                    }).join('');

                    var tags = recipe.tags ? '<div class="mb-3"><span class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">' + recipe.tags.split(',').join('</span> <span class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">') + '</span></div>' : '';

                    return '<div class="bg-white p-6 rounded-lg shadow hover:shadow-lg transition-shadow">' +
                        '<h3 class="text-xl font-semibold text-gray-900 mb-2">' + recipe.title + '</h3>' +
                        tags +
                        (recipe.cookTimeMin ? '<p class="text-sm text-gray-500 mb-3">⏱ 調理時間: ' + recipe.cookTimeMin + '分</p>' : '') +
                        '<div class="mb-4">' +
                        '<h4 class="font-medium text-gray-700 mb-2">材料:</h4>' +
                        '<ul class="list-disc list-inside text-sm text-gray-600">' + ingredients + '</ul>' +
                        '</div>' +
                        '<div class="mb-4">' +
                        '<h4 class="font-medium text-gray-700 mb-2">作り方:</h4>' +
                        '<ol class="list-decimal pl-5 text-sm text-gray-600 space-y-2">' + steps + '</ol>' +
                        '</div>' +
                        '<button onclick="deleteRecipe(' + recipe.id + ')" class="text-red-600 hover:text-red-800 text-sm font-medium">削除</button>' +
                        '</div>';
                }).join('');
                listDiv.innerHTML = html;
            }
        })
        .catch(function(error) {
            console.error('レシピの読み込みに失敗しました:', error);
        });
}

function showRecipeForm() {
    document.getElementById('recipe-modal').classList.remove('hidden');
}

function hideRecipeForm() {
    document.getElementById('recipe-modal').classList.add('hidden');
    document.getElementById('recipe-title').value = '';
    document.getElementById('recipe-description').value = '';
    document.getElementById('recipe-instructions').value = '';
    document.getElementById('ingredients-list').innerHTML =
        '<div class="flex gap-2 mb-2">' +
        '<input type="text" placeholder="材料名" required class="flex-1 px-3 py-2 border border-gray-300 rounded-md ingredient-name" />' +
        '<input type="text" placeholder="分量" required class="w-32 px-3 py-2 border border-gray-300 rounded-md ingredient-quantity" />' +
        '</div>';
}

function addIngredientField() {
    var listDiv = document.getElementById('ingredients-list');
    var newField = document.createElement('div');
    newField.className = 'flex gap-2 mb-2';
    newField.innerHTML =
        '<input type="text" placeholder="材料名" required class="flex-1 px-3 py-2 border border-gray-300 rounded-md ingredient-name" />' +
        '<input type="text" placeholder="分量" required class="w-32 px-3 py-2 border border-gray-300 rounded-md ingredient-quantity" />' +
        '<button type="button" onclick="this.parentElement.remove()" class="px-3 py-2 bg-red-500 text-white rounded-md hover:bg-red-600">削除</button>';
    listDiv.appendChild(newField);
}

function handleRecipeSubmit(event) {
    event.preventDefault();
    
    var title = document.getElementById('recipe-title').value;
    var description = document.getElementById('recipe-description').value;
    var instructions = document.getElementById('recipe-instructions').value;
    
    var ingredientNames = [];
    var ingredientQuantities = [];
    document.querySelectorAll('.ingredient-name').forEach(function(el) {
        ingredientNames.push(el.value);
    });
    document.querySelectorAll('.ingredient-quantity').forEach(function(el) {
        ingredientQuantities.push(el.value);
    });
    
    var ingredients = ingredientNames.map(function(name, idx) {
        return { name: name, quantity: ingredientQuantities[idx] };
    });
    
    axios.post('/recipes', { 
        title: title, 
        description: description, 
        instructions: instructions, 
        ingredients: ingredients 
    })
    .then(function() {
        hideRecipeForm();
        loadRecipes();
    })
    .catch(function(error) {
        var message = (error.response && error.response.data && error.response.data.detail) || '不明なエラー';
        alert('レシピの作成に失敗しました: ' + message);
    });
}

function deleteRecipe(id) {
    if (!confirm('このレシピを削除してもよろしいですか？')) return;
    
    axios.delete('/recipes/' + id)
        .then(function() {
            loadRecipes();
        })
        .catch(function(error) {
            console.error('レシピの削除に失敗しました:', error);
        });
}

function showPantry() {
    hideAllPages();
    document.getElementById('pantry-page').classList.remove('hidden');
    loadPantry();
}

function loadPantry() {
    axios.get('/pantry')
        .then(function(response) {
            var items = response.data.content || [];
            var listDiv = document.getElementById('pantry-list');
            
            if (items.length === 0) {
                listDiv.innerHTML = '<div class="p-6 text-center text-gray-500">在庫アイテムが見つかりません。最初のアイテムを追加しましょう！</div>';
            } else {
                var itemsHtml = items.map(function(item) {
                    var isExpired = item.expiresOn && new Date(item.expiresOn) < new Date();
                    var isExpiringSoon = false;
                    
                    if (!isExpired && item.expiresOn) {
                        var expireDate = new Date(item.expiresOn);
                        var today = new Date();
                        var diffDays = Math.ceil((expireDate - today) / (1000 * 60 * 60 * 24));
                        isExpiringSoon = diffDays <= 7 && diffDays >= 0;
                    }
                    
                    var bgClass = isExpired ? 'bg-red-50' : isExpiringSoon ? 'bg-yellow-50' : '';
                    var textClass = isExpired ? 'text-red-600 font-semibold' : isExpiringSoon ? 'text-yellow-600 font-semibold' : 'text-gray-600';
                    
                    var expiresText = '';
                    if (item.expiresOn) {
                        var prefix = isExpired ? '期限切れ: ' : isExpiringSoon ? 'まもなく期限切れ: ' : '賞味期限: ';
                        expiresText = '<p class="text-sm ' + textClass + '">' + prefix + new Date(item.expiresOn).toLocaleDateString() + '</p>';
                    }
                    
                    return '<li class="p-4 hover:bg-gray-50 ' + bgClass + '">' +
                        '<div class="flex justify-between items-start">' +
                        '<div class="flex-1">' +
                        '<h3 class="text-lg font-medium text-gray-900">' + item.ingredientName + '</h3>' +
                        '<p class="text-sm text-gray-600">数量: ' + item.amount + '</p>' +
                        expiresText +
                        '</div>' +
                        '<button onclick="deletePantryItem(' + item.id + ')" class="ml-4 text-red-600 hover:text-red-800 text-sm">削除</button>' +
                        '</div>' +
                        '</li>';
                }).join('');
                
                listDiv.innerHTML = '<ul class="divide-y divide-gray-200">' + itemsHtml + '</ul>';
            }
        })
        .catch(function(error) {
            console.error('在庫の読み込みに失敗しました:', error);
        });
}

function showPantryForm() {
    document.getElementById('pantry-modal').classList.remove('hidden');
}

function hidePantryForm() {
    document.getElementById('pantry-modal').classList.add('hidden');
    document.getElementById('pantry-ingredient').value = '';
    document.getElementById('pantry-amount').value = '';
    document.getElementById('pantry-expires').value = '';
}

function handlePantrySubmit(event) {
    event.preventDefault();
    
    var ingredientName = document.getElementById('pantry-ingredient').value;
    var amount = document.getElementById('pantry-amount').value;
    var expiresOn = document.getElementById('pantry-expires').value || null;
    
    axios.post('/pantry', { 
        ingredientName: ingredientName, 
        amount: amount, 
        expiresOn: expiresOn 
    })
    .then(function() {
        hidePantryForm();
        loadPantry();
    })
    .catch(function(error) {
        var message = (error.response && error.response.data && error.response.data.detail) || '不明なエラー';
        alert('アイテムの追加に失敗しました: ' + message);
    });
}

function deletePantryItem(id) {
    if (!confirm('このアイテムを削除してもよろしいですか？')) return;
    
    axios.delete('/pantry/' + id)
        .then(function() {
            loadPantry();
        })
        .catch(function(error) {
            console.error('アイテムの削除に失敗しました:', error);
        });
}

function hideAllPages() {
    document.getElementById('dashboard-page').classList.add('hidden');
    document.getElementById('recipes-page').classList.add('hidden');
    document.getElementById('pantry-page').classList.add('hidden');
}

console.log('App.js読み込み完了');
