package org.j2b2tmuseum.org.StatusAPI.updatestats;

import org.j2b2tmuseum.org.StatusAPI.ChallengeComponent;
import org.j2b2tmuseum.org.StatusAPI.StatusApiApplication;
import org.j2b2tmuseum.org.StatusAPI.auth.SignedChallenge;
import org.j2b2tmuseum.org.StatusAPI.obj.Result;
import org.j2b2tmuseum.org.StatusAPI.obj.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class UpdateStatsController {

    @Autowired
    private ChallengeComponent challengeComponent;

    @PostMapping("/update")
    public Result update
            (@RequestBody PostUpdateRequest postUpdateRequest) {
        if (!StatusApiApplication.isKeyPairReady()) {
            return new Result(State.ERROR, "Can't authenticate");
        }
        boolean pass = challengeComponent.getChallengeManager().verifyChallenge(postUpdateRequest.challenge());
        if (!pass) {
            return new Result(State.ERROR, "Not authenticated");
        }
        StatusApiApplication.globalServerStat = postUpdateRequest.stats();
        return new Result(State.OK);
    }

    @PatchMapping("/update")
    public Result update
            (@RequestBody SignedChallenge auth,
             @RequestParam(required = false) Integer uniquePlayers,
             @RequestParam(required = false) Integer currentPlayers,
             @RequestParam(required = false) Integer worldSize,
             @RequestParam(required = false) String worldSizeUnits)
    {
        if (!StatusApiApplication.isKeyPairReady()) {
            return new Result(State.ERROR, "Can't authenticate");
        }
        boolean pass = challengeComponent.getChallengeManager().verifyChallenge(auth);
        if (!pass) {
            return new Result(State.ERROR, "Not authenticated");
        }
        AtomicInteger i = new AtomicInteger(0);
        if (uniquePlayers != null) {
            i.getAndIncrement();
            StatusApiApplication.globalServerStat =
                    StatusApiApplication.globalServerStat.withUniquePlayers(uniquePlayers);
        }
        if (currentPlayers != null) {
            i.getAndIncrement();
            StatusApiApplication.globalServerStat =
                    StatusApiApplication.globalServerStat.withCurrentPlayers(currentPlayers);
        }
        if (worldSize != null) {
            i.getAndIncrement();
            StatusApiApplication.globalServerStat =
                    StatusApiApplication.globalServerStat.withWorldSize(worldSize);
        }
        if (worldSizeUnits != null) {
            i.getAndIncrement();
            StatusApiApplication.globalServerStat =
                    StatusApiApplication.globalServerStat.withWorldSizeUnits(worldSizeUnits);
        }
        if (i.get() == 0) {
            return new Result(State.ERROR, "No parameters were passed");
        }
        return new Result(State.OK);
    }
}
