package com.chrischivers.londonbustracker;

/* Copyright 2013 Google Inc.
        Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */


        import android.animation.ObjectAnimator;
        import android.animation.TypeEvaluator;
        import android.animation.ValueAnimator;
        import android.annotation.TargetApi;
        import android.os.Build;
        import android.os.Handler;
        import android.os.SystemClock;
        import android.util.Property;
        import android.view.animation.AccelerateDecelerateInterpolator;
        import android.view.animation.Interpolator;

        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;

public class MarkerAnimation {
    static void animateMarkerToGB(final MarkerPair markerPair, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator, final long duration) {
        final LatLng startPosition = markerPair.imageMarker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = duration;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng interpolateResult = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                markerPair.imageMarker.setPosition(interpolateResult);
                markerPair.textMarker.setPosition(interpolateResult);


                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void animateMarkerToHC(final MarkerPair markerPair, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator, final long duration) {
        final LatLng startPosition = markerPair.imageMarker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                markerPair.imageMarker.setPosition(newPosition);
                markerPair.textMarker.setPosition(newPosition);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static void animateMarkerToICS(MarkerPair markerPair, LatLng finalPosition, final LatLngInterpolator latLngInterpolator, final long duration) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator imageMarkerAnimator = ObjectAnimator.ofObject(markerPair.imageMarker, property, typeEvaluator, finalPosition);
        ObjectAnimator textMarkerAnimator = ObjectAnimator.ofObject(markerPair.textMarker, property, typeEvaluator, finalPosition);
        imageMarkerAnimator.setDuration(duration);
        textMarkerAnimator.setDuration(duration);
        imageMarkerAnimator.start();
        textMarkerAnimator.start();
    }
}