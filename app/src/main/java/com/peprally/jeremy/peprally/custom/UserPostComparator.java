package com.peprally.jeremy.peprally.custom;

import com.peprally.jeremy.peprally.db_models.DBUserPost;

import java.util.Comparator;

public enum UserPostComparator implements Comparator<DBUserPost> {
    HOTTEST_SORT {
        public int compare(DBUserPost p1, DBUserPost p2) {
            return Integer.valueOf(p1.getFistbumpsCount()).compareTo(p2.getFistbumpsCount());
        }},
    LATEST_SORT {
        public int compare(DBUserPost p1, DBUserPost p2) {
            return Long.valueOf(p1.getTimeInSeconds()).compareTo(p2.getTimeInSeconds());
        }};

    public static Comparator<DBUserPost> decending(final Comparator<DBUserPost> other) {
        return new Comparator<DBUserPost>() {
            public int compare(DBUserPost p1, DBUserPost p2) {
                return -1 * other.compare(p1, p2);
            }
        };
    }

    public static Comparator<DBUserPost> getComparator(final UserPostComparator... multipleOptions) {
        return new Comparator<DBUserPost>() {
            public int compare(DBUserPost p1, DBUserPost p2) {
                for (UserPostComparator option : multipleOptions) {
                    int result = option.compare(p1, p2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }
}