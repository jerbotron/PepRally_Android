package com.peprally.jeremy.peprally;

public class Team implements Comparable<Team> {
    String name;
    int photoId;

    Team(String name, int photoId) {
        this.name = name;
        this.photoId = photoId;
    }

    @Override
    public int compareTo(Team another) {
        return name.compareTo(another.name);
    }
}
