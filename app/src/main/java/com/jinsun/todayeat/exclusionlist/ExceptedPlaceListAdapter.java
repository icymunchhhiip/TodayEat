package com.jinsun.todayeat.exclusionlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.jinsun.todayeat.MainActivity;
import com.jinsun.todayeat.R;
import com.jinsun.todayeat.model.ExceptedPlaceModel;
import com.jinsun.todayeat.network.HttpClient;
import com.jinsun.todayeat.network.HttpInterface;
import com.jinsun.todayeat.search.SearchFragment;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;

public class ExceptedPlaceListAdapter extends RecyclerView.Adapter<ExceptedPlaceListAdapter.MyViewHolder> {
    private final Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            description = view.findViewById(R.id.description);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }


    public ExceptedPlaceListAdapter(Context context) {
        this.context = context;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_places, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ExceptedPlaceModel item = SearchFragment.mExceptList.get(position);
        holder.name.setText(item.getName());
        holder.description.setText(item.getAddr());
    }

    @Override
    public int getItemCount() {
        return SearchFragment.mExceptList.size();
    }

    public void removeItem(int position) {

        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        Single.fromCallable(() -> {

            HttpInterface httpInterface = HttpClient.getServerClient().create(HttpInterface.class);
            Call<String> call = httpInterface.setPlaces(
                    (int) MainActivity.sMyId,
                    SearchFragment.mExceptList.get(position).getId(),
                    "",
                    "",
                    "",
                    "delete"
            );

            String response = call.execute().body();
            return response.equals("true");
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(aBoolean -> {

                    if (aBoolean) {
                        SearchFragment.mExceptList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "제외 음식점 해제 성공!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "제외 음식점 해제 실패!", Toast.LENGTH_SHORT).show();
                    }

//                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                })
                .doOnError(throwable -> {
                    throwable.getStackTrace();
                    Toast.makeText(context, "제외 음식점 해제 실패!", Toast.LENGTH_SHORT).show();
//                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                })
                .subscribe();
    }

    public void restoreItem(ExceptedPlaceModel item, int position) {
        SearchFragment.mExceptList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }
}
