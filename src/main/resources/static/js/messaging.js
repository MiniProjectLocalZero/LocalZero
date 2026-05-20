let currentConversationUserId = null;

function getCsrfToken() {
    // Attempt to extract CSRF token if meta tags are present in Thymeleaf
    const tokenElement = document.querySelector('meta[name="_csrf"]');
    const headerElement = document.querySelector('meta[name="_csrf_header"]');

    if (tokenElement && headerElement) {
        return {
            header: headerElement.getAttribute('content'),
            token: tokenElement.getAttribute('content')
        };
    }
    return null;
}

function fetchWithCsrf(url, options = {}) {
    const csrf = getCsrfToken();
    if (csrf) {
        if (!options.headers) {
            options.headers = {};
        }
        options.headers[csrf.header] = csrf.token;
    }
    return fetch(url, options);
}

function getMessagingModal() {
    return document.getElementById('messagingModal');
}

function getMessagingContent() {
    return document.getElementById('messagingContent');
}

function setMessagingOverlayState(isOpen) {
    const modal = getMessagingModal();
    if (!modal) {
        return;
    }

    modal.classList.toggle('is-open', isOpen);
    modal.setAttribute('aria-hidden', String(!isOpen));
    document.body.style.overflow = isOpen ? 'hidden' : '';
}

function openMessagingModal() {
    const content = getMessagingContent();
    if (!content) {
        return;
    }

    setMessagingOverlayState(true);

    fetchWithCsrf('/messages/inbox?fragment=modal')
        .then(response => {
            if (!response.ok) throw new Error('Failed to load messages');
            return response.text();
        })
        .then(html => {
            content.innerHTML = html;
            bindMessagingOverlayEvents();
            updateMessageBadge();
        })
        .catch(err => {
            console.error('Error fetching messaging modal: ', err);
            content.innerHTML = '<div class="messaging-empty-state"><div class="messaging-empty-card"><p class="messaging-empty-title">Could not load messages</p><p>Please try again in a moment.</p></div></div>';
        });
}

function closeMessagingModal() {
    const content = getMessagingContent();
    if (content) {
        content.innerHTML = '';
    }
    currentConversationUserId = null;
    setMessagingOverlayState(false);
}

function loadConversation(userId) {
    currentConversationUserId = userId;

    fetchWithCsrf(`/messages/conversation/${userId}?fragment=thread`)
        .then(response => {
            if (!response.ok) throw new Error('Failed to load conversation');
            return response.text();
        })
        .then(html => {
            const chatArea = document.getElementById('messaging-chat-area');
            if (chatArea) {
                chatArea.innerHTML = html;
                scrollToBottom();
                initAutoExpandTextarea();

                // Trigger mobile slide-in
                const shellBody = document.querySelector('.messaging-shell-body');
                if (shellBody) shellBody.classList.add('show-chat');
            }

            updateActiveConversation(userId);
            refreshConversationList(); // Sync sidebar dots and unread badge immediately
        })
        .catch(err => {
            console.error('Error fetching conversation: ', err);
            alert('Could not load conversation');
        });
}

function markAsUnread(event, messageId) {
    if (event) {
        event.stopPropagation();
    }

    fetchWithCsrf(`/messages/${messageId}/unread`, { method: 'POST' })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(`Failed to mark as unread: ${response.status} ${text}`);
                });
            }
            refreshConversationList();
        })
        .catch(err => {
            console.error('Error marking message as unread: ', err);
        });
}

function handleConversationKeydown(event, userId) {
    if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        loadConversation(userId);
    }
}

function initAutoExpandTextarea() {
    const textarea = document.getElementById('messageContent');
    if (!textarea) return;

    const adjustHeight = function() {
        this.style.height = 'auto';
        const newHeight = Math.min(this.scrollHeight, 120);
        this.style.height = newHeight + 'px';
        
        if (this.scrollHeight > 120) {
            this.style.overflowY = 'auto';
        } else {
            this.style.overflowY = 'hidden';
        }
    };

    textarea.addEventListener('input', adjustHeight);
    
    // Handle Enter to send, Shift+Enter for new line
    textarea.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            const form = this.closest('form');
            if (form && this.value.trim().length > 0) {
                sendMessage(new Event('submit'), form);
            }
        }
    });

    // Initial adjustment in case there is content
    adjustHeight.call(textarea);
}

function closeConversationMobile() {
    const shellBody = document.querySelector('.messaging-shell-body');
    if (shellBody) shellBody.classList.remove('show-chat');
}


function sendMessage(event, form) {
     event.preventDefault();

     const formData = new FormData(form);
     const params = new URLSearchParams();
     for(const pair of formData.entries()) {
         params.append(pair[0], pair[1]);
     }

     const receiverId = formData.get('receiver.id');
     if (!receiverId) return;

     const fetchOptions = {
         method: 'POST',
         headers: {
             'Content-Type': 'application/x-www-form-urlencoded',
         },
         body: params.toString()
     };

     // Add CSRF token to the headers as a backup
     // (the token is already in the form data as a parameter)
     const csrf = getCsrfToken();
     if (csrf && fetchOptions.headers) {
         fetchOptions.headers[csrf.header] = csrf.token;
     }

     fetchWithCsrf('/messages/send?fragment=thread', fetchOptions)
     .then(response => {
         if (!response.ok) {
             console.error('Response status:', response.status);
             throw new Error(`Failed to send message (${response.status})`);
         }
         return response.text();
     })
     .then(html => {
         const chatArea = document.getElementById('messaging-chat-area');
         if (chatArea) {
             chatArea.innerHTML = html;
             scrollToBottom();
             initAutoExpandTextarea();
         }

         const textarea = form.querySelector('textarea');
         if (textarea) {
             textarea.value = '';
         }

         currentConversationUserId = receiverId;
         refreshConversationList();
     })
     .catch(err => {
         console.error('Error sending message: ', err);
         alert('Could not send message. Please try again.');
     });
 }

