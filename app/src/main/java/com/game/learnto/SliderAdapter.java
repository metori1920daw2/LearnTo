package com.game.learnto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {
    private LayoutInflater inflater;
    int[] loyouts;
    Context context;

    public SliderAdapter(int[] loyouts, Context context) {
        this.loyouts = loyouts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return loyouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(loyouts[position], container,false );

        container.addView(v);
        return  v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View v = (View) object;
        container.removeView(v);
    }

}
