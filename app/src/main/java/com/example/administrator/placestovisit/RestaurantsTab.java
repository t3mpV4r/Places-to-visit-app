package com.example.administrator.placestovisit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import java.util.List;

import adapters.PlaceAdapter;
import helpers.RetrofitClient;
import models.Places;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.PlaceService;
import services.RestorantsService;

public class RestaurantsTab extends Fragment {

    List<Places> restorants_list;
    ListView listViewRestaurants;
    PlaceAdapter restaurantAdapter;
    Context context;
    SwipeRefreshLayout mSwipeRefreshLayout;
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_restorants, container, false);

        context = getActivity().getApplicationContext();

        mSwipeRefreshLayout = rootView.findViewById(R.id.restaurantsTab);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        listViewRestaurants = rootView.findViewById(R.id.listViewRestorants);
        searchView = rootView.findViewById(R.id.search);
        searchView.setQueryHint("Search restaurants");

        registerForContextMenu(listViewRestaurants);


        populateRestaurants();

        listViewRestaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Places place_restaurant = (Places) listViewRestaurants.getAdapter().getItem(position);
                 Intent intent = new Intent(context, PlaceDetailsActivity.class);
                 intent.putExtra("PlaceID",place_restaurant.getId());
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
                        listViewRestaurants.setAdapter(null);
                        populateRestaurants();
                        restaurantAdapter.notifyDataSetChanged();
                        listViewRestaurants.smoothScrollToPosition(0);
                    }
                }, 2000);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                RestorantsService searchAPI = RetrofitClient.getClient().create(RestorantsService.class);
                Call<List<Places>> getSearchResult = searchAPI.search_restaurants(s);

                getSearchResult.enqueue(new Callback<List<Places>>() {
                    @Override
                    public void onResponse(Call<List<Places>> call, Response<List<Places>> response) {
                        restorants_list = response.body();
                        listViewRestaurants.setAdapter(new PlaceAdapter(getActivity().getApplicationContext(), restorants_list));
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

    public  void populateRestaurants() {
        RestorantsService restaurantsAPI = RetrofitClient.getClient().create(RestorantsService.class);
        Call<List<Places>> getRestaurants = restaurantsAPI.get_restaurants();

        getRestaurants.enqueue(new Callback<List<Places>>() {
            @Override
            public void onResponse(Call<List<Places>> call, Response<List<Places>> response) {
                restorants_list = response.body();
                restaurantAdapter = new PlaceAdapter(context, restorants_list);
                listViewRestaurants.setAdapter(restaurantAdapter);
            }

            @Override
            public void onFailure(Call<List<Places>> call, Throwable t) {
                Log.d("RESTORANTS API FAILES", t.getMessage());
            }
        });
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Delete");
    }

    public boolean onContextItemSelected(MenuItem item){

        if( getUserVisibleHint() == false )
        {
            return false;
        }

        PlaceService placeAPI = RetrofitClient.getClient().create(PlaceService.class);

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int index = info.position;
            Places restaurant_clicked = (Places) listViewRestaurants.getAdapter().getItem(index);


            if (item.getTitle() == "Edit") {
                Intent myIntent =  new Intent(getActivity().getApplicationContext(), EditPlaceActivity.class);
                myIntent.putExtra("Edit_ID", restaurant_clicked.getId());
                myIntent.putExtra("Edit_place_type", "restaurant");
                startActivity(myIntent);
            }

            if (item.getTitle() == "Delete") {
                final String placeName = restaurant_clicked.getName();
                Call<ResponseBody> deletePlace = placeAPI.delete_place(restaurant_clicked.getId());
                deletePlace.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Toast.makeText(getContext(), placeName + " have been deleted", Toast.LENGTH_LONG).show();
                        populateRestaurants();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                    }
                });
            }
        return true;
    }


}