function refreshConversationList() {
    fetchWithCsrf('/messages/inbox?fragment=list')
        .then(response => {
            if (!response.ok) throw new Error('Failed to refresh conversation list');
            return response.text();
        })
        .then(html => {
            const sidebar = document.getElementById('messaging-conversation-list');
            if (sidebar) {
                sidebar.innerHTML = html;
                updateActiveConversation(currentConversationUserId);
            }
            updateMessageBadge();
        })
        .catch(err => console.error('Error refreshing list: ', err));
}

function updateMessageBadge() {
    const badge = document.getElementById('message-badge');
    
    // Prioritize getting the most recent unread count from the list fragment
    const listShell = document.querySelector('.conversation-list-shell');
    const modalShell = document.querySelector('.messaging-shell');
    
    let unreadCount = null;
    if (listShell && listShell.hasAttribute('data-unread-count')) {
        unreadCount = listShell.getAttribute('data-unread-count');
    } else if (modalShell && modalShell.hasAttribute('data-unread-count')) {
        unreadCount = modalShell.getAttribute('data-unread-count');
    }

    if (badge && unreadCount !== null) {
        const count = parseInt(unreadCount);
        if (count > 0) {
            badge.textContent = count;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }
}

function updateActiveConversation(userId) {
    const activeItems = document.querySelectorAll('.conversation-item.is-active');
    activeItems.forEach(item => item.classList.remove('is-active'));

    if (!userId) {
        return;
    }

    const target = Array.from(document.querySelectorAll('.conversation-item')).find(item => {
        return item.getAttribute('data-user-id') === String(userId);
    });

    if (target) {
        target.classList.add('is-active');
    }
}

function bindMessagingOverlayEvents() {
    const modal = getMessagingModal();
    if (!modal) {
        return;
    }

    modal.onclick = function (event) {
        if (event.target === modal) {
            closeMessagingModal();
        }
    };

    document.onkeydown = function (event) {
        if (event.key === 'Escape' && modal.classList.contains('is-open')) {
            closeMessagingModal();
        }
    };
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('messages-container');
    if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

// User Picker Functions
function openUserPicker() {
    const modal = document.getElementById('userPickerModal');
    if (modal) {
        modal.style.display = 'flex';
        const searchInput = document.getElementById('userSearchInput');
        if (searchInput) {
            searchInput.value = '';
            searchInput.focus();
        }
        fetchCommunityUsers();
    }
}

function closeUserPicker() {
    const modal = document.getElementById('userPickerModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function fetchCommunityUsers() {
    fetchWithCsrf('/messages/community-users')
        .then(r => {
            if (!r.ok) throw new Error('Failed to fetch community users');
            return r.json();
        })
        .then(users => {
            const list = document.getElementById('userPickerList');
            if (!list) return;

            if (users.length === 0) {
                list.innerHTML = '<div class="user-picker-empty"><p>No other members available</p></div>';
                return;
            }

            list.innerHTML = users.map(user => `
                <button type="button" class="user-picker-item" onclick="startConversationWith(${user.id})">
                    <div class="conversation-avatar">
                        <span>${(user.username || '?').substring(0, 1).toUpperCase()}</span>
                    </div>
                    <div>
                        <div class="user-picker-item-name">
                            ${user.username || 'Unknown'} 
                            ${user.representative ? '<span class="rep-badge-inline">(Representative)</span>' : ''}
                        </div>
                        <div class="user-picker-item-community">${user.communityName || 'Community'}</div>
                    </div>
                </button>
            `).join('');
        })
        .catch(err => {
            console.error('Error fetching community users: ', err);
            const list = document.getElementById('userPickerList');
            if (list) {
                list.innerHTML = '<div class="user-picker-empty"><p>Could not load community members</p></div>';
            }
        });
}

function openBroadcastModal() {
    const modal = document.getElementById('broadcastModal');
    if (modal) {
        modal.style.display = 'flex';
        const textarea = document.getElementById('broadcastContent');
        if (textarea) {
            textarea.value = '';
            textarea.focus();
        }
    }
}

function closeBroadcastModal() {
    const modal = document.getElementById('broadcastModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function sendBroadcast(event) {
    if (event) event.preventDefault();
    
    const content = document.getElementById('broadcastContent').value;
    if (!content.trim()) return;

    const params = new URLSearchParams();
    params.append('content', content);

    fetchWithCsrf('/messages/broadcast', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params.toString()
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to send broadcast');
        return response.json();
    })
    .then(data => {
        closeBroadcastModal();
        alert('Broadcast sent successfully to all community members!');
        refreshConversationList();
    })
    .catch(err => {
        console.error('Error sending broadcast: ', err);
        alert('Could not send broadcast. Please try again.');
    });
}


function filterUsers(query) {
    const items = document.querySelectorAll('.user-picker-item');
    const lowerQuery = query.toLowerCase();

    items.forEach(item => {
        const nameEl = item.querySelector('.user-picker-item-name');
        const name = nameEl ? nameEl.textContent.toLowerCase() : '';
        item.style.display = name.includes(lowerQuery) ? '' : 'none';
    });
}

function startConversationWith(userId) {
    closeUserPicker();
    loadConversation(userId);
}
