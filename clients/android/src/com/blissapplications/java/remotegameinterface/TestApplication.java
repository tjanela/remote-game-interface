package com.blissapplications.java.remotegameinterface;

import android.app.Application;
import android.content.Context;

public class TestApplication extends Application {

	  private static TestApplication appInstance;

	  @Override
	  public void onCreate() {
//	    ACRA.init(this);
	    setInstance(this);
	    super.onCreate();
	  }

	  private static void setInstance(TestApplication instance) {
	    appInstance = instance;
	  }

	  public static TestApplication getInstance() {
	    return appInstance;
	  }

	  public static Context getContext() {
	    return getInstance().getApplicationContext();
	  }
}
