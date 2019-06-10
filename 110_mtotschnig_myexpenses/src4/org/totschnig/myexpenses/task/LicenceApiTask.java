package org.totschnig.myexpenses.task;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.threeten.bp.LocalDate;
import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.retrofit.ValidationService;
import org.totschnig.myexpenses.util.Result;
import org.totschnig.myexpenses.util.TextUtils;
import org.totschnig.myexpenses.util.licence.Licence;
import org.totschnig.myexpenses.util.licence.LicenceHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.totschnig.myexpenses.preference.PrefKey.LICENCE_EMAIL;
import static org.totschnig.myexpenses.preference.PrefKey.NEW_LICENCE;

public class LicenceApiTask extends AsyncTask<Void, Void, Result> {
  private final TaskExecutionFragment taskExecutionFragment;
  private final int taskId;

  @Inject
  LicenceHandler licenceHandler;

  @Inject
  OkHttpClient.Builder builder;

  @Inject
  @Named("deviceId")
  String deviceId;

  LicenceApiTask(TaskExecutionFragment tTaskExecutionFragment, int taskId) {
    this.taskExecutionFragment = tTaskExecutionFragment;
    this.taskId = taskId;
    MyApplication.getInstance().getAppComponent().inject(this);
  }

  private class DateTimeDeserializer implements JsonDeserializer<LocalDate> {
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
    }
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
  }

  @Override
  protected Result doInBackground(Void... voids) {
    String licenceEmail = LICENCE_EMAIL.getString("");
    String licenceKey = NEW_LICENCE.getString("");
    if ("".equals(licenceKey) || "".equals(licenceEmail)) {
      return Result.FAILURE;
    }

    final OkHttpClient okHttpClient = builder
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new DateTimeDeserializer())
        .create();

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(licenceHandler.getBackendUri())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build();

    ValidationService service = retrofit.create(ValidationService.class);

    if (taskId == TaskExecutionFragment.TASK_VALIDATE_LICENCE) {
      Call<Licence> licenceCall = service.validateLicence(licenceEmail, licenceKey, deviceId);
      try {
        Response<Licence> licenceResponse = licenceCall.execute();
        Licence licence = licenceResponse.body();
        if (licenceResponse.isSuccessful() && licence != null && licence.getType() != null) {
          licenceHandler.updateLicenceStatus(licence);
          return Result.ofSuccess(TextUtils.concatResStrings(MyApplication.getInstance(), " ",
              R.string.licence_validation_success, licence.getType().getResId()));
        } else {
          switch (licenceResponse.code()) {
            case 452:
              licenceHandler.updateLicenceStatus(null);
              return Result.ofFailure(R.string.licence_validation_error_expired);
            case 453:
              licenceHandler.updateLicenceStatus(null);
              return Result.ofFailure(R.string.licence_validation_error_device_limit_exceeded);
            case 404:
              licenceHandler.updateLicenceStatus(null);
              return Result.ofFailure(R.string.licence_validation_failure);
            default:
              return buildFailureResult(String.valueOf(licenceResponse.code()));
          }
        }
      } catch (IOException e) {
        return buildFailureResult(e.getMessage());
      }
    } else if (taskId == TaskExecutionFragment.TASK_REMOVE_LICENCE) {
      Call<Void> licenceCall = service.removeLicence(licenceEmail, licenceKey, deviceId);
      try {
        Response<Void> licenceResponse = licenceCall.execute();
        if (licenceResponse.isSuccessful() || licenceResponse.code() == 404) {
          NEW_LICENCE.remove();
          LICENCE_EMAIL.remove();
          licenceHandler.updateLicenceStatus(null);
          return Result.ofSuccess(R.string.licence_removal_success);
        } else {
          return buildFailureResult(String.valueOf(licenceResponse.code()));
        }
      } catch (IOException e) {
        return buildFailureResult(e.getMessage());
      }
    }
    return Result.FAILURE;
  }

  @NonNull
  private Result buildFailureResult(String s) {
    return Result.ofFailure(R.string.error, s);
  }

  @Override
  protected void onPostExecute(Result result) {
    if (this.taskExecutionFragment.mCallbacks != null) {
      this.taskExecutionFragment.mCallbacks.onPostExecute(taskId, result);
    }
  }
}
