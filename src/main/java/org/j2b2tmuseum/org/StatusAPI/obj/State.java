package org.j2b2tmuseum.org.StatusAPI.obj;

public enum State {
    OK ("OK"),
    ERROR("ERROR");

    private String str;
    
    State(String str) {
        this.str= str;
    }

    @Override
    public String toString() {
        return str;
    }
}
