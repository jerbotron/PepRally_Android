package com.peprally.jeremy.peprally.network;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.util.Arrays;

public class NetworkHelpers {

    public static Pair<String, Long> parsePostId(String postId) {
        String [] splitArray = postId.split("_");
        String postUsername = TextUtils.join("_", Arrays.copyOfRange(splitArray, 0, splitArray.length - 1));
        Long timestampSeconds = Long.valueOf(splitArray[splitArray.length - 1]);
        return new Pair<>(postUsername, timestampSeconds);
    }
}
