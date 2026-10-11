## McServerWebStatusAPI

A simple RESTful web server (using SpringBoot) for tracking Minecraft server stats, complete with unit testing!

### What's so special?
I didn't want to open another port on the Minecraft server, instead, 
I wanted to use a cheap VPS that I have to run this web server, and then
have the Minecraft server send HTTP POST requests with the statistics to that VPS periodically.
The web server will hold onto the information and allow anyone to request the information via a public endpoint.
Running this server seperately from the Minecraft server helps protect the Minecraft server as much as possible by reducing
attack surface.

### Security
In order to prevent bad actors and random folk from uploading B.S. statistics to the web server,
the relevant endpoints are protected by public and private key authentication. 
It's sort of like logging into an SSH server. The server generates a public and private keypair on first run,
and the public key stays on the server, and you'll keep the private key on the Minecraft server. You'll use that
to generate signatures for challenges sent by the web server, and use that signature to make authenticated requests.

### See it in action
We're running it live on [https://2b2tmuseum.org/](https://2b2tmuseum.org/), watch it populate in the header! (Ensure JS is enabled.)
![Image](readme_assets/header.png)

### Disclaimer
This software is written the old-fashioned way with no AI agents.

 Endpoints
---
## GET /challenge
```
GET /challange/{long}
GET /challenge/43242383
//              ^ This is your "User ID", you'll need it for when you send your signature.
//              You choose your User ID, best practice is just to randomly generate a Long type.
```
Gets a authentication challenge for you to generate a signature for using your private key. You'll need that to use any
of the endpoints that update the stats.

### Response:
```json
{"challenge":"89wDBVl4AvgtyCEN8Q0LWIAlJhUyxHvroq+4/eCSPwY="}
//             ^ byte[32] encoded as a String in base64
```
---
## GET /stats
```
GET /stats
```
Retrieves the server's statistics, no authentication is required for this endpoint, and it's the only endpoint
annotated with `@CrossOrigin(origins = {"*"})`, so
it can be used on any website, even if the domain is different.

### Response:
```json
{
  "uniquePlayers": 1381, // how many players have joined the server at least once
  "currentPlayers": 0, // players currently online
  "worldSize": 1306, // how big the world is
  "worldSizeUnits": "GB" // what unit is worldSize reported in
}
```
---
## POST /update
```
POST /update
```
This updates the statistics that will be displayed when sending `GET /stats`. You'll need to specify every field.
If only updating one or more values, you can use the `PATCH` endpoint explained further down.

### Content Body:
```json
{
  "challenge": {
    "id": 43242383, // This is your "User ID" that you stored when getting your challenge.
    "challenge": "pEsE7Y5MrMuAnaNy/ftNVG9U1/x92V/pPJBlNhW+284=", 
//                 ^ This is the challenge you received from the server, you'll need to send it back to the server.
//                 If it doesn't match what the server sent you, it won't accept your signature.
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
### Successful Response:
```json
{
  "message": "",
  "state": "OK"
}
```
### Authentication failure response:
```json
{
  "message": "Not authenticated",
  "state": "ERROR"
}
```
---
## PATCH /update
```
PATCH /update?uniquePlayers=47283
PATCH /update?uniquePlayers=47283&worldSize=10034
                                 ^ You can add as many additional parameters as you'd like in any order using the '&' to split them
