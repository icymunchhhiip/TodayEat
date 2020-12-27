package com.jinsun.todayeat.login;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.jinsun.todayeat.MainActivity;
import com.jinsun.todayeat.R;
import com.jinsun.todayeat.network.HttpClient;
import com.jinsun.todayeat.network.HttpInterface;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;

public class SessionCallback implements ISessionCallback {

    private Context mContext;

    public SessionCallback(Context context) {
        mContext = context;
    }

    // 로그인에 성공한 상태
    @Override
    public void onSessionOpened() {
        requestMe();
    }

    // 로그인에 실패한 상태
    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }

    // 사용자 정보 요청
    public void requestMe() {
        UserManagement.getInstance()
                .me(new MeV2ResponseCallback() {
                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        Log.i("KAKAO_API", "사용자 아이디: " + result.getId());
                        Single.fromCallable(() -> {
                            HttpInterface httpInterface = HttpClient.getServerClient().create(HttpInterface.class);
                            Call<String> call = httpInterface.setUser((int) result.getId(),"insert");
                            String response = call.execute().body();
                            return response.equals("true");
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSuccess(aBoolean -> {
                                    if (aBoolean) {
                                        MainActivity.sMyId = result.getId();
                                        Toast.makeText(mContext, R.string.login_success, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(mContext, R.string.login_fail, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .subscribe();
                    }
                });
    }
}
