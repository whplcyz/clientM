package com.mot.dm.clientM.Base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qngv36 on 4/10/2016.
 */
public class ActivityCollector
{
    private  static List<Activity> activities = new ArrayList<Activity>();
    public static void addActivity(Activity activity)
    {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity)
    {
        activities.remove(activity);
    }

    public static void exitAll()
    {
        for (Activity activity: activities)
        {
            if(activity != null && !activity.isFinishing())
            {
                activity.finish();
            }
        }
    }
}