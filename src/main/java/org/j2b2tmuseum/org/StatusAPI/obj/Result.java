package org.j2b2tmuseum.org.StatusAPI.obj;

public class Result {

    private State state;
    private String message;

    public Result() {}

    public Result(State state, String message) {
        this.state = state;
        this.message = message;
    }

    public Result(String state, String message) {
        this.state = State.valueOf(state);
        this.message = message;
    }

    public Result(State state) {
        this.state = state;
        this.message = "";
    }

    public String getMessage() {
        return message;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getState().toString()).append(" - ").append(getMessage());
        return builder.toString();
    }
}
