package org.j2b2tmuseum.org.StatusAPI.auth;

import org.j2b2tmuseum.org.StatusAPI.ChallengeComponent;
import org.j2b2tmuseum.org.StatusAPI.obj.Result;
import org.j2b2tmuseum.org.StatusAPI.obj.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChallengeController {

    @Autowired
    private ChallengeComponent challengeComponent;

    @GetMapping("/challenge/{id}")
    public Challenge getChallenge(@PathVariable Long id) {
        byte[] challenge = challengeComponent.getChallengeManager().issueChallenge(id);
        return new Challenge(challenge);
    }

    @PostMapping("challenge/{id}")
    public Result postChallenge(@PathVariable(required = false) Long id, @RequestBody SignedChallenge signedChallenge) {
        if (id == null) id = signedChallenge.id();
        byte[] challenge = challengeComponent.getChallengeManager().retrieveChallenge(id);
        if (challenge == null) {
            return new Result(State.ERROR, "No authentication challenge is cached for user " + id);
        }
        boolean flag = challengeComponent.getChallengeManager().verifyChallenge(id, signedChallenge);
        return new Result(flag ? State.OK : State.ERROR);
    }
}
