package org.j2b2tmuseum.org.StatusAPI.obj;

public record ServerStat(
        int uniquePlayers,
        int currentPlayers,
        int worldSize,
        String worldSizeUnits
) {

    public ServerStat withUniquePlayers(int uniquePlayers) {
        return new ServerStat(uniquePlayers,
                this.currentPlayers(),
                this.worldSize(),
                this.worldSizeUnits());
    }

    public ServerStat withCurrentPlayers(int currentPlayers) {
        return new ServerStat(this.uniquePlayers,
                currentPlayers,
                this.worldSize(),
                this.worldSizeUnits());
    }

    public ServerStat withWorldSize(int worldSize) {
        return new ServerStat(this.uniquePlayers(),
                this.currentPlayers(),
                worldSize,
                this.worldSizeUnits());
    }

    public ServerStat withWorldSizeUnits(String worldSizeUnits) {
        return new ServerStat(this.uniquePlayers(),
                this.currentPlayers(),
                this.worldSize(),
                worldSizeUnits);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerStat(int players, int currentPlayers1, int size, String sizeUnits)) {
            return this.uniquePlayers == players &&
                    this.worldSize == size &&
                    this.currentPlayers == currentPlayers1 &&
                    this.worldSizeUnits.equals(sizeUnits);
        }
        return this.hashCode() == obj.hashCode();
    }
}
