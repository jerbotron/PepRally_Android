package com.peprally.jeremy.peprally.utils;

import com.amazonaws.regions.Regions;

public class Constants {

    // Amazon Web Services: EC2 Server Constants
//    private static final String pushServerURL = "http://peprally-push.dcif4cvzmx.us-west-2.elasticbeanstalk.com/send";
//    public static final String PUSH_SERVER_URL = "http://ec2-107-21-196-112.compute-1.amazonaws.com/send";
    public static final String PUSH_SERVER_URL = "http://ec2-52-90-150-67.compute-1.amazonaws.com/send";
    public static final String SOCKETIO_SERVER_URL = "http://ec2-52-90-150-67.compute-1.amazonaws.com";

    public final static String IDENTITY_POOL_ID = "us-east-1:62a77974-d33d-4131-8a1d-122db8e07dfa";
    public final static Regions COGNITO_REGION = Regions.US_EAST_1;

    public final static String S3_ROSTER_PHOTOS_URL = "https://s3.amazonaws.com/rosterphotos/";
    public final static String S3_ROSTER_PHOTOS_2016_URL = "https://s3.amazonaws.com/rosterphotos-utaustin-2016/";

    // Integer Constants
    public final static Integer INTEGER_DEFAULT_COUNT = 0;
    public final static Integer INTEGER_INVALID = -1;
}

