package com.nlbn.ads.rate;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRatingBar;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;
import com.nlbn.ads.R;


public class RateAppDiaLog extends Dialog {
    private TextView tvTitle, tvContent, btnRate, btnNotnow;

    private RateBuilder builder;
    private Context context;
    private ImageView imgRate;
    private AppCompatRatingBar rtb;
    private LinearLayout dialog;
    private RelativeLayout bg_star;

    private boolean isZoomedIn = false;

    

    public RateAppDiaLog(Activity context, RateBuilder builder) {
        super(context);
        this.context = context;
        this.builder = builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_rate);
        getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        initView();
    }

    private void initView() {
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        imgRate = findViewById(R.id.imgRate);
        rtb = findViewById(R.id.rtb);
        btnRate = findViewById(R.id.btnRate);
        btnNotnow = findViewById(R.id.btnNotnow);
        dialog = findViewById(R.id.dialog);
        bg_star = findViewById(R.id.bg_star);
        if (builder.getTitle() != null)
            tvTitle.setText(builder.getTitle());
        if (builder.getContext() != null)
            tvContent.setText(builder.getContent());
        if (builder.getTitleColor() != 0)
            tvTitle.setTextColor(builder.getTitleColor());
        if (builder.getContentColor() != 0)
            tvContent.setTextColor(builder.getContentColor());
        if (builder.getRateUsColor() != 0) {
            btnRate.setTextColor(builder.getRateUsColor());
        }
        if (builder.getNotNowColor() != 0) {
            btnNotnow.setTextColor(builder.getNotNowColor());
        }

        if (builder.getColorStart() != null && builder.getColorEnd() != null) {
            TextPaint paint = tvTitle.getPaint();
            float width = paint.measureText(tvTitle.getText().toString());
            Shader textShader = new LinearGradient(0, 0, width, tvTitle.getTextSize(), new int[]{Color.parseColor(builder.getColorStart()),
                    Color.parseColor(builder.getColorEnd()),}, null, Shader.TileMode.CLAMP);
            tvTitle.getPaint().setShader(textShader);
        }


        if (builder.getTitleSize() != 0) {
            tvTitle.setTextSize(builder.getTitleSize());
        }
        if (builder.getRateNowSize() != 0) {
            btnRate.setTextSize(builder.getRateNowSize());
        }
        if (builder.getNotNowSize() != 0) {
            btnNotnow.setTextSize(builder.getNotNowSize());
        }
        if (builder.getNotNow() != null && builder.getRateUs() != null) {
            btnRate.setText(builder.getRateUs());
            btnNotnow.setText(builder.getNotNow());
        }
        if (builder.getDrawableRateUs() != 0) {
            btnRate.setBackgroundResource(builder.getDrawableRateUs());
        }
        if (builder.getContentSize() != 0) {
            tvContent.setTextSize(builder.getContentSize());
        }
        if (builder.getTypeface() != null) {
            tvTitle.setTypeface(builder.getTypeface());
            tvContent.setTypeface(builder.getTypeface());
            btnRate.setTypeface(builder.getTypeface());
            btnNotnow.setTypeface(builder.getTypeface());
        }
        if (builder.getTypefaceTitle() != null) {
            tvTitle.setTypeface(builder.getTypefaceTitle());
        }
        if (builder.getTypefaceContent() != null) {
            tvContent.setTypeface(builder.getTypefaceContent());
        }
        if (builder.getTypefaceRateUs() != null) {
            btnRate.setTypeface(builder.getTypefaceRateUs() );
        }
        if (builder.getTypefaceNotNow() != null) {
            btnNotnow.setTypeface(builder.getTypefaceNotNow());
        }

        if (builder.getDrawableDialog() != 0) {
            dialog.setBackgroundResource(builder.getDrawableDialog());
        }
        if (builder.getDrawableBgStar() != 0) {
            bg_star.setBackgroundResource(builder.getDrawableBgStar());
        }

        btnNotnow.setOnClickListener(v -> {
            builder.getOnClickBtn().onclickNotNow();
            dismiss();
        });
        btnRate.setOnClickListener(v -> {
            builder.getOnClickBtn().onClickRate(rtb.getRating());
            if (rtb.getRating() >= builder.getNumberRateInApp()) {
                if (builder.isRateInApp()) {
                    reviewApp(context);
                } else {
                    dismiss();
                }
            } else {
                if (rtb.getRating() > 0) {
                    dismiss();
                }
            }

        });

        changeRating();

        if (builder.getColorRatingBar() != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rtb.setProgressTintList(ColorStateList.valueOf(Color.parseColor(builder.getColorRatingBar())));
            }
        if (builder.getColorRatingBarBg() != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rtb.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor(builder.getColorRatingBarBg())));
            }

        if (builder.getNumberRateDefault() > 0 && builder.getNumberRateDefault() < 6) {
            rtb.setRating(builder.getNumberRateDefault());
        }
    }

    public void reviewApp(Context context) {
        ReviewManager manager = ReviewManagerFactory.create(context);
        com.google.android.gms.tasks.Task<com.google.android.play.core.review.ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ReviewInfo reviewInfo = task.getResult();
                        Task<Void> flow = manager.launchReviewFlow(((Activity) context), reviewInfo);
                        flow.addOnCompleteListener(task2 -> {
                            builder.getOnClickBtn().onReviewAppSuccess();
                            dismiss();
                        });
                    } else {
                        Log.e("ReviewError", "" + task.getException().toString());
                    }
                }
        );
    }

    public void changeRating() {
        rtb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String getRating = String.valueOf(rtb.getRating());
                animationStar();
                switch (getRating) {
                    case "1.0":
                        imgRate.setImageResource(builder.getArrStar()[1]);
                        break;
                    case "2.0":
                        imgRate.setImageResource(builder.getArrStar()[2]);
                        break;
                    case "3.0":
                        imgRate.setImageResource(builder.getArrStar()[3]);
                        break;
                    case "4.0":
                        imgRate.setImageResource(builder.getArrStar()[4]);
                        break;
                    case "5.0":
                        imgRate.setImageResource(builder.getArrStar()[5]);
                        break;
                    default:
                        rtb.setRating(1f);
                        imgRate.setImageResource(builder.getArrStar()[0]);
                        break;
                }
            }
        });
    }

    private void animationStar() {
        if (!isZoomedIn) {
            animateZoomIn();
        }
    }

    private void animateRotation(){
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(imgRate, "rotation", 0f, 360f);
        rotationAnimator.setDuration(500); // Animation duration in milliseconds
        rotationAnimator.setInterpolator(new LinearInterpolator()); // Use a linear interpolator for constant speed
        rotationAnimator.start();
        animateZoomOut();
    }

    private void animateZoomIn() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgRate, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgRate, "scaleY", 1f, 1.2f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.start();
        scaleY.start();

        isZoomedIn = true;
        animateRotation();

    }

    private void animateZoomOut() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgRate, "scaleX", 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgRate, "scaleY", 1.2f, 1f);
        scaleX.setDuration(800);
        scaleY.setDuration(800);
        scaleX.start();
        scaleY.start();

        isZoomedIn = false;
    }
    
}
