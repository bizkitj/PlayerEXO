package com.example.jeason.playerexo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow;
    private long playbackPosition;
    private RecyclerView mWordList;
    private Button testButton;
    private WordAdapter mAdapter;
    private TextView currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeRecyclerView();
        highlightItemUpdate();
    }

    private void initializeRecyclerView() {
        mWordList = findViewById(R.id.recyclerView);
        String[] simplifiedChineseRowItem = getResources().getStringArray(R.array.simplifiedChinese);
        String[] chinesePinYinRowItem = getResources().getStringArray(R.array.ChinesePinYin);
        String[] englishRowItem = getResources().getStringArray(R.array.English);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(1);
        mAdapter = new WordAdapter(simplifiedChineseRowItem, this, chinesePinYinRowItem, englishRowItem);
        mWordList.setLayoutManager(layoutManager);
        mWordList.setAdapter(mAdapter);
        mWordList.setHasFixedSize(true);
        SeparatorDecoration itemDecoration = new SeparatorDecoration(
                this, getResources().getColor(R.color.divider), 0.5f);
        mWordList.addItemDecoration(itemDecoration);
        mWordList.setItemAnimator(new DefaultItemAnimator());
        /*
        findLastVisibleItemPosition from the RecyclerView
         */
        mWordList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int lastCompletelyVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                Log.v(TAG, "lastCompletelyVisibleItem: " + String.valueOf(lastCompletelyVisibleItem));
                mWordList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
//        scrollPosition = scrollPosition + lastVisibleItemPosition;

    }

    private void highlightItemUpdate() {
        testButton = findViewById(R.id.testButton);
        currentPosition = findViewById(R.id.currentPosition);
        final int[] highlightItem = {0};
        testButton.setText(String.valueOf(highlightItem[0]));
//        currentPosition.setText(String.valueOf(player.getCurrentPosition()));
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highlightItem[0]++;
                mAdapter.setRowHighlightUpdateTracker(highlightItem[0]);
                testButton.setText(String.valueOf(highlightItem[0]));
//                currentPosition.setText(String.valueOf(player.getCurrentPosition()));
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    private void initializePlayer() {
        SimpleExoPlayerView playerView = findViewById(R.id.playerView);
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
        MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url)));
        player.prepare(mediaSource, true, false);
//        player.sendMessages();
    }

    private void updateProgressBar() {
        long duration = player == null ? 0 : player.getDuration();
        long position = player == null ? 0 : player.getCurrentPosition();
    }



    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri,
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
}
