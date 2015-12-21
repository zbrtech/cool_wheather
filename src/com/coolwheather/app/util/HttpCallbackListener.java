package com.coolwheather.app.util;

public interface HttpCallbackListener {

	public void onFinish(String string);

	public void onError(Exception e);

}
