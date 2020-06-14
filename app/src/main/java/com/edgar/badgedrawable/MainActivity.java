package com.edgar.badgedrawable;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.edgar.widget.badge.BadgeDrawable;
import com.edgar.widget.badge.BadgeUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.icon_view);
        BadgeDrawable drawable= BadgeDrawable.create(this,R.style.BadgeTextLong);
        BadgeUtils.attachBadgeDrawable(drawable, imageView);

        ImageView dotBadgeView = findViewById(R.id.icon_view_dot_badge);
        BadgeDrawable dotBadgeDrawable = BadgeDrawable.create(this,R.style.BadgeTextDot);
        BadgeUtils.attachBadgeDrawable(dotBadgeDrawable,dotBadgeView);

        ImageView dotBadgeView1 = findViewById(R.id.icon_view_dot_badge1);
        BadgeDrawable dotBadgeDrawable1 = BadgeDrawable.create(this,R.style.BadgeDot);
        BadgeUtils.attachBadgeDrawable(dotBadgeDrawable1,dotBadgeView1);
    }
}