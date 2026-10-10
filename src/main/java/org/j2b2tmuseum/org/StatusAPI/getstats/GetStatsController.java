package org.j2b2tmuseum.org.StatusAPI.getstats;

import org.j2b2tmuseum.org.StatusAPI.StatusApiApplication;
import org.j2b2tmuseum.org.StatusAPI.obj.ServerStat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetStatsController {

    @CrossOrigin(origins = {"*"})
    @GetMapping("/stats")
    public ServerStat stats() {
        return StatusApiApplication.globalServerStat;
    }

}
