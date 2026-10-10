package org.j2b2tmuseum.org.StatusAPI.updatestats;

import org.j2b2tmuseum.org.StatusAPI.auth.SignedChallenge;
import org.j2b2tmuseum.org.StatusAPI.obj.ServerStat;

public record PostUpdateRequest(SignedChallenge challenge, ServerStat stats) {
}
