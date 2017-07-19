package com.peprally.jeremy.peprally.utils;

import com.amazonaws.regions.Regions;

public class Constants {

    // Amazon Web Services: EC2 Server Constants
//    public static final String PUSH_SERVER_URL = "http://ec2-54-167-226-181.compute-1.amazonaws.com/push";
//    public static final String SOCKETIO_SERVER_URL = "http://ec2-54-167-226-181.compute-1.amazonaws.com";

    // Note: for local testing, change the server URLs to your dev machine's static IP, NOT localhost
    // Device is running on virtual machine so it doesn't listen to localhost of your dev machine
    public static final String PUSH_SERVER_URL = "http://192.168.0.16:8080/push";
    public static final String SOCKETIO_SERVER_URL = "http://192.168.0.16:8080/";

    public final static String IDENTITY_POOL_ID = "us-east-1:62a77974-d33d-4131-8a1d-122db8e07dfa";
    public final static Regions COGNITO_REGION = Regions.US_EAST_1;

    public final static String S3_ROSTER_PHOTOS_URL = "https://s3.amazonaws.com/rosterphotos/";
    public final static String S3_ROSTER_PHOTOS_2016_URL = "https://s3.amazonaws.com/rosterphotos-utaustin-2016/";

    // Integer Constants
    public final static Integer INTEGER_DEFAULT_COUNT = 0;
    public final static Integer INTEGER_INVALID = -1;
}

