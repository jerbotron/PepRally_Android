package com.peprally.jeremy.peprally.custom;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.JsonMarshaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommentsJSONMarshaller extends JsonMarshaller<ArrayList<Comment>> implements DynamoDBMarshaller<ArrayList<Comment>> {
    @Override
    public String marshall(ArrayList<Comment> comments) {
        return convertCommentsToJSON(comments);
    }

    @Override
    public ArrayList<Comment> unmarshall(Class<ArrayList<Comment>> clazz, String json) {
        return decodeJSONComments(json);
    }

    // Static Helpers
    public static ArrayList<Comment> decodeJSONComments(String jsonComments) {
        ArrayList<Comment> comments = new ArrayList<>();
        try {
            JSONArray jsonCommentsArray = new JSONArray(jsonComments);
            for (int i = 0; i < jsonCommentsArray.length(); i++) {
                comments.add(new Comment(jsonCommentsArray.getJSONObject(i)));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return comments;
    }

    public static String convertCommentsToJSON(ArrayList<Comment> comments) {
        JSONArray jsonCommentsArray = new JSONArray();
        for (Comment comment : comments) {
            JSONObject jsonComment = comment.toJSONObject();
            jsonCommentsArray.put(jsonComment);
        }
        return jsonCommentsArray.toString();
    }
}
