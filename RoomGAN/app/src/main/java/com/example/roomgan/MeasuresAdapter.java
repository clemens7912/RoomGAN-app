package com.example.roomgan;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeasuresAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private ArrayList<MeasureItem> mMeasuresItems;
    private static ClickListener clickListener;

    public MeasuresAdapter(ArrayList<MeasureItem> measuresItems){
        this.mMeasuresItems = measuresItems;
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType){
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_measure, parent, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == mMeasuresItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mMeasuresItems == null ? 0 : mMeasuresItems.size();
    }

    public void addItems(ArrayList<MeasureItem> measureItems){
        for(MeasureItem measureItem : measureItems){
            mMeasuresItems.add(measureItem);
        }
        notifyDataSetChanged();
    }

    public void addItem(MeasureItem item, int position){
        mMeasuresItems.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position){
        mMeasuresItems.remove(position);
        notifyItemRemoved(position);
    }

    public void addLoading(){
        isLoaderVisible = true;
        mMeasuresItems.add(new MeasureItem());
        notifyItemInserted(mMeasuresItems.size()-1);
    }

    public void removeLoading(){
        isLoaderVisible = false;
        int position = mMeasuresItems.size() - 1;
        MeasureItem item = getItem(position);
        if (item != null) {
            mMeasuresItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear(){
        mMeasuresItems.clear();
        notifyDataSetChanged();
    }

    public MeasureItem getItem(int position){
        return mMeasuresItems.get(position);
    }

    public void setOnItemClickListener(ClickListener clickListener){
        MeasuresAdapter.clickListener = clickListener;
    }

    public interface ClickListener{
        void onItemClick(int position, View v);
    }

    public class ViewHolder extends BaseViewHolder{
        @BindView(R.id.sourceImage)
        ImageView sourceImage;
        @BindView(R.id.targetImage)
        ImageView targetImage;
        @BindView(R.id.generatedImage)
        ImageView generatedImage;

        public ViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MeasuresAdapter.clickListener.onItemClick(getAdapterPosition(), itemView);
                }
            });
        }

        protected void clear(){

        }

        public void onBind(int position){
            super.onBind(position);
            MeasureItem item = mMeasuresItems.get(position);

            sourceImage.setImageBitmap(item.getSourceImage());
            targetImage.setImageBitmap(item.getTargetImage());
            generatedImage.setImageBitmap(item.getGeneratedImage());


        }
    }

    public class ProgressHolder extends BaseViewHolder{
        public ProgressHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void clear(){

        }
    }
}
