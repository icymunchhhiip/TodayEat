package com.jinsun.todayeat.exclusionlist;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.jinsun.todayeat.MainActivity;
import com.jinsun.todayeat.R;
import com.jinsun.todayeat.databinding.FragmentExclusionListBinding;
import com.jinsun.todayeat.model.ExceptedPlaceModel;
import com.jinsun.todayeat.network.HttpClient;
import com.jinsun.todayeat.network.HttpInterface;
import com.jinsun.todayeat.search.SearchFragment;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;

public class ExclusionListFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    public static ExceptedPlaceListAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentExclusionListBinding fragmentExclusionListBinding = FragmentExclusionListBinding.inflate(inflater, container, false);

        fragmentExclusionListBinding.tbExclusion.inflateMenu(R.menu.exclusion_menu);
        fragmentExclusionListBinding.tbExclusion.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.a_beer) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_a_beer)));
                startActivity(intent);
            } else if (id == R.id.logout) {
                UserManagement.getInstance()
                        .requestLogout(new LogoutResponseCallback() {
                            @Override
                            public void onCompleteLogout() {
                                MainActivity.sMyId = -1;
                                Toast.makeText(getContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (id == R.id.unlink) {
                Single.fromCallable(() -> {
                    HttpInterface httpInterface = HttpClient.getServerClient().create(HttpInterface.class);
                    Call<String> call = httpInterface.setUser((int) MainActivity.sMyId, "delete");
                    String response = call.execute().body();
                    return response.equals("true");
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess(aBoolean -> {
                            if (aBoolean) {
                                UserManagement.getInstance()
                                        .requestUnlink(new UnLinkResponseCallback() {
                                            @Override
                                            public void onSessionClosed(ErrorResult errorResult) {
                                                MainActivity.sMyId = -1;
                                                Toast.makeText(getContext(), R.string.session_closed, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onSuccess(Long result) {
                                                MainActivity.sMyId = -1;
                                                Toast.makeText(getContext(), R.string.unlink_success, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getContext(), R.string.unlink_fail, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .subscribe();
            }
            return false;
        });

        RecyclerView recyclerView = fragmentExclusionListBinding.rvExcepted;
        coordinatorLayout = fragmentExclusionListBinding.coordinatorLayout;
        mAdapter = new ExceptedPlaceListAdapter(getContext());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        prepareCart();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback1 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP) {
            @Override
            public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter
            }

            @Override
            public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback1).attachToRecyclerView(recyclerView);

        return fragmentExclusionListBinding.getRoot();
    }

    private void prepareCart() {

        Single.fromCallable(() -> {

            HttpInterface httpInterface = HttpClient.getServerClient().create(HttpInterface.class);
            Call<List<ExceptedPlaceModel>> call = httpInterface.getPlaces(
                    (int) MainActivity.sMyId
            );

            List<ExceptedPlaceModel> response = call.execute().body();
            if (response == null) {
                Toast.makeText(getContext(), "Couldn't fetch the menu! Pleas try again.", Toast.LENGTH_LONG).show();
                return false;
            } else {
                // adding items to cart list
                SearchFragment.mExceptList.clear();
                SearchFragment.mExceptList.addAll(response);

            }


            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(aBoolean -> {
                    // refreshing recycler view

                    mAdapter.notifyDataSetChanged();

//                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                })
                .doOnError(throwable -> {
                    throwable.getStackTrace();
                    Toast.makeText(getContext(), "정보를 불러올 수 없습니다!", Toast.LENGTH_SHORT).show();
//                        mFragmentSearchBinding.progressbar.setVisibility(View.GONE);
                })
                .subscribe();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ExceptedPlaceListAdapter.MyViewHolder) {
            // get the removed item name to display it in snack bar
            String name = SearchFragment.mExceptList.get(viewHolder.getAdapterPosition()).getName();

            // backup of removed item for undo purpose
            final ExceptedPlaceModel deletedItem = SearchFragment.mExceptList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            mAdapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", view -> {

                // undo is selected, restore the deleted item
                mAdapter.restoreItem(deletedItem, deletedIndex);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}