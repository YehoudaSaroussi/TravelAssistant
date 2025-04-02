
const chatMessages = document.getElementById('chat-messages');
const messageInput = document.getElementById('message-input');
const sendButton = document.getElementById('send-button');
const suggestionsContainer = document.getElementById('suggestions');
const newChatButton = document.getElementById('new-chat-btn');
const chatHistory = document.getElementById('chat-history');
const statusIndicator = document.getElementById('status-indicator');
const statusText = document.getElementById('status-text');
const userIdElement = document.getElementById('user-id');

let userId = localStorage.getItem('travelAssistantUserId');
if (!userId) {
    userId = 'user_' + Math.random().toString(36).substring(2, 10);
    localStorage.setItem('travelAssistantUserId', userId);
}
userIdElement.textContent = userId;

let conversations = [];

function getUserConversations(uid) {
    return JSON.parse(localStorage.getItem(`chatHistory_${uid}`) || '[]');
}

conversations = getUserConversations(userId);
updateChatHistorySidebar();

if (conversations.length > 0) {
    const activeConversationIndex = conversations.findIndex(conv => !conv.isCompleted);
    if (activeConversationIndex >= 0) {
        loadConversation(activeConversationIndex);
    } else {
        loadConversation(0);
    }
} else {
    addMessageToChat('assistant', 'Hello! I\'m your travel assistant. How can I help you plan your next adventure?');
}

sendButton.addEventListener('click', sendMessage);
messageInput.addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        sendMessage();
    }
});
newChatButton.addEventListener('click', startNewChat);
document.addEventListener('click', e => {
    if (e.target.classList.contains('suggestion-chip')) {
        messageInput.value = e.target.textContent;
        sendMessage();
    }
});


const changeUserBtn = document.getElementById('change-user-btn');
const userModal = document.getElementById('user-modal');
const closeModalBtn = document.querySelector('.close');
const saveUserBtn = document.getElementById('save-user-btn');
const newUserIdInput = document.getElementById('new-user-id');

changeUserBtn.addEventListener('click', () => {
    userModal.style.display = 'block';
    newUserIdInput.value = '';
});

closeModalBtn.addEventListener('click', () => {
    userModal.style.display = 'none';
});

window.addEventListener('click', (event) => {
    if (event.target === userModal) {
        userModal.style.display = 'none';
    }
});

saveUserBtn.addEventListener('click', () => {
    const newUserId = newUserIdInput.value.trim();
    if (newUserId) {
        userId = newUserId;
        localStorage.setItem('travelAssistantUserId', userId);
        userIdElement.textContent = userId;
        
        conversations = getUserConversations(userId);
        
        chatMessages.innerHTML = '';
        
        updateChatHistorySidebar();
        
        userModal.style.display = 'none';
        
        if (conversations.length === 0) {
            addMessageToChat('assistant', 'Hello! I\'m your travel assistant. How can I help you plan your trip today?');
        } else {
            loadConversation(0);
        }
    }
});

const defaultSuggestions = [
    "Where should I travel in summer?",
    "What should I pack for Japan?",
    "Top attractions in Rome?",
    "Best time to visit Australia?"
];
updateSuggestions(defaultSuggestions);


function sendMessage() {
    const message = messageInput.value.trim();
    if (message === '') return;
    
    addMessageToChat('user', message);
    
    messageInput.value = '';
    
    showTypingIndicator();
    
    fetchResponse(message);
    
    saveMessageToHistory('user', message);
}

function fetchResponse(message) {
    const requestBody = {
        userId: userId,
        message: message
    };
    
    fetch('/api/conversation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        removeTypingIndicator();
        
        addMessageToChat('assistant', data.responseMessage);
        
        if (data.suggestions) {
            updateSuggestions(data.suggestions);
        } else {
            updateSuggestions(defaultSuggestions);
        }
        
        saveMessageToHistory('assistant', data.responseMessage);
        
        updateConnectionStatus(true);
    })
    .catch(error => {
        console.error('Error:', error);
        removeTypingIndicator();
        addMessageToChat('assistant', 'Sorry, I encountered an error processing your request.');
        updateConnectionStatus(false, error.message);
    });
}

