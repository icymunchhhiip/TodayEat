package com.jinsun.todayeat.search;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jinsun.todayeat.Constants;
import com.jinsun.todayeat.MainActivity;
import com.jinsun.todayeat.R;
import com.jinsun.todayeat.databinding.FragmentSearchBinding;
import com.jinsun.todayeat.model.ExceptedPlaceModel;
import com.jinsun.todayeat.model.poi.POIData;
import com.jinsun.todayeat.model.UserRequestData;
import com.jinsun.todayeat.model.poi.POINode;
import com.jinsun.todayeat.model.poi.POIWrap;
import com.jinsun.todayeat.model.poidetail.POIDetailData;
import com.jinsun.todayeat.network.HttpClient;
import com.jinsun.todayeat.network.HttpInterface;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.MarkerIcons;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.jinsun.todayeat.Constants.PERMISSIONS_REQUEST_CODE;
import static com.jinsun.todayeat.exclusionlist.ExclusionListFragment.mAdapter;

public class SearchFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, OnMapReadyCallback {
    private FragmentSearchBinding mFragmentSearchBinding;
    private GpsTracker mGpsTracker;
    private String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private UserRequestData mUserRequestData;
    private NaverMap mNaverMap;
    private Marker mCurrentMarker;
    private CircleOverlay circleOverlay;
    private Marker mResMarker;
    private POINode mSelectedPOIData;
    private InfoWindow mInfoWindow;
    private CustomInfoWindowAdapter mCustomInfoWindowAdapter;
    private POIDetailData mDetailData;
    public static ArrayList<ExceptedPlaceModel> mExceptList = new ArrayList<>();
    private Boolean isSelected = false;
    private POIWrap mPOIWrap;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentSearchBinding = FragmentSearchBinding.inflate(inflater, container, false);

        if (!isEnableLocationService()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        FragmentManager fm = getChildFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        mUserRequestData = new UserRequestData();
        mCurrentMarker = new Marker();
        mCurrentMarker.setCaptionTextSize(14);
        mCurrentMarker.setCaptionText(getString(R.string.user_location));
        mCurrentMarker.setCaptionColor(getContext().getColor(R.color.colorFontDark));
        mCurrentMarker.setSubCaptionTextSize(12);
        mCurrentMarker.setSubCaptionColor(getContext().getColor(R.color.colorFontLight));
        mCurrentMarker.setSubCaptionMinZoom(12);
        mCurrentMarker.setIcon(MarkerIcons.BLACK);
        mCurrentMarker.setIconTintColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));


        circleOverlay = new CircleOverlay();

        mInfoWindow = new InfoWindow();
        mInfoWindow.setOnClickListener(overlay -> {
            String name = mDetailData.getPoiDetailInfo().getName();
            if (name == null) {
                name = mSelectedPOIData.getName().replace("[", " ");
                name = name.replace("]", "");
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_search_base) + mSelectedPOIData.getLowerAddrName() + " " + name));
            startActivity(intent);
            return true;
        });

        mResMarker = new Marker();
        mResMarker.setCaptionTextSize(14);
        mResMarker.setCaptionColor(getContext().getColor(R.color.colorFontDark));
        mResMarker.setSubCaptionTextSize(12);
        mResMarker.setSubCaptionColor(getContext().getColor(R.color.colorFontLight));
        mResMarker.setSubCaptionMinZoom(12);
        mResMarker.setIcon(MarkerIcons.BLACK);
        mResMarker.setIconTintColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mResMarker.setOnClickListener(overlay -> {
//            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
//                @NonNull
//                @Override
//                public CharSequence getText(@NonNull InfoWindow infoWindow) {
//                    return "Marker 1";
//                }
//            });
//            infoWindow.setAnchor(new PointF(0, 1));
//            infoWindow.setOffsetX(getResources().getDimensionPixelSize(R.dimen.custom_info_window_offset_x));
//            infoWindow.setOffsetY(getResources().getDimensionPixelSize(R.dimen.custom_info_window_offset_y));
            if (mResMarker.getInfoWindow() == null) {
                // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                mInfoWindow.open(mResMarker);
            } else {
                // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                mInfoWindow.close();
            }

            return true;
        });

        int color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
        circleOverlay.setColor(ColorUtils.setAlphaComponent(color, 31));
        circleOverlay.setOutlineColor(color);
        circleOverlay.setOutlineWidth(getResources().getDimensionPixelSize(R.dimen.overlay_line_width));

        mFragmentSearchBinding.ibMyLocation
                .setOnClickListener(view -> {
                    mGpsTracker = new GpsTracker(getContext());

                    double lat = mGpsTracker.getLat();
                    double lon = mGpsTracker.getLon();

                    String addr = getCurrentAddress(lat, lon);
                    if (!addr.equals("")) {
                        mUserRequestData.setAddress(addr);
                        mUserRequestData.setLat(lat);
                        mUserRequestData.setLon(lon);
                        mFragmentSearchBinding.tvLocation.setText(addr);
                    }
                });

        mFragmentSearchBinding.tvLocation.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AddressActivity.class);
            intent.putExtra(Constants.EXTRA_ADDRESS, mUserRequestData.getAddress());
            startActivityForResult(intent, Constants.ADDRESS_REQUEST_CODE);
        });

        mUserRequestData.setRadius((int) (mFragmentSearchBinding.rsbRadius.getLeftSeekBar().getProgress()));
//        Toast.makeText(getContext(),mSearchingData.getRadius()+"",Toast.LENGTH_SHORT).show();
        changeSeekBarIndicator(mFragmentSearchBinding.rsbRadius, Float.parseFloat(mFragmentSearchBinding.rsbRadius.getProgressLeft() + ""));
        mFragmentSearchBinding.rsbRadius.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                changeSeekBarIndicator(view, leftValue);
                if (!mUserRequestData.getAddress().equals("")) {
                    showRadiusOnMap();
                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //선택된 반경 값 가져오기
                showRandomPlace();
            }
        });

        mFragmentSearchBinding.cbKorean.setOnCheckedChangeListener(this);
        mFragmentSearchBinding.cbJapanese.setOnCheckedChangeListener(this);
        mFragmentSearchBinding.cbChinese.setOnCheckedChangeListener(this);
        mFragmentSearchBinding.cbWestern.setOnCheckedChangeListener(this);
        mFragmentSearchBinding.cbFlour.setOnCheckedChangeListener(this);

        mFragmentSearchBinding.tvLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!mUserRequestData.getAddress().equals("")) {
                    showCurrentMarkOnMap();
                    showRadiusOnMap();
                }
                showRandomPlace();
            }
        });

        return mFragmentSearchBinding.getRoot();
    }

    private void changeSeekBarIndicator(RangeSeekBar seekBar, Float value) {
        if (value < 5f) {
            seekBar.setIndicatorText("1km");
            mUserRequestData.setRadius(1);
        } else if (value < 10f) {
            seekBar.setIndicatorText("2km");
            mUserRequestData.setRadius(2);
        } else if (value < 15f) {
            seekBar.setIndicatorText("3km");
            mUserRequestData.setRadius(3);
        } else if (value < 20f) {
            seekBar.setIndicatorText("5km");
            mUserRequestData.setRadius(5);
        } else if (value < 25f) {
            seekBar.setIndicatorText("8km");
            mUserRequestData.setRadius(8);
        } else if (value < 30f) {
            seekBar.setIndicatorText("10km");
            mUserRequestData.setRadius(10);
        } else if (value < 35f) {
            seekBar.setIndicatorText("12km");
            mUserRequestData.setRadius(12);
        } else if (value < 40f) {
            seekBar.setIndicatorText("15km");
            mUserRequestData.setRadius(15);
        } else {
            seekBar.setIndicatorText("도/시");
            mUserRequestData.setRadius(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean isGrant = true;

            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    isGrant = false;
                    break;
                }
            }

            if (isGrant) {
                //위치 값을 가져올 수 있음
            } else {
                Toast.makeText(getContext(), "권한을 허용하지 않아 현재 위치를 불러올 수 없습니다.\n직접 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getContext(), "현재 위치를 받아오고 싶다면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }
    }

    private String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getContext(), "지오코더 서비스 사용불가합니다", Toast.LENGTH_LONG).show();
            return "";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getContext(), "잘못된 GPS 좌표입니다", Toast.LENGTH_LONG).show();
            return "";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getContext(), "주소를 발견할 수 없습니다", Toast.LENGTH_SHORT).show();
            return "";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0);
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("위치 서비스가 비활성화됨");
        builder.setMessage("현재 위치를 받아오시려면 위치 권한이 필요합니다.\n위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", (dialog, id) -> {
            Intent callGPSSettingIntent
                    = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, Constants.GPS_ENABLE_REQUEST_CODE);
        });
        builder.setNegativeButton("취소", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.GPS_ENABLE_REQUEST_CODE:
                if (isEnableLocationService()) {
                    if (isEnableLocationService()) {
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
            case Constants.ADDRESS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    //주소 받아서 저장하고 tv에도 보여주기
                    String addr = data.getStringExtra(Constants.EXTRA_ADDRESS);
                    double lat = data.getDoubleExtra(Constants.EXTRA_LAT, -200);
                    double lon = data.getDoubleExtra(Constants.EXTRA_LON, -200);
                    if (addr != null && lat != -200 && lon != 200) {
                        mUserRequestData.setAddress(addr);
                        mUserRequestData.setLat(lat);
                        mUserRequestData.setLon(lon);
                        mFragmentSearchBinding.tvLocation.setText(mUserRequestData.getAddress());
                    }
                }
                break;
        }

    }

    private boolean isEnableLocationService() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.cb_korean:
                mUserRequestData.setKorean(b);
                break;
            case R.id.cb_chinese:
                mUserRequestData.setChinese(b);
                break;
            case R.id.cb_japanese:
                mUserRequestData.setJapanese(b);
                break;
            case R.id.cb_western:
                mUserRequestData.setWestern(b);
                break;
            case R.id.cb_flour:
                mUserRequestData.setFlour(b);
                break;
        }
        showRandomPlace();
    }

    private void showRandomPlace() {
        mFragmentSearchBinding.progressbar.setVisibility(View.VISIBLE);
        if (mUserRequestData.isReadyData()) {
            // 반경, 기준주소, 음식종류로 음식점 검색
            // 후에 그 중 랜덤으로 하나 뽑는다
//            Toast.makeText(getContext(), "검색 준비 완료!", Toast.LENGTH_SHORT).show();


            Single.fromCallable(() -> {

                HttpInterface httpInterface = HttpClient.getTmapClient().create(HttpInterface.class);
                Call<POIData> poiDataCall = httpInterface.getPOIData(
                        Constants.TMAP_VERSION, 1, Constants.POI_MAX_COUNT,
                        mUserRequestData.getFoodsToString(), mUserRequestData.getLon(), mUserRequestData.getLat(),
                        mUserRequestData.getRadius(), getContext().getString(R.string.tmap)
                );

                POIData response = poiDataCall.execute().body();
                if (response == null) {
                    //TODO: null
//                        mSelectedPOIData = null;
                    return false;
                } else {
                    mPOIWrap = response.getSearchPoiInfo();
                    hasResInBoundary();
                }
                return true;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(aBoolean -> {

                        showOnMap(aBoolean);

                        detailDataDisposable();

                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                    })
                    .doOnError(throwable -> {
                        throwable.getStackTrace();
                        Toast.makeText(getContext(), "검색 결과를 찾을 수 없습니다!", Toast.LENGTH_SHORT).show();
                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                    })
                    .subscribe();

        } else {
            Toast.makeText(getContext(), "주소 입력 혹은 음식 종류를 하나 이상 선택해주세요", Toast.LENGTH_SHORT).show();
//          Toast.makeText(getContext(), "lat"+mSearchingData.getLat()+"\nlon"+mSearchingData.getLon()+"\naddr"+mSearchingData.getAddress(), Toast.LENGTH_SHORT).show();
            mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
        }

        //길찾기
        mFragmentSearchBinding.ibKakaoMap.setOnClickListener(view -> {
            String uri = getString(R.string.uri_route_kakao,
                    String.valueOf(mUserRequestData.getLat()),
                    String.valueOf(mUserRequestData.getLon()),
                    mSelectedPOIData.getFrontLat(),
                    mSelectedPOIData.getFrontLon()
            );
            try {
                Intent intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
                Intent existPackage = getContext().getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                if (existPackage != null) {
                    startActivity(intent);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "카카오맵을 먼저 설치해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        mFragmentSearchBinding.ibTmap.setOnClickListener(view -> {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse(
                            getString(R.string.uri_route_tamp,
                                    mSelectedPOIData.getFrontLon(),
                                    mSelectedPOIData.getFrontLat(),
                                    mSelectedPOIData.getName()
                            )
                    ));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "구글맵을 먼저 설치해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        mFragmentSearchBinding.ibGoogleMap.setOnClickListener(view -> {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse(
                            getString(R.string.uri_route_google,
                                    String.valueOf(mUserRequestData.getLat()),
                                    String.valueOf(mUserRequestData.getLon()),
                                    mSelectedPOIData.getFrontLat(),
                                    mSelectedPOIData.getFrontLon()
                            )
                    ));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(
                    "com.google.android.apps.maps",
                    "com.google.android.maps.MapsActivity");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "구글맵을 먼저 설치해주세요", Toast.LENGTH_SHORT).show();
            }
        });
        //전화 걸기
        mFragmentSearchBinding.ibCall.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + mSelectedPOIData.getTelNo()));
            startActivity(intent);
        });

        //TODO: 제외 목록 추가
        mFragmentSearchBinding.btExcept.setOnClickListener(view -> {

            StringBuilder addr = new StringBuilder();
            addr.append(mSelectedPOIData.getUpperAddrName());
            addr.append(" ");
            addr.append(mSelectedPOIData.getMiddleAddrName());
            addr.append(" ");
            addr.append(mSelectedPOIData.getRoadName());
            addr.append(" ");
            addr.append(mSelectedPOIData.getBuildingNo1());
            String tmp = mSelectedPOIData.getBuildingNo2();
            if (!(tmp == null || tmp.equals("0") || tmp.equals(""))) {
                addr.append("-");
                addr.append(mSelectedPOIData.getBuildingNo2());
            }
            ExceptedPlaceModel place = new ExceptedPlaceModel(
                    Integer.parseInt(mSelectedPOIData.getId()),
                    mSelectedPOIData.getName(),
                    mResMarker.getCaptionText(),
                    mSelectedPOIData.getLowerAddrName()
            );

            Single.fromCallable(() -> {

                HttpInterface httpInterface = HttpClient.getServerClient().create(HttpInterface.class);
                Call<String> call = httpInterface.setPlaces(
                        (int) MainActivity.sMyId,
                        Integer.parseInt(mSelectedPOIData.getId()),
                        mSelectedPOIData.getName(),
                        addr.toString(),
                        mSelectedPOIData.getLowerAddrName(),
                        "insert"
                );

                String response = call.execute().body();
                return response.equals("true");
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(aBoolean -> {

                        if (aBoolean) {
                            mExceptList.add(place);
//                                    mAdapter.notifyDataSetChanged();
                            mAdapter.notifyItemInserted(mExceptList.size()-1);
                            Toast.makeText(getContext(), "제외 음식점 등록 성공!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "제외 음식점 등록 실패!", Toast.LENGTH_SHORT).show();
                        }

                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                    })
                    .doOnError(throwable -> {
                        throwable.getStackTrace();
                        Toast.makeText(getContext(), "제외 음식점 등록 실패!", Toast.LENGTH_SHORT).show();
                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                    })
                    .subscribe();
            hasResInBoundary();


//                int tryNum = 0;
//                do {
//                    hasResInBoundary();
//                    ++tryNum;
//                } while (tryNum < Constants.POI_MAX_COUNT);
//                if(tryNum==Constants.POI_MAX_COUNT){
//                    Toast.makeText(getContext(),"해당 범위의 검색 가능한 음식점이 모두 제외되었습니다.\n제외 목록에서 해제해주세요",Toast.LENGTH_SHORT).show();
//                } else {
//                    showOnMap(true);
//                    detailDataDisposable();
//                }

        });
    }

    private void hasResInBoundary() {
        int totalCount = mPOIWrap.getTotalCount();
        int randParam = totalCount / Constants.POI_MAX_COUNT;
        int randomPage;
        if (randParam > 1) {
            if (randParam > Constants.POI_MAX_PAGE) {
                randomPage = getRandomInt(Constants.POI_MAX_PAGE) + 1;
            } else {
                randomPage = getRandomInt(randParam) + 1;
            }
            HttpInterface httpInterface = HttpClient.getTmapClient().create(HttpInterface.class);
            Call<POIData> poiDataCall = httpInterface.getPOIData(
                    Constants.TMAP_VERSION, randomPage, Constants.POI_MAX_COUNT,
                    mUserRequestData.getFoodsToString(), mUserRequestData.getLon(), mUserRequestData.getLat(),
                    mUserRequestData.getRadius(), getContext().getString(R.string.tmap)
            );
            try {
                mPOIWrap = poiDataCall.execute().body().getSearchPoiInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("카운트!", totalCount + "");

        int selectedCount = getRandomInt(mPOIWrap.getCount());
        for (int i = 0 ; i<mExceptList.size(); i++) {
            if (mExceptList.get(i).getId() == selectedCount) {
                selectedCount = getRandomInt(mPOIWrap.getCount());
                i = -1;
            }
        }
        Log.d("선택!", selectedCount + "");
        POINode[] poiNodes = mPOIWrap.getPois().getPoi();
        mSelectedPOIData = poiNodes[selectedCount];
    }

    private void showOnMap(Boolean hasData) {
        if (hasData) {
            Double lat = Double.parseDouble(mSelectedPOIData.getNoorLat());
            Double lon = Double.parseDouble(mSelectedPOIData.getNoorLon());
            StringBuilder roadAddr = new StringBuilder();
            roadAddr.append(mSelectedPOIData.getUpperAddrName());
            roadAddr.append(" ");
            roadAddr.append(mSelectedPOIData.getMiddleAddrName());
            roadAddr.append(" ");
            roadAddr.append(mSelectedPOIData.getRoadName());
            roadAddr.append(" ");
            roadAddr.append(mSelectedPOIData.getBuildingNo1());
            String tmp = mSelectedPOIData.getBuildingNo2();
            if (!(tmp == null || tmp.equals("0") || tmp.equals(""))) {
                roadAddr.append("-");
                roadAddr.append(mSelectedPOIData.getBuildingNo2());
            }
            roadAddr.append("\n");
            StringBuilder oldAddr = new StringBuilder();
            oldAddr.append(mSelectedPOIData.getUpperAddrName());
            oldAddr.append(" ");
            oldAddr.append(mSelectedPOIData.getMiddleAddrName());
            oldAddr.append(" ");
            oldAddr.append(mSelectedPOIData.getLowerAddrName());
            oldAddr.append(" ");
            String tmp2 = mSelectedPOIData.getDetailAddrName();
            if (!(tmp2 != null || (tmp2.equals("")))) {
                oldAddr.append(mSelectedPOIData.getDetailAddrName());
                oldAddr.append(" ");
            }
            oldAddr.append(mSelectedPOIData.getFirstNo());
            String tmp3 = mSelectedPOIData.getSecondNo();
            if (!tmp3.equals("")) {
                oldAddr.append("-");
                oldAddr.append(tmp3);
            }
            roadAddr.append(oldAddr);
            showResMarkOnMap(lat, lon, mSelectedPOIData.getName(), roadAddr.toString());

            if (!isSelected) {
                Animation animMoveToTop = AnimationUtils.loadAnimation(getContext(), R.anim.move);

                if (!mSelectedPOIData.getTelNo().equals("")) {
                    mFragmentSearchBinding.ibCall.setVisibility(View.VISIBLE);
                }
                mFragmentSearchBinding.btExcept.startAnimation(animMoveToTop);
                mFragmentSearchBinding.llOptions.setAnimation(animMoveToTop);

                mFragmentSearchBinding.btExcept.setVisibility(View.VISIBLE);
                mFragmentSearchBinding.llOptions.setVisibility(View.VISIBLE);

            } else {
                if (mFragmentSearchBinding.ibCall.getVisibility() == View.VISIBLE && mSelectedPOIData.getTelNo().equals("")) {
                    //사라지기
                    Animation phoneAnimation = new AlphaAnimation(1, 0);
                    phoneAnimation.setDuration(500);
                    mFragmentSearchBinding.ibCall.setAnimation(phoneAnimation);
                    mFragmentSearchBinding.ibCall.setVisibility(View.GONE);
                } else if (mFragmentSearchBinding.ibCall.getVisibility() == View.GONE) {
                    //나타나기
                    Animation phoneAnimation = new AlphaAnimation(0, 1);
                    phoneAnimation.setDuration(500);
                    mFragmentSearchBinding.ibCall.setVisibility(View.VISIBLE);
                    mFragmentSearchBinding.ibCall.setAnimation(phoneAnimation);
                }
            }
            isSelected = true;
        } else {
            Toast.makeText(getContext(), "반경 내에 음식점을 찾을 수 없습니다!", Toast.LENGTH_SHORT).show();
        }
    }

    private void detailDataDisposable() {
        Single.fromCallable(() -> {
            if (mSelectedPOIData != null) {
                HttpInterface httpInterface = HttpClient.getTmapClient().create(HttpInterface.class);
                Call<POIDetailData> getPOIDetailData = httpInterface.getPOIDetailData(
                        mSelectedPOIData.getId(), Constants.TMAP_VERSION, getString(R.string.tmap));
                mDetailData = getPOIDetailData.execute().body();
            }
            return mDetailData != null;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(aBoolean2 -> {
                    if (aBoolean2) {
                        mCustomInfoWindowAdapter = new CustomInfoWindowAdapter(
                                getContext(), mDetailData.getPoiDetailInfo()
                        );
                        mInfoWindow.setAdapter(mCustomInfoWindowAdapter);
                    } else {
                        mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                            @NonNull
                            @Override
                            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                return "상세 정보를 불러올 수 없습니다";
                            }
                        });
                    }
                })
                .subscribe();
    }


    private void showResMarkOnMap(Double lat, Double lon, String name, String addr) {
        mResMarker.setPosition(new LatLng(lat, lon));
        mResMarker.setCaptionText(name);
        mResMarker.setSubCaptionText(addr);

        mResMarker.setMap(mNaverMap);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(
                new LatLng(lat, lon))
                .animate(CameraAnimation.Easing);
        mNaverMap.moveCamera(cameraUpdate);
    }

    private void showCurrentMarkOnMap() {
        double lat = mUserRequestData.getLat();
        double lon = mUserRequestData.getLon();
        mCurrentMarker.setPosition(new LatLng(lat, lon));
        mCurrentMarker.setSubCaptionText(mUserRequestData.getAddress());

        mCurrentMarker.setMap(mNaverMap);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(
                new LatLng(lat, lon))
                .animate(CameraAnimation.Easing);
        mNaverMap.moveCamera(cameraUpdate);
    }

    private void showRadiusOnMap() {
        circleOverlay.setMap(null);
        circleOverlay.setCenter(new LatLng(mUserRequestData.getLat(), mUserRequestData.getLon()));
        circleOverlay.setRadius(mUserRequestData.getRadius() * 1000);
        circleOverlay.setMap(mNaverMap);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;
        mNaverMap.setOnMapClickListener((coord, point) -> mInfoWindow.close());
    }

    private int getRandomInt(int count) {
        if (count < 1) {
            return count;
        }
        Random random = new Random(System.currentTimeMillis());
        return random.nextInt(count);
    }
}
