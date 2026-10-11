package org.j2b2tmuseum.org.StatusAPI.obj;

public record ServerStat(
        int uniquePlayers,
        int currentPlayers,
        long worldSizeBytes
) {

    public ServerStat withUniquePlayers(int uniquePlayers) {
        return new ServerStat(uniquePlayers,
                this.currentPlayers(),
                this.worldSizeBytes());
    }

    public ServerStat withCurrentPlayers(int currentPlayers) {
        return new ServerStat(this.uniquePlayers,
                currentPlayers,
                this.worldSizeBytes());
    }

    public ServerStat withWorldSizeBytes(long worldSizeBytes) {
        return new ServerStat(this.uniquePlayers(),
                this.currentPlayers(),
                worldSizeBytes);
    }

    public Result isValid() {
        final String cantBeLessZero = " can't be less than zero";
        if (this.uniquePlayers() < 0) {
            return new Result(State.ERROR, "uniquePlayers" + cantBeLessZero);
        }
        if (this.currentPlayers() < 0) {
            return new Result(State.ERROR, "currentPlayers" + cantBeLessZero);
        }
        if (this.worldSizeBytes() < 0L) {
            return new Result(State.ERROR, "worldSizeBytes" + cantBeLessZero);
        }
        return new Result(State.OK);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerStat(int uniquePlayers1, int currentPlayers1, long worldSizeBytes1)) {
            return this.uniquePlayers == uniquePlayers1 &&
                    this.worldSizeBytes == worldSizeBytes1 &&
                    this.currentPlayers == currentPlayers1;
        }
        return this.hashCode() == obj.hashCode();
    }
}
