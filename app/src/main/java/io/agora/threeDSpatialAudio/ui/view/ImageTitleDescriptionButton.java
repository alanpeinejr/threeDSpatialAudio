package io.agora.threeDSpatialAudio.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import io.agora.threeDSpatialAudio.R;

public class ImageTitleDescriptionButton extends LinearLayout {

    public ImageTitleDescriptionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_image_title_description, this);
        TypedArray customAttributesStyle = context.obtainStyledAttributes(attrs, R.styleable.ImageTitleDescriptionButton, 0, 0);

        TextView title = this.findViewById(R.id.view_image_title_button_description_title);
        TextView description = this.findViewById(R.id.view_image_title_button_description_description);
        ImageView image = this.findViewById(R.id.view_image_title_button_description_title_image_view);
        GradientDrawable backgroundShape = ((GradientDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.layout_rounded_corner_bg, context.getTheme()).mutate());

        try {
            title.setText(customAttributesStyle.getString(R.styleable.ImageTitleDescriptionButton_title));
            description.setText(customAttributesStyle.getString(R.styleable.ImageTitleDescriptionButton_description));
            image.setImageDrawable(customAttributesStyle.getDrawable(R.styleable.ImageTitleDescriptionButton_image));
            if (backgroundShape != null) {
                int backgroundColor = customAttributesStyle.getColor(R.styleable.ImageTitleDescriptionButton_color, Color.TRANSPARENT);
                backgroundShape.setColor(backgroundColor);
            }
            this.setBackground(backgroundShape);
            this.setClipToOutline(true);

        } finally {
            customAttributesStyle.recycle();
        }
    }
}