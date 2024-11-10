import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const setupWebSocket = (onMessageReceived, onError) => {
    const socket = new SockJS('http://localhost:8081/ws');
    const stompClient = new Client({
        webSocketFactory: () => socket,
        debug: function (str) {
            console.log(str);
        },
        onConnect: () => {
            console.log('Connected to WebSocket');
            // Subscribe to necessary topics
            stompClient.subscribe('/topic/quiz/question', (message) => {
                const question = JSON.parse(message.body);
                onMessageReceived('question', question);
            });

            stompClient.subscribe('/topic/quiz/leaderboard', (message) => {
                const leaderboard = JSON.parse(message.body);
                onMessageReceived('leaderboard', leaderboard);
            });

        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
            onError(frame);
        },
    });

    stompClient.activate();

    return stompClient;
};

export default setupWebSocket;
