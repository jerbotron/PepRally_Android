package com.peprally.jeremy.peprally.db_models.json_marshallers;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshaller;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.JsonMarshaller;
import com.peprally.jeremy.peprally.custom.Feedback;
import com.peprally.jeremy.peprally.custom.FeedbackContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FeedbacksJSONMarshaller extends JsonMarshaller<FeedbackContainer> implements DynamoDBMarshaller<FeedbackContainer> {

    @Override
    public String marshall(FeedbackContainer feedbackContainer) {
        JSONObject jsonFeedbacks = new JSONObject();
        try {
            ArrayList<Feedback> feedbacksList = feedbackContainer.getFeedbacks();
            if (feedbacksList != null) {
                JSONArray jsonFeedbacksArray = new JSONArray();
                for (Feedback feedback : feedbacksList) {
                    JSONObject jsonFeedback = new JSONObject();
                    jsonFeedback.put("username", feedback.getUsername());
                    jsonFeedback.put("feedback", feedback.getFeedback());
                    jsonFeedback.put("timestamp", feedback.getTimestamp());
                    jsonFeedback.put("platform", feedback.getPlatform().toInt());
                    jsonFeedback.put("feedback_type", feedback.getFeedbackType().toInt());
                    jsonFeedbacksArray.put(jsonFeedback);
                }
                jsonFeedbacks.put("feedbacks", jsonFeedbacksArray);
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonFeedbacks.toString();
    }

    @Override
    public FeedbackContainer unmarshall(Class<FeedbackContainer> clazz, String json) {
        try {
            JSONObject jsonFeedbacks = new JSONObject(json);
            ArrayList<Feedback> feedbacksList = new ArrayList<>();
            JSONArray jsonFeedbacksArray = jsonFeedbacks.getJSONArray("feedbacks");
            for (int i = 0; i < jsonFeedbacksArray.length(); i++) {
                feedbacksList.add(new Feedback(jsonFeedbacksArray.getJSONObject(i)));
            }
            return new FeedbackContainer(feedbacksList);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
