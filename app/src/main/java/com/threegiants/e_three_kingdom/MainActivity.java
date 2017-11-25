package com.threegiants.e_three_kingdom;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;
import com.threegiants.e_three_kingdom.cards.SliderAdapter;
import com.threegiants.e_three_kingdom.utils.DecodeBitmapTask;

public class MainActivity extends AppCompatActivity {

    public static final String
            STORAGE_READ_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE",
            STORAGE_WRITE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";


    private final int[] pics = {R.drawable.scj, R.drawable.sg_rw,
            R.drawable.yy_qw, R.drawable.sg_zz, R.drawable.sg_jm,
            R.drawable.sg_sj, R.drawable.sg_dm, R.drawable.sg_gj};
    private final int[]  backgrounds= {R.drawable.scj_mbl, R.drawable.sg_rw_mbl,
            R.drawable.yy_qw_mbl, R.drawable.sg_zz_mbl, R.drawable.sg_jm_mbl,
            R.drawable.sg_sj_mbl, R.drawable.sg_dm_mbl, R.drawable.sg_gj_mbl};
    private final int[] descriptions = {R.string.text1, R.string.text2,
            R.string.text3, R.string.text4, R.string.text5,
            R.string.text6, R.string.text7, R.string.text8};
    private final String[] functions = {"收藏夹", "三国人物", "演义全文",
            "三国战争", "三国计谋", "三国事件",
            "三国地名", "三国官爵"};
    private final String[] short_titles = {
            "积水成河，风云兴焉",    //收藏夹
            "江山如画，一时多少豪杰",  //三国人物
            "周公吐哺，天下归心",  //演义章节
            "金戈铁马，气吞万里如虎",  //三国战争
            "谈笑间，樯橹灰飞烟灭",  //三国计谋
            "古今多少事，都付笑谈中",  //三国事件
            "故垒西边，人道是，三国周郎赤壁",  //三国地名
            "谋士风流，三国起，一时峥嵘岁月"};  //三国官爵

    private final SliderAdapter sliderAdapter = new SliderAdapter(pics, pics.length, new OnCardClickListener());

    private CardSliderLayoutManager layoutManger;
    private RecyclerView recyclerView;
    private ImageSwitcher backgroundSwitcher;
    private TextSwitcher shortTitleSwitcher;
    private TextSwitcher descriptionsSwitcher;

    private TextView function1TextView;
    private TextView function2TextView;
    private int functionOffset1;
    private int functionOffset2;
    private long functionAnimDuration;
    private int currentPosition;

