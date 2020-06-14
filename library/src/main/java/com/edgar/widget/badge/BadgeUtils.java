package com.edgar.widget.badge;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewOverlay;

import androidx.annotation.NonNull;
import androidx.annotation.XmlRes;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Edgar on 2020/6/13.
 */
public class BadgeUtils {

    public static void attachBadgeDrawable(
            @NonNull final BadgeDrawable badgeDrawable,
            @NonNull final View anchor) {
        if (badgeDrawable.getCallback() == null) {
            anchor.getOverlay().add(badgeDrawable);
            anchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    badgeDrawable.updateBadgeCoordinates(anchor);
                }
            });
        }
    }

    public static void detachBadgeDrawable(@NonNull final BadgeDrawable badgeDrawable, @NonNull final View anchor) {
        ViewOverlay overlay = anchor.getOverlay();
        overlay.remove(badgeDrawable);
    }

    @NonNull
    static AttributeSet parseDrawableXml(
            @NonNull Context context, @XmlRes int id, @NonNull CharSequence startTag) {
        try {
            XmlPullParser parser = context.getResources().getXml(id);

            int type;
            do {
                type = parser.next();
            } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            if (!TextUtils.equals(parser.getName(), startTag)) {
                throw new XmlPullParserException("Must have a <" + startTag + "> start tag");
            }

            AttributeSet attrs = Xml.asAttributeSet(parser);

            return attrs;
        } catch (XmlPullParserException | IOException e) {
            Resources.NotFoundException exception =
                    new Resources.NotFoundException("Can't load badge resource ID #0x" + Integer.toHexString(id));
            exception.initCause(e);
            throw exception;
        }
    }
}