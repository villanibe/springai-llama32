    var askButton = document.getElementById('submitBtn');
    var chatContainer = document.getElementById('chat-messages');
    var userInput = document.getElementById('messageInput');

askButton.addEventListener('click', () => {
    var inputText = userInput.value;
    //var url = '/api/chat/streaming?question=' + encodeURIComponent(inputText);
    var url = 'api/chat/streaming-object?question=' + encodeURIComponent(inputText);
    // clear the input field
    userInput.value = '';

    // block button and input text area
    askButton.disabled = true;
    askButton.innerText = 'Processing...';
    userInput.disabled = true;

    // Add user message to chat messages
    chatContainer.innerHTML += `<div class="chat-area message-content"><strong>You:</strong>${inputText}<span class="time-right">${new Date().toLocaleTimeString()}</span></div>`;

    keepScrolling();

    callChat(url);
});

function callChat(url) {

    // Read messages as server sent events.
    const eventSource = new EventSource(url, { withCredentials: true });

    eventSource.addEventListener('open', (event) => {
        console.log('Connection was opened');
    });

    // Add AI message to chat messages
    chatContainer.innerHTML += `<div class="chat-area-darker message-content"><strong>AI:</strong><span class="time-right">${new Date().toLocaleTimeString()}</span></div>`;

    eventSource.addEventListener('llmResponse', (event) => {
    //update with server sent events
        var chatResponse = JSON.parse(event.data);
        var nodes = document.querySelectorAll('.chat-area-darker')
        var lastMessageElement = nodes[nodes.length-1];
        lastMessageElement.innerHTML += chatResponse.value;
        keepScrolling();

    });

    eventSource.addEventListener('error', (event) => {
        eventSource.close();
        askButton.disabled = false;
        askButton.innerText = 'Send'
        userInput.disabled = false;
        console.error("Error occurred:", event);
    });
}

function keepScrolling() {
    // Automatically scroll down whenever the messages change
    chatContainer.scrollTop = chatContainer.scrollHeight;
}