    private DecodeBitmapTask decodeMapBitmapTask;
    private DecodeBitmapTask.Listener mapLoadListener;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);


        initRecyclerView();
        iniFunctionText();
        initSwitchers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && decodeMapBitmapTask != null) {
            decodeMapBitmapTask.cancel(true);
        }
    }

    /**
     * Initial the function recyclerView;
     */
    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(sliderAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onActiveCardChange();
                }
            }
        });

        layoutManger = (CardSliderLayoutManager) recyclerView.getLayoutManager();

        new CardSnapHelper().attachToRecyclerView(recyclerView);
    }
    private void onActiveCardChange() {
        final int pos = layoutManger.getActiveCardPosition();
        if (pos == RecyclerView.NO_POSITION || pos == currentPosition) {
            return;
        }

        onActiveCardChange(pos);
    }
    private void onActiveCardChange(int pos) {
        int animH[] = new int[] {R.anim.slide_in_right, R.anim.slide_out_left};
        int animV[] = new int[] {R.anim.slide_in_top, R.anim.slide_out_bottom};

        final boolean left2right = pos < currentPosition;
        if (left2right) {
            animH[0] = R.anim.slide_in_left;
            animH[1] = R.anim.slide_out_right;

            animV[0] = R.anim.slide_in_bottom;
            animV[1] = R.anim.slide_out_top;
        }

        setFunctionText(functions[pos % functions.length], left2right);

        shortTitleSwitcher.setInAnimation(MainActivity.this, animV[0]);
        shortTitleSwitcher.setOutAnimation(MainActivity.this, animV[1]);
        shortTitleSwitcher.setText(short_titles[pos % short_titles.length]);

        descriptionsSwitcher.setText(getString(descriptions[pos % descriptions.length]));

        showMap(backgrounds[pos % backgrounds.length]);

        currentPosition = pos;
    }
    private void showMap(@DrawableRes int resId) {
        if (decodeMapBitmapTask != null) {
            decodeMapBitmapTask.cancel(true);
        }

        final int w = backgroundSwitcher.getWidth();
        final int h = backgroundSwitcher.getHeight();

        decodeMapBitmapTask = new DecodeBitmapTask(getResources(), resId, w, h, mapLoadListener);
        decodeMapBitmapTask.execute();
    }
    private void setFunctionText(String text, boolean left2right) {
        final TextView invisibleText;
        final TextView visibleText;
        if (function1TextView.getAlpha() > function2TextView.getAlpha()) {
            visibleText = function1TextView;
            invisibleText = function2TextView;
        } else {
            visibleText = function2TextView;
            invisibleText = function1TextView;
        }

        final int vOffset;
        if (left2right) {
            invisibleText.setX(0);
            vOffset = functionOffset2;
        } else {
            invisibleText.setX(functionOffset2);
            vOffset = 0;
        }

        invisibleText.setText(text);

        final ObjectAnimator iAlpha = ObjectAnimator.ofFloat(invisibleText, "alpha", 1f);
        final ObjectAnimator vAlpha = ObjectAnimator.ofFloat(visibleText, "alpha", 0f);
        final ObjectAnimator iX = ObjectAnimator.ofFloat(invisibleText, "x", functionOffset1);
        final ObjectAnimator vX = ObjectAnimator.ofFloat(visibleText, "x", vOffset);

        final AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(iAlpha, vAlpha, iX, vX);
        animSet.setDuration(functionAnimDuration);
        animSet.start();
    }

    /**
     * Initial switchers
     */
    private void initSwitchers() {

        shortTitleSwitcher = (TextSwitcher) findViewById(R.id.ts_short_title);
        shortTitleSwitcher.setFactory(new TextViewFactory(R.style.PlaceTextView, false));
        shortTitleSwitcher.setCurrentText(short_titles[0]);

        descriptionsSwitcher = (TextSwitcher) findViewById(R.id.ts_description);
        descriptionsSwitcher.setInAnimation(this, android.R.anim.fade_in);
        descriptionsSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        descriptionsSwitcher.setFactory(new TextViewFactory(R.style.DescriptionTextView, false));
        descriptionsSwitcher.setCurrentText(getString(descriptions[0]));

        backgroundSwitcher = (ImageSwitcher) findViewById(R.id.ts_background);
        backgroundSwitcher.setInAnimation(this, R.anim.fade_in);
        backgroundSwitcher.setOutAnimation(this, R.anim.fade_out);
        backgroundSwitcher.setFactory(new ImageViewFactory());
        backgroundSwitcher.setImageResource(backgrounds[0]);

        mapLoadListener = new DecodeBitmapTask.Listener() {
            @Override
            public void onPostExecuted(Bitmap bitmap) {
                ((ImageView)backgroundSwitcher.getNextView()).setImageBitmap(bitmap);
                backgroundSwitcher.showNext();
            }
        };
    }

    /**
     * Initial function textView
     */
    private void iniFunctionText() {
        functionAnimDuration = getResources().getInteger(R.integer.labels_animation_duration);
        functionOffset1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        functionOffset2 = getResources().getDimensionPixelSize(R.dimen.card_width);
        function1TextView = (TextView) findViewById(R.id.tv_function_1);
        function2TextView = (TextView) findViewById(R.id.tv_function_2);

        function1TextView.setX(functionOffset1);
        function2TextView.setX(functionOffset2);
        function1TextView.setText(functions[0]);
        function2TextView.setAlpha(0f);

        function1TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
        function2TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
    }

    /**
     * add three private class here: OnCardClickListener, TextViewFactory, ImageViewFactory
     * 
     * OnCardClickListener: Handle the Card Click and make card jump to the second activities
     * TextViewFactory: This is the class for creating new TextView in MainActivities
     * ImageViewFactory: This is the class for creating new ImageView in MainActivities
     * 
     */
    private class OnCardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final CardSliderLayoutManager lm =  (CardSliderLayoutManager) recyclerView.getLayoutManager();

            if (lm.isSmoothScrolling()) {
                return;
            }

            final int activeCardPosition = lm.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return;
            }


            final int clickedPosition = recyclerView.getChildAdapterPosition(view);
            if (clickedPosition == activeCardPosition) {
                switch (activeCardPosition){
                    case 0:
                        //Toast.makeText(MainActivity.this, "收藏夹功能尚未开发，敬请期待(>3<)~", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(context, CharactersActivity.class);
                        intent.putExtra("fav", true);
                        startActivityForResult(intent, 1);
                        break;
                    case 1:
                        //Toast.makeText(MainActivity.this, "三国人物功能正在开发中，敬请期待(>3<)~", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(context, CharactersActivity.class);
                        intent.putExtra("fav", false);
                        startActivityForResult(intent, 1);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "该功能正在开发中，敬请期待(>3<)~", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (clickedPosition > activeCardPosition) {
                recyclerView.smoothScrollToPosition(clickedPosition);
                onActiveCardChange(clickedPosition);
            }

        }
    }
    private class TextViewFactory implements  ViewSwitcher.ViewFactory {

        @StyleRes
        final int styleId;
        final boolean center;

        TextViewFactory(@StyleRes int styleId, boolean center) {
            this.styleId = styleId;
            this.center = center;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View makeView() {
            final TextView textView = new TextView(MainActivity.this);

            if (center) {
                textView.setGravity(Gravity.CENTER);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                textView.setTextAppearance(MainActivity.this, styleId);
            } else {
                textView.setTextAppearance(styleId);
            }

            return textView;
        }

    }

    private class ImageViewFactory implements ViewSwitcher.ViewFactory {
        @Override
        public View makeView() {
            final ImageView imageView = new ImageView(MainActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            final ViewGroup.LayoutParams lp = new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(lp);

            return imageView;
        }
    }

    /**
     * Verify the storage permissions for an activity.
     * Only READ_EXTERNAL_STORAGE for now.
     * @param activity The activity which need to be verified.
     */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            int read_permission = ActivityCompat.checkSelfPermission(
                    activity,
                    STORAGE_READ_PERMISSION
            );
            int write_permission = ActivityCompat.checkSelfPermission(
                    activity,
                    STORAGE_WRITE_PERMISSION
            );
            if (read_permission != PackageManager.PERMISSION_GRANTED ||
                write_permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{ STORAGE_READ_PERMISSION, STORAGE_WRITE_PERMISSION },
                        0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == R.integer.CharactersActivity_return_MainActivity) {

        }
        //从CharacterActivity返回
    }
}