function addMessageToChat(sender, content) {
    const messageElement = document.createElement('div');
    messageElement.classList.add('message', sender);
    
    const messageContent = document.createElement('div');
    messageContent.classList.add('message-content');
    
    const formattedContent = content
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    
    messageContent.innerHTML = `<p>${formattedContent}</p>`;
    
    const messageTime = document.createElement('div');
    messageTime.classList.add('message-time');
    messageTime.textContent = getCurrentTime();
    
    messageElement.appendChild(messageContent);
    messageElement.appendChild(messageTime);
    
    chatMessages.appendChild(messageElement);
    
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function showTypingIndicator() {
    const typingElement = document.createElement('div');
    typingElement.classList.add('message', 'assistant', 'typing-indicator-container');
    
    const typingIndicator = document.createElement('div');
    typingIndicator.classList.add('typing-indicator');
    
    for (let i = 0; i < 3; i++) {
        const dot = document.createElement('div');
        dot.classList.add('typing-dot');
        typingIndicator.appendChild(dot);
    }
    
    typingElement.appendChild(typingIndicator);
    chatMessages.appendChild(typingElement);
    
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function removeTypingIndicator() {
    const typingIndicator = document.querySelector('.typing-indicator-container');
    if (typingIndicator) {
        typingIndicator.remove();
    }
}

function updateSuggestions(suggestions) {
    const suggestionChips = document.querySelector('.suggestion-chips');
    suggestionChips.innerHTML = '';
    
    suggestions.forEach(suggestion => {
        const chip = document.createElement('button');
        chip.classList.add('suggestion-chip');
        chip.textContent = suggestion;
        suggestionChips.appendChild(chip);
    });
}

function getCurrentTime() {
    const now = new Date();
    return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function updateConnectionStatus(connected, errorMessage = '') {
    if (connected) {
        statusIndicator.className = 'connected';
        statusText.textContent = 'Connected';
    } else {
        statusIndicator.className = 'disconnected';
        statusText.textContent = 'Disconnected';
        console.error('Connection error:', errorMessage);
    }
}


function saveMessageToHistory(sender, content) {
    let currentConversation = conversations[0];
    
    if (!currentConversation || currentConversation.isCompleted) {
        currentConversation = {
            id: Date.now(),
            title: content.substring(0, 30) + (content.length > 30 ? '...' : ''),
            messages: [],
            timestamp: new Date().toISOString(),
            isCompleted: false
        };
        conversations.unshift(currentConversation);
    }
    
    currentConversation.messages.push({
        sender: sender,
        content: content,
        timestamp: new Date().toISOString()
    });
    
    if (sender === 'user' && currentConversation.messages.length === 1) {
        currentConversation.title = content.substring(0, 30) + (content.length > 30 ? '...' : '');
    }
    
    localStorage.setItem(`chatHistory_${userId}`, JSON.stringify(conversations));
    
    updateChatHistorySidebar();
}

function updateChatHistorySidebar() {
    chatHistory.innerHTML = '';
    
    conversations.forEach((conversation, index) => {
        const historyItem = document.createElement('div');
        historyItem.classList.add('history-item');
        historyItem.innerHTML = `
            <i class="fas fa-comment"></i>
            <span>${conversation.title}</span>
        `;
        
        historyItem.addEventListener('click', () => {
            loadConversation(index);
        });
        
        chatHistory.appendChild(historyItem);
    });
}

function loadConversation(index) {
    const conversation = conversations[index];
    
    chatMessages.innerHTML = '';
    
    conversation.messages.forEach(msg => {
        addMessageToChat(msg.sender, msg.content);
    });
    
    conversations.forEach(conv => conv.isCompleted = true);
    conversation.isCompleted = false;
    
    localStorage.setItem(`chatHistory_${userId}`, JSON.stringify(conversations));
    
    updateChatHistorySidebar();
}

function startNewChat() {
    conversations.forEach(conv => conv.isCompleted = true);
    
    chatMessages.innerHTML = '';
    
    addMessageToChat('assistant', 'Hello! I\'m your travel assistant. How can I help you plan your next adventure?');
    
    updateSuggestions(defaultSuggestions);
    
    localStorage.setItem(`chatHistory_${userId}`, JSON.stringify(conversations));
    
    updateChatHistorySidebar();
}
