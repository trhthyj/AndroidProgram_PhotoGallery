package com.mi.www.photogallary;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGallaryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGallaryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return PhotoGallaryFragment.getInstance();
    }
}
