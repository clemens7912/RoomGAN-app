package com.example.roomgan;

import android.graphics.Bitmap;

public class MeasureItem {

    private int id;
    private Bitmap sourceImage;
    private Bitmap targetImage;
    private Bitmap generatedImage;

    public MeasureItem(){

    }

    public MeasureItem(int id, Bitmap sourceImage, Bitmap targetImage, Bitmap generatedImage){
        this.id = id;
        this.sourceImage = sourceImage;
        this.targetImage = targetImage;
        this.generatedImage = generatedImage;
    }

    public int getId(){
        return  id;
    }

    public Bitmap getSourceImage() {
        return sourceImage;
    }

    public void setSourceImage(Bitmap sourceImage) {
        this.sourceImage = sourceImage;
    }

    public Bitmap getTargetImage() {
        return targetImage;
    }

    public void setTargetImage(Bitmap targetImage) {
        this.targetImage = targetImage;
    }

    public Bitmap getGeneratedImage() {
        return generatedImage;
    }

    public void setGeneratedImage(Bitmap generatedImage) {
        this.generatedImage = generatedImage;
    }
}
