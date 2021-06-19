package com.example.roomgan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.roomgan.PaginationListener.PAGE_START;

public class HomeFragment extends Fragment implements Listener, SwipeRefreshLayout.OnRefreshListener {

    private final int GET_FROM_GALLERY = 5;
    private final int GET_FROM_CAMERA = 6;

    private static final String TAG = "HomeFragment";

    private String username;

    private File mImageFolder;
    private String mImageFileName;

    private Activity mActivity;
    private Context mContext;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MeasuresAdapter adapter;

    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;
    private int offset = 0;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/

        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();

        username = ((GlobalState) mActivity.getApplication()).getUsername();

        createImageFolder();
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = view.findViewById(R.id.measures);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new MeasuresAdapter(new ArrayList<>());
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new MeasuresAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                onCardViewClick(position, v);
            }
        });
        //doApiCall();

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                doApiCall();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();
                MeasureItem removedItem = adapter.getItem(position);
                adapter.removeItem(position);

                Snackbar snackbar = Snackbar.make(mRecyclerView,mActivity.getResources().getString(R.string.measureDeleted), Snackbar.LENGTH_LONG)
                        .setAction(mActivity.getResources().getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                adapter.addItem(removedItem, position);
                            }
                        });

                snackbar.addCallback(new Snackbar.Callback(){
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT){
                            HashMap<String, String> params = new HashMap<>();
                            params.put("id",String.valueOf(removedItem.getId()));
                            new ServerConnection().get(HomeFragment.this,"measure/delete",
                                    params, ((GlobalState)mActivity.getApplication()).getToken());
                        }
                    }
                });

                 snackbar.show();

            }

            @Override
            public void onChildDraw(@NonNull @NotNull Canvas c, @NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(mActivity, c,recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.RED)
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        return view;
    }


    private void doApiCall(){
        HashMap<String,String> params = new HashMap<>();
        params.put("offset", String.valueOf(offset));

        new ServerConnection().get(HomeFragment.this, "measures", params,
                ((GlobalState)mActivity.getApplication()).getToken());
    }

    private void getMeasures(){
        new ServerConnection().get(HomeFragment.this, "/measures",null,
                ((GlobalState)mActivity.getApplication()).getToken());
    }

    private Bitmap getBitmap(int drawableRes){
        Drawable drawable = mActivity.getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0,0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    private void storeImage(Bitmap bitmap) {
        new Thread(new Runnable() {
            private Activity activity;

            public Runnable init(Activity activity){
                this.activity = activity;
                return this;
            }

            @Override
            public void run() {
                //Bitmap bitmap = loadBitmapFromView();

                FileOutputStream outputStream = null;

                if(!mImageFolder.exists()){
                    mImageFolder.mkdirs();
                }
                createFileName();
                try {
                    outputStream = new FileOutputStream(mImageFileName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Image Stored", Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.i("prueba", "Image stored");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.init(mActivity)).start();
    }

    /*
    private Bitmap loadBitmapFromView(){
        Bitmap b = Bitmap.createBitmap(imageView.getLayoutParams().width, imageView.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        imageView.layout(0,0, imageView.getLayoutParams().width,imageView.getLayoutParams().height);
        imageView.draw(c);
        return b;
    }

     */



    private void createImageFolder(){
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "RoomGan");
        if(!mImageFolder.exists()){
            mImageFolder.mkdirs();
        }
    }

    private File createFileName(){
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp;
        File imageFile = new File(mImageFolder, prepend+".jpeg");
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }


    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("method")){
                case "getMeasures":
                    switch (data.getInt("status")){
                        case 0:
                            final ArrayList<MeasureItem> items = new ArrayList<>();
                            JSONArray measures = data.getJSONArray("data");
                            for(int i=0; i < measures.length(); i++){
                                itemCount++;
                                JSONObject measure = measures.getJSONObject(i);
                                int id = measure.getInt("id");
                                Bitmap sourceImage = getBitmapEncodedImage(measure.getString("sourceImage"));
                                Bitmap targetImage = getBitmapEncodedImage(measure.getString("targetImage"));
                                Bitmap generatedImage = getBitmapEncodedImage(measure.getString("generatedImage"));
                                items.add(new MeasureItem(id, sourceImage, targetImage, generatedImage));
                            }

                            offset += measures.length();
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(currentPage != PAGE_START){
                                        adapter.removeLoading();
                                    }

                                    if(items.size() > 0){
                                        adapter.addItems(items);
                                    }
                                    swipeRefresh.setRefreshing(false);

                                    if(currentPage < totalPage && measures.length()==5){
                                        adapter.addLoading();
                                    } else{
                                        isLastPage = true;
                                    }


                                    isLoading = false;
                                }
                            });
                            break;
                        default:
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "Internal server error", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                    break;
                case "deleteMeasure":
                    switch (data.getInt("status")){
                        case 0:
                            offset--;
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "Item deleted successfully", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        default:
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "Server error. Item not deleted", Toast.LENGTH_LONG).show();
                                    swipeRefresh.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefresh.setRefreshing(true);
                                        }
                                    });
                                }
                            });
                    }
                    break;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private String encodeImage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public void onCardViewClick(int position, View v){
        View view = LayoutInflater.from(mActivity).inflate(R.layout.measure_item_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.DialogTheme);
        alertDialogBuilder.setView(view);

        ImageView sourceImage = view.findViewById(R.id.sourceImage);
        ImageView targetImage = view.findViewById(R.id.targetImage);
        ImageView generatedImage = view.findViewById(R.id.generatedImage);
        ImageButton saveImageButton = view.findViewById(R.id.saveImageButton);

        MeasureItem item = adapter.getItem(position);

        sourceImage.setImageBitmap(item.getSourceImage());
        targetImage.setImageBitmap(item.getTargetImage());
        generatedImage.setImageBitmap(item.getGeneratedImage());

        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImage(item.getGeneratedImage());
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private Bitmap getBitmapEncodedImage(String image){
        byte[] imageBytes = Base64.decode(image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return bitmap;
    }

    @Override
    public void onRefresh() {
        itemCount = 0;
        offset = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        doApiCall();
    }
}