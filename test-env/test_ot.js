const { Client } = require('@stomp/stompjs');
const WebSocket = require('ws');
const fs = require('fs');

Object.assign(global, { WebSocket });

const baseUrl = 'http://127.0.0.1:8080/api';
const wsUrl = 'ws://127.0.0.1:8080/ws/websocket';
const email = `test_${Date.now()}@gmail.com`;
const password = '123456';

const logs = [];
function log(msg) { logs.push(msg); console.log(msg); }
async function delay(ms) { return new Promise(resolve => setTimeout(resolve, ms)); }

async function runTest() {
    try {
        log("--- STEP 1 & 2: Auth and Create Room ---");
        let loginRes = await fetch(`${baseUrl}/auth/login`, {
            method: 'POST', body: JSON.stringify({ email, password }),
            headers: { 'Content-Type': 'application/json' }
        });
        
        if (loginRes.status !== 200) {
            log(`Login failed with status ${loginRes.status}`);
            const errorText = await loginRes.text();
            log(`Full Error Response: ${errorText}`);
            
            if (loginRes.status === 400 || loginRes.status === 401) {
                log("Attempting registration as fallback...");
                const regRes = await fetch(`${baseUrl}/auth/register`, {
                    method: 'POST', body: JSON.stringify({ name: "User A", email, password }),
                    headers: { 'Content-Type': 'application/json' }
                });
                log(`Registration Status: ${regRes.status}`);
                const regText = await regRes.text();
                log(`Registration Response: ${regText}`);

                loginRes = await fetch(`${baseUrl}/auth/login`, {
                    method: 'POST', body: JSON.stringify({ email, password }),
                    headers: { 'Content-Type': 'application/json' }
                });
            }
        }

        if (loginRes.status !== 200) {
            throw new Error(`Auth failed after registration attempt (Status: ${loginRes.status})`);
        }

        const tokenData = await loginRes.json();
        const token = tokenData.token;
        const headers = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

        const createRes = await fetch(`${baseUrl}/rooms/create`, {
            method: 'POST', body: JSON.stringify({ name: "OT Eval Room", language: "java" }),
            headers
        });
        const roomCode = (await createRes.json()).roomCode;
        log(`Room created: ${roomCode}`);

        log("\n--- STEP 3: Connecting WebSocket ---");
        const client = new Client({
            brokerURL: wsUrl,
            forceWebsockets: true,
            reconnectDelay: 0,
            webSocketFactory: () => new WebSocket(wsUrl, [], { headers: { 'Authorization': `Bearer ${token}` } }),
            onConnect: async () => {
                log("Connected to STOMP Broker!");
                client.subscribe(`/topic/editor/${roomCode}`, (msg) => {
                    const data = JSON.parse(msg.body);
                    log(`[WS] Received broadcast: ${data.operationId} (rev ${data.revision}) - ${data.type} '${data.character}' at ${data.position}`);
                });

                log("\n--- STEP 4 & 5: Sequential Operations ---");
                const op1 = { operationId: "op1", roomCode: roomCode, userId: "user-a", userName: "User A", userColor: "#FF6B6B", type: "INSERT", position: 0, character: "H", revision: 0 };
                client.publish({ destination: `/app/editor/${roomCode}/operation`, body: JSON.stringify(op1) });
                await delay(300);

                const op2 = { operationId: "op2", roomCode: roomCode, userId: "user-a", userName: "User A", userColor: "#FF6B6B", type: "INSERT", position: 1, character: "i", revision: 1 };
                client.publish({ destination: `/app/editor/${roomCode}/operation`, body: JSON.stringify(op2) });
                await delay(1000);

                log("\n--- STEP 6: Verifying Sequential Content ---");
                const res1 = await fetch(`${baseUrl}/rooms/${roomCode}`, { headers });
                const roomData1 = await res1.json();
                log(`Content: "${roomData1.content}" (Revision: ${roomData1.revision})`);

                log("\n--- STEP 7: Concurrent Operations ---");
                const op3 = { operationId: "op3", roomCode: roomCode, userId: "user-a", userName: "User A", userColor: "#FF6B6B", type: "INSERT", position: 0, character: "X", revision: 2 };
                const op4 = { operationId: "op4", roomCode: roomCode, userId: "user-b", userName: "User B", userColor: "#4ECDC4", type: "INSERT", position: 0, character: "Y", revision: 2 };
                client.publish({ destination: `/app/editor/${roomCode}/operation`, body: JSON.stringify(op3) });
                client.publish({ destination: `/app/editor/${roomCode}/operation`, body: JSON.stringify(op4) });
                await delay(1500);

                log("\n--- STEP 8: Verifying Concurrent Content ---");
                const res2 = await fetch(`${baseUrl}/rooms/${roomCode}`, { headers });
                const roomData2 = await res2.json();
                log(`Final Content: "${roomData2.content}" (Revision: ${roomData2.revision})`);

                client.deactivate();
                fs.writeFileSync('result.json', JSON.stringify(logs, null, 2));
            }
        });
        client.activate();
    } catch (e) {
        log("ERROR: " + e.message);
        fs.writeFileSync('result.json', JSON.stringify(logs, null, 2));
    }
}
runTest();