```
### Content Body:
```json
{
  "id": 43242383, // This is your "User ID" that stored when getting your challenge.
  "challenge": "pEsE7Y5MrMuAnaNy/ftNVG9U1/x92V/pPJBlNhW+284=",
  //                 ^ This is the challenge you received from the server, you'll need to send it back to the server.
  //                 If it doesn't match what the server sent you, it won't accept your signature.
  "signature": "KARSRVAMzAC9mhMD8JIMKVlxtukTWLWENuOF9jBbhlWdF/oidbP2W7i/Sy8QitgNXNqLMTF9kJRQBW9fFnEIwH+wybc1roampvs2qkdDlPtmD7evzY1EMxB1CAMH3bPsqHog2nXUHcYVlLVGxKMdIzxF7qvQzrz9DMdvzzcVSwZ0t9Acdig1avRqOmTFLrfh8g6mrqIa+VBSnQOzTHY7XLjbP6Qz8fjHOf/XykwpAGapT8IpfSv3wDlFQFg6rC/ouN94HiEbr32nhTWnFOZ2xK/cfWIqbKi3PAvJuPkgbcQwbUEAHjM9hXTQMrrVlbjQTKO6y/NZmIP4vBOLSwGBxQ=="
}
```
### Response:
```json
{
  "message": "",
  "state": "OK"
}
```
### Authentication failure response:
```json
{
  "message": "Not authenticated",
  "state": "ERROR"
}
```

## Client-side example
```Java
    private final ObjectMapper mapper;
    private final PrivateKey privateKey;
    private static final long ONE_KEY = 1L;
    private final Cache<Long, SignedChallenge> oneSignatureCache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(25))
        .maximumSize(100)
        .build();
    // ^ com.google.common.cache.Cache is great for this
    
    private void updateStats() { // best to run this on an async thread
        final String url = getMyBukkitPlugin().getConfiguration().getValues().webstatusUrl;
        SignedChallenge signature = oneSignatureCache.getIfPresent(ONE_KEY); // Your challenge expires on the server, make sure your client is aware.
        // You can re-use the Records in the server on your client side too, and use Jackson for (de)serialisation.
        try {
            try (HttpClient client = HttpClient.newHttpClient()) {
                sendDebugMsg("Opened HttpClient");
                // Authenticate if needed.
                if (signature == null || signature.signature().isEmpty()) {
                    sendDebugMsg("Need to authenticate.");
                    currentUser = Math.abs(random.nextLong());
                    HttpRequest challengeReq = HttpRequest.newBuilder()
                            .uri(URI.create(url + "/challenge/" + currentUser))
                            .header("Content-Type", "application/json")
                            .GET()
                            .build();
                    HttpResponse<String> challengeRes = client.send(challengeReq, HttpResponse.BodyHandlers.ofString());
                    sendDebugMsg("The server has given a challenge.");
                    String challengeObj = challengeRes.body();
                    Challenge challenge = mapper.readValue(challengeObj, Challenge.class);
                    //                     ^ com.fasterxml.jackson.databind.ObjectMapper will take care of serialising it.
                    byte[] challengeBytes = challenge.challenge();
                    signature = new SignedChallenge(
                            currentUser, challengeBytes, signChallenge(challengeBytes, this.privateKey));
                    oneSignatureCache.put(ONE_KEY, signature);
                    sendDebugMsg("Generated a signature and cached it for subsequent requests.");
                }
                // Now that you're authenticated, you're authorised to update the statistics.
                sendDebugMsg("Prepare to send stats");
                ServerStat stat = StatsModule.INSTANCE.buildServerStats();
                PostUpdateRequest request = new PostUpdateRequest(signature, stat);
                String json = mapper.writeValueAsString(request);
                HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(json);
                HttpRequest updateReq = HttpRequest.newBuilder()
                        .uri(URI.create(url + "/update"))
                        .header("Content-Type", "application/json")
                        .POST(publisher)
                        .build();
                HttpResponse<String> result = client.send(updateReq, HttpResponse.BodyHandlers.ofString());
                sendDebugMsg("Sent stats");
                String resultBody = result.body();
                Result realResult = mapper.readValue(resultBody, Result.class);
                if (realResult.getState() == State.ERROR) {
                    getMyBukkitPlugin().getLogger().log(Level.SEVERE, "Received error while updating web status");
                    getMyBukkitPlugin().getLogger().log(Level.SEVERE, realResult.getMessage());
                }
            }
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            getMyBukkitPlugin()
                    .getLogger()
                    .log(Level.SEVERE, "Received error while communicating with web status server");
        }
    }

    private static String signChallenge(byte[] challengeBytes, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(challengeBytes);
        byte[] signatureBytes = signer.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }
```