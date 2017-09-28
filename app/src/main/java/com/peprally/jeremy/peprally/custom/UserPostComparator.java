package com.peprally.jeremy.peprally.custom;

import com.peprally.jeremy.peprally.data.UserPost;
import com.peprally.jeremy.peprally.db_models.DBUserPost;

import java.util.Comparator;

public enum UserPostComparator implements Comparator<UserPost> {
    HOTTEST_SORT {
        public int compare(UserPost p1, UserPost p2) {
            return Integer.valueOf(p1.getFistbumpsCount()).compareTo(p2.getFistbumpsCount());
        }},
    LATEST_SORT {
        public int compare(UserPost p1, UserPost p2) {
            return Long.valueOf(p1.getTimestampSeconds()).compareTo(p2.getTimestampSeconds());
        }};

    public static Comparator<UserPost> decending(final Comparator<UserPost> other) {
        return new Comparator<UserPost>() {
            public int compare(UserPost p1, UserPost p2) {
                return -1 * other.compare(p1, p2);
            }
        };
    }

    public static Comparator<UserPost> getComparator(final UserPostComparator... multipleOptions) {
        return new Comparator<UserPost>() {
            public int compare(UserPost p1, UserPost p2) {
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