package com.example.administrator.placestovisit;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import android.support.v7.widget.SearchView;

import adapters.PlaceAdapter;
import helpers.RetrofitClient;
import models.Places;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.NightclubsService;
import services.PlaceService;
import services.RestorantsService;

public class NightclubsTab extends Fragment {

    List<Places> nightclubs_list;
    ListView listViewNightBars;
    PlaceAdapter nightclubAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Context context;
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_nightclubs, container, false);
        context = getActivity().getApplicationContext();

        mSwipeRefreshLayout = rootView.findViewById(R.id.nightclubsTab);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);

        listViewNightBars = rootView.findViewById(R.id.listViewNightclubs);

        searchView = rootView.findViewById(R.id.search);
        searchView.setQueryHint("Search nightclubs");

        registerForContextMenu(listViewNightBars);


        populateNightclubs();

        listViewNightBars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Places place = (Places) listViewNightBars.getAdapter().getItem(i);

                Intent intent = new Intent(context, PlaceDetailsActivity.class);
                intent.putExtra("PlaceID",place.getId());
                getActivity().startActivity(intent);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //handling swipe refresh
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        listViewNightBars.setAdapter(null);
                        populateNightclubs();
                        nightclubAdapter.notifyDataSetChanged();
                        listViewNightBars.smoothScrollToPosition(0);
                    }
                }, 2000);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                NightclubsService searchAPI = RetrofitClient.getClient().create(NightclubsService.class);
                Call<List<Places>> getSearchResult = searchAPI.search_nightclubs(s);

                getSearchResult.enqueue(new Callback<List<Places>>() {
                    @Override
                    public void onResponse(Call<List<Places>> call, Response<List<Places>> response) {
                        nightclubs_list = response.body();
                        listViewNightBars.setAdapter(new PlaceAdapter(getActivity().getApplicationContext(), nightclubs_list));
                    }

                    @Override
                    public void onFailure(Call<List<Places>> call, Throwable t) {
                        Log.d("RESTORANTS API FAILES", t.getMessage());
                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return rootView;
    }

    public void populateNightclubs() {
        NightclubsService nightclubsServiceAPI = RetrofitClient.getClient().create(NightclubsService.class);
        Call<List<Places>> getNightclubs = nightclubsServiceAPI.get_nightclubs();

        getNightclubs.enqueue(new Callback<List<Places>>() {
            @Override
            public void onResponse(Call<List<Places>> call, Response<List<Places>> response) {
                nightclubs_list = response.body();
                nightclubAdapter = new PlaceAdapter(context, nightclubs_list);
                listViewNightBars.setAdapter(nightclubAdapter);
            }

            @Override
            public void onFailure(Call<List<Places>> call, Throwable t) {
                Log.d("NIGHTCLUBS API FAILES", t.getMessage());
            }
        });
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        menu.add(2, v.getId(), 0, "Edit");
        menu.add(2, v.getId(), 0, "Delete");
    }

    public boolean onContextItemSelected(MenuItem item){
        if( getUserVisibleHint() == false )
        {
            return false;
        }

        PlaceService placeAPI = RetrofitClient.getClient().create(PlaceService.class);

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int index = info.position;
            Places nightclub_clicked = (Places) listViewNightBars.getAdapter().getItem(index);


            if (item.getTitle() == "Edit") {
                Intent myIntent =  new Intent(getActivity().getApplicationContext(), EditPlaceActivity.class);
                myIntent.putExtra("Edit_ID", nightclub_clicked.getId());
                myIntent.putExtra("Edit_place_type", "nightclub");
                startActivity(myIntent);
            }

            if (item.getTitle() == "Delete") {
                final String placeName = nightclub_clicked.getName();
                Call<ResponseBody> deletePlace = placeAPI.delete_place(nightclub_clicked.getId());
                deletePlace.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Toast.makeText(getContext(), placeName + " have been deleted", Toast.LENGTH_LONG).show();
                        populateNightclubs();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                    }
                });
            }

        return true;
    }
}
