package com.peprally.jeremy.peprally.utils;

public class Team implements Comparable<Team> {
    public String name;
    public int photoId;

    public Team(String name, int photoId) {
        this.name = name;
        this.photoId = photoId;
    }

    @Override
    public int compareTo(Team another) {
        return name.compareTo(another.name);
    }
}
