package com.gjson.androidtools.rxjavaoretrofit.model.impl;


import com.gjson.androidtools.rxjavaoretrofit.bean.WeatherBean;
import com.gjson.androidtools.rxjavaoretrofit.model.callback.OnWeatherCallback;
import com.gjson.androidtools.rxjavaoretrofit.retrofitApi.IWeatherService;
import com.gjson.androidtools.rxjavaoretrofit.retrofitApi.RetrofitManager;
import com.gjson.androidtools.rxjavaoretrofit.model.interfaces.IWeatherModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @DateTime: 2016-07-08 09:57
 * @Author: gjson
 * @Deacription: model层实现类,Retrofit方式
 */
public class WeatherNetModelImpl implements IWeatherModel, Callback<WeatherBean> {

    private OnWeatherCallback listener;

    public WeatherNetModelImpl(OnWeatherCallback callback) {
        this.listener = callback;
    }

    @Override
    public void getWeather() {
        RetrofitManager.getInstance().createService(IWeatherService.class).getWeather().enqueue(this);
    }

    @Override
    public void onResponse(Call<WeatherBean> call, Response<WeatherBean> response) {
        WeatherBean weather = response.body();
        if (listener != null)
            listener.onResponse(weather);
    }

    @Override
    public void onFailure(Call<WeatherBean> call, Throwable t) {
        if (listener != null)
            listener.onFailure(t.getMessage());
    }
}