## McServerWebStatusAPI

A simple RESTful web server (using SpringBoot) for tracking Minecraft server stats, complete with unit testing!

### What's so special?
I didn't want to open another port on the Minecraft server, instead, 
I wanted to use a cheap VPS that I have to run this web server, and then
have the Minecraft server send HTTP POST requests with the statistics to that VPS periodically.
The web server will hold onto the information and allow anyone to request the information via a public endpoint.

### Security
In order to prevent bad actors from uploading B.S. statistics to the web server,
the relevant endpoints are protected by public and private key authentication, 
sort of like logging into an SSH server. The server generates a public and private keypair on first run,
and the public key stays on the server, you will keep the private key on the Minecraft server and use that
to sign challenges sent by the web server, and use that to make authenticated requests.

### See it in action
We're running it live on [https://2b2tmuseum.org/](https://2b2tmuseum.org/), watch it populate in the header! (Ensure JS is enabled.)
![Image](readme_assets/header.png)

### Disclaimer
This software is written the old-fashioned way with no AI agents.

 Endpoints
---
```
GET /challange/{long}
example (/challenge/43242383)
```
Response:
```json
{"challenge":"byte[32] encoded into a String"}
Example: {"challenge":"89wDBVl4AvgtyCEN8Q0LWIAlJhUyxHvroq+4/eCSPwY="}
```
---
```
GET /stats
```
(no auth required)

Response:
```json
{
  "uniquePlayers": 1381,
  "currentPlayers": 0,
  "worldSize": 1306,
  "worldSizeUnits": "GB"
}
```
---
```
POST /update
```
Content Body:
```json
{
  "challenge": {
    "id": 1,
    "challenge": "pEsE7Y5MrMuAnaNy/ftNVG9U1/x92V/pPJBlNhW+284=",
    "signature": "KARSRVAMzAC9mhMD8JIMKVlxtukTWLWENuOF9jBbhlWdF/oidbP2W7i/Sy8QitgNXNqLMTF9kJRQBW9fFnEIwH+wybc1roampvs2qkdDlPtmD7evzY1EMxB1CAMH3bPsqHog2nXUHcYVlLVGxKMdIzxF7qvQzrz9DMdvzzcVSwZ0t9Acdig1avRqOmTFLrfh8g6mrqIa+VBSnQOzTHY7XLjbP6Qz8fjHOf/XykwpAGapT8IpfSv3wDlFQFg6rC/ouN94HiEbr32nhTWnFOZ2xK/cfWIqbKi3PAvJuPkgbcQwbUEAHjM9hXTQMrrVlbjQTKO6y/NZmIP4vBOLSwGBxQ=="
  },
  "stats": {
    "uniquePlayers": 4783,
    "currentPlayers": 20,
    "worldSize": 10034,
    "worldSizeUnits": "MB"
  }
}
```
Response:
```json
{
  "message": "",
  "state": "OK"
}
``````
Authentication failure response:
```json
{
  "message": "Not authenticated",
  "state": "ERROR"
}
```
---
```
PATCH /update?anyfield=47283&asmanyfieldsasyoudlike=10034
example: PATCH /update?uniquePlayers=47283&worldSize=10034
```
Content Body:
```json
{
  "id": 1,
  "challenge": "yyAQP/zh+YgSJt5oSj8fHqZ/Gi+xuueDuMebtgTP4DY=",
  "signature": "OlvnNqBi4Ge5IKpD1zlK+En4YbB5P+Nf915J5uSi9nExE3UXptTs4qNzeCLP4XJgZ57WQx5ycjgOY1vqVnzePjj60FuukwTzC0yzjzWdQB2lDchBIACjl2o5QEL7Ql09gxqr2aZeImEWhiH5DWmaFLa23G2QRaX5BUp8WvrH0f2fHaeZVPj9DVKGos635GZXmNytmpl5yC4xDb3naG0hkZZeutovwGrWylgW5Hm/+RqGCRk9Wg/8cud1augk17mWIDyS6uzCsTE5lg10kh3bp6ZmnGYBo1JCKLRw5iMF4pvQP7V/g6Go10+qdeYxRjwFTw3YFofFWdNFha6G+QTx3g=="
}
```
Response:
```json
{
  "message": "",
  "state": "OK"
}
```
