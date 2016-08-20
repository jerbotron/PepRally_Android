package com.peprally.jeremy.peprally.custom.preferences;

public class NotificationsPref {

    private boolean notifyDirectFistbump;
    private boolean notifyPostFistbump;
    private boolean notifyCommentFistbump;
    private boolean notifyNewComment;
    private boolean notifyDirectMessage;

    // Constructor
    public NotificationsPref(boolean notifyDirectFistbump,
                             boolean notifyPostFistbump,
                             boolean notifyCommentFistbump,
                             boolean notifyNewComment,
                             boolean notifyDirectMessage)
    {
        this.notifyDirectFistbump = notifyDirectFistbump;
        this.notifyPostFistbump = notifyPostFistbump;
        this.notifyCommentFistbump = notifyCommentFistbump;
        this.notifyNewComment = notifyNewComment;
        this.notifyDirectMessage = notifyDirectMessage;
    }

    // Getters/Setters
    public boolean isNotifyDirectFistbump() {
        return notifyDirectFistbump;
    }
    public void setNotifyDirectFistbump(boolean notifyDirectFistbump) {
        this.notifyDirectFistbump = notifyDirectFistbump;
    }

    public boolean isNotifyPostFistbump() {
        return notifyPostFistbump;
    }
    public void setNotifyPostFistbump(boolean notifyPostFistbump) {
        this.notifyPostFistbump = notifyPostFistbump;
    }

    public boolean isNotifyCommentFistbump() {
        return notifyCommentFistbump;
    }
    public void setNotifyCommentFistbump(boolean notifyCommentFistbump) {
        this.notifyCommentFistbump = notifyCommentFistbump;
    }

    public boolean isNotifyNewComment() {
        return notifyNewComment;
    }
    public void setNotifyNewComment(boolean notifyNewComment) {
        this.notifyNewComment = notifyNewComment;
    }

    public boolean isNotifyDirectMessage() {
        return notifyDirectMessage;
    }
    public void setNotifyDirectMessage(boolean notifyDirectMessage) {
        this.notifyDirectMessage = notifyDirectMessage;
    }
}
