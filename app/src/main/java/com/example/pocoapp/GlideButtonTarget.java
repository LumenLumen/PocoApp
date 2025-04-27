package com.example.pocoapp;

import android.graphics.drawable.Drawable;

import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class GlideButtonTarget extends CustomTarget<Drawable> {
    private final Button button;

    public GlideButtonTarget(Button button) {
        this.button = button;
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        button.setBackground(resource);
        button.setPadding(5, 5, 5, 5); // Important sinon l’image écrase tout
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        button.setBackground(placeholder);
    }
}
