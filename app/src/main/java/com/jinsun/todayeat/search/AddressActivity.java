package com.jinsun.todayeat.search;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jinsun.todayeat.Constants;
import com.jinsun.todayeat.databinding.ActivityAddressBinding;

import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddressActivity extends AppCompatActivity {
    private ActivityAddressBinding mActivityAddressBinding;
    //    private ArrayList<String> arrayList;
    private final String[] mStrings = {"", "", "", "", ""};
    private ArrayAdapter adapter;
    private List<Address> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityAddressBinding = ActivityAddressBinding.inflate(getLayoutInflater());
        View view = mActivityAddressBinding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        String str = intent.getExtras().getString(Constants.EXTRA_ADDRESS);
        if (str!=null){
            mActivityAddressBinding.etAddress.setText(str);
            showList(str);
        }

//        arrayList = new ArrayList<>(10);
        //                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, mStrings);
        mActivityAddressBinding.lvAddresses.setAdapter(adapter);

        mActivityAddressBinding.tbAddr.setNavigationOnClickListener(view1 -> onBackPressed());

        mActivityAddressBinding.etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //결과 주소를 리스트로 보여준다;
                showList(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mActivityAddressBinding.etAddress.setOnKeyListener((v, keyCode, event) -> {
            String string = mActivityAddressBinding.etAddress.getText().toString();
            list = getAddress(string);
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                //엔터시 결과 주소가 있으면 주소를 넘긴다
                if (list != null && list.size() != 0) {
                    Address info = list.get(0);
                    if (info.hasLatitude() && info.hasLongitude()) {
                        Intent intent1 = new Intent(getApplicationContext(), SearchFragment.class);
                        intent1.putExtra(Constants.EXTRA_ADDRESS, info.getAddressLine(0));
                        intent1.putExtra(Constants.EXTRA_LAT, info.getLatitude());
                        intent1.putExtra(Constants.EXTRA_LON, info.getLongitude());
                        setResult(RESULT_OK, intent1);
                        finish();
                        return true;
                    }
                }
                Toast.makeText(getApplicationContext(), "해당하는 주소가 없습니다", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        mActivityAddressBinding.lvAddresses.setOnItemClickListener((adapterView, view12, i, l) -> {
            //item 클릭 시 선택된 주소를 SearchFragment로 넘김
            if (!mStrings[i].equals("")) {
                Address info = list.get(i);
                if (info.hasLatitude() && info.hasLongitude()) {
                    Intent intent12 = new Intent(getApplicationContext(), SearchFragment.class);
                    intent12.putExtra(Constants.EXTRA_ADDRESS, info.getAddressLine(0));
                    intent12.putExtra(Constants.EXTRA_LAT, info.getLatitude());
                    intent12.putExtra(Constants.EXTRA_LON, info.getLongitude());
                    setResult(RESULT_OK, intent12);
                    finish();
                }
            }
        });

    }

    private void showList(CharSequence charSequence) {
        mActivityAddressBinding.progressbar.setVisibility(View.VISIBLE);
        Observable.fromCallable(() -> {
            list = getAddress(charSequence.toString());
            for (int j = 0; j < 5; j++) {
                if (list != null && j < list.size()) {
//                                arrayList.set(i,list.get(i).getAddressLine(0));
                    Address addr = list.get(j);
                    if (addr.hasLongitude() && addr.hasLatitude()) {
                        mStrings[j] = addr.getAddressLine(0);
                    }
                } else {
                    mStrings[j] = "";
                }
            }
            return false;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    adapter.notifyDataSetChanged();
                    mActivityAddressBinding.progressbar.setVisibility(View.GONE);
                });
    }

    private List<Address> getAddress(String string) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = null;
        try {
            list = geocoder.getFromLocationName(
                    string, 5
            );
            Log.d("@@@ ", list.size() + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
