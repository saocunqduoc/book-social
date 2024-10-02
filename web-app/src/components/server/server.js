const WebSocket = require('ws');

const server = new WebSocket.Server({ port: 8084 });

server.on('connection', (ws) => {
    console.log('Client connected');

    ws.on('message', (message) => {
        console.log('Received:', message);
        const parsedMessage = JSON.parse(message);

        // Gửi thông điệp đến tất cả client
        broadcast(parsedMessage);
    });

    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

// Hàm để gửi thông điệp đến tất cả client
function broadcast(message) {
    server.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(message));
        }
    });
}

console.log('WebSocket server is running on ws://localhost:8084');