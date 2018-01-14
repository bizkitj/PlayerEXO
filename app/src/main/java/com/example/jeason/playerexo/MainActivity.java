package com.example.jeason.playerexo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.audio.SimpleDecoderAudioRenderer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int FIRST_ASSEMBLY_CN = 300404;
    private static final int SECOND_ASSEMBLY_CN = 858362;
    private static final int THIRD_ASSEMBLY_CN = 961219;
    private static final int FOURTH_ASSEMBLY_CN = 1272504;
    private static final int FIFTH_ASSEMBLY_CN = 1473297;
    private static final int MANTRA_HEART_CN = 1736345;
    private static final int FIRST_ASSEMBLY_Sanskrit = 4841;
    private static final int SECOND_ASSEMBLY_sanskrit = 354395;// 354395.126984127;
    private static final int THIRD_ASSEMBLY_sanskrit = 457939;//457939.615079365;
    private static final int FOURTH_ASSEMBLY_sanskrit = 692168;//692168.98015873;
    private static final int FIFTH_ASSEMBLY_sanskrit = 880580;// 880580.472222222;
    private static final int MANTRA_HEART_sanskrit = 1090716;
    private final Handler updateCurrentPostionHandler = new Handler();
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow;
    private long playbackPosition;
    private RecyclerView mWordList;
    private Button chapterOne, chapterTwo, chapterThree, chapterFour, chapterFive, mantraEssence;
    private WordAdapter mAdapter;
    private int mediaCurrentPositionInSecond;
    private LinearLayoutManager layoutManager;
    private int lastCompletelyVisibleItem;
    private ArrayList<Integer> subTitlePosition = new ArrayList<>();
    private String jSonStringFromAsset;
    private ArrayList<String> mantraLinesSimplifiedCN = new ArrayList<>();
    private ArrayList<String> mantraLinesSimplifiedCNPinYin = new ArrayList<>();
    private SparseIntArray subtitlePosItemMap;
    private Runnable updateMediaCurrentPositionRunnable = new Runnable() {
        @Override
        public void run() {
            long mediaCurrentPosition = player == null ? 0 : player.getCurrentPosition();
            //test code of progressing the Mantra
            mediaCurrentPositionInSecond = (int) Math.round(mediaCurrentPosition / 1000.0);
            Log.v(TAG, "CurrentPos in second " + String.valueOf(mediaCurrentPositionInSecond));
            //progress the Mantra
            updateHighlightItem();
            updateCurrentPostionHandler.postDelayed(this, 1000);

        }
    };

    private void updateHighlightItem() {
        int indexOfValue = subtitlePosItemMap.indexOfValue(mediaCurrentPositionInSecond);
        Log.v(TAG, "indexOfValue " + String.valueOf(indexOfValue));
        if (indexOfValue != -1) {
            mAdapter.setRowHighlightUpdateTracker(indexOfValue);
            mAdapter.notifyDataSetChanged();
            int storedPosition = subtitlePosItemMap.get(indexOfValue);
            Log.v(TAG, "storedPosition " + String.valueOf(storedPosition));
            mWordList.smoothScrollToPosition(indexOfValue);
//            mWordList.scrollToPosition(indexOfValue);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeChapterButtons();
        initializeRecyclerView();
        findLastCompletelyVisibleItemPostionInRecyclerView();
//        highlightItemUpdate();
    }

    private void initializeChapterButtons() {
        chapterOne = findViewById(R.id.buttonOne);
        chapterOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(FIRST_ASSEMBLY_Sanskrit);
//                mediaCurrentPositionInSecond = (int) Math.round(300404 / 1000.0);
            }
        });
        chapterTwo = findViewById(R.id.buttonTwo);
        chapterTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(SECOND_ASSEMBLY_sanskrit);

            }
        });
        chapterThree = findViewById(R.id.buttonThree);
        chapterThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(THIRD_ASSEMBLY_sanskrit);
            }
        });
        chapterFour = findViewById(R.id.buttonFour);
        chapterFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(FOURTH_ASSEMBLY_sanskrit);
            }
        });
        chapterFive = findViewById(R.id.buttonFive);
        chapterFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(FIFTH_ASSEMBLY_sanskrit);
            }
        });
        mantraEssence = findViewById(R.id.buttonMantraEssence);
        mantraEssence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(MANTRA_HEART_sanskrit);
            }
        });
    }

    private void parseJsonFile(String incomingJsonString) {
        subtitlePosItemMap = new SparseIntArray();
        try {
            JSONArray jsonArray = new JSONArray(incomingJsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
//                String line = object.getString("line");
                long lineStartPostion = object.getLong("startTime");
                Double postionInSecond = lineStartPostion / 1000.0;
                mantraLinesSimplifiedCN.add(object.getString("content"));
//                mantraLinesSimplifiedCNPinYin.add(object.getString("CHN_PINYIN"));
                subTitlePosition.add((int) Math.round(postionInSecond));
                //Map position with counter
                subtitlePosItemMap.append(i, (int) Math.round(postionInSecond));
//                Log.v(TAG, "subTitlePosition " + String.valueOf(subTitlePosition));
            }
//            Log.v("Json line ", line);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJsonFromAsset(InputStream input) {
        String json;
        byte[] data;
        try {
            data = new byte[input.available()];
            if (input.read(data) == -1) {
                throw new EOFException();
            }
            json = new String(data, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private void initializeRecyclerView() {
        mWordList = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(1);
        mAdapter = new WordAdapter(mantraLinesSimplifiedCN, this);
        mWordList.setLayoutManager(layoutManager);
        mWordList.setAdapter(mAdapter);
        mWordList.setHasFixedSize(true);
        SeparatorDecoration itemDecoration = new SeparatorDecoration(
                this, getResources().getColor(R.color.divider), 0.5f);
        mWordList.addItemDecoration(itemDecoration);
        mWordList.setItemAnimator(new DefaultItemAnimator());

    }

    private void findLastCompletelyVisibleItemPostionInRecyclerView() {
        mWordList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lastCompletelyVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                Log.v(TAG, "lastCompletelyVisibleItem: " + String.valueOf(lastCompletelyVisibleItem));
                mWordList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
//        MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.shurangma_in_sanskrit)));//Chinese online mentra
        MediaSource mediaSource = localMediaSource();//local sanskrit
        player.prepare(mediaSource, true, false);
//        player.sendMessages();
    }

    private MediaSource localMediaSource() {
        Uri uri = RawResourceDataSource.buildRawResourceUri(R.raw.shurangama_in_sanskrit_audio);
        DataSpec dataSpec = new DataSpec(uri);
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(this);
        ExtractorMediaSource mediaSource = null;
        try {
            rawResourceDataSource.open(dataSpec);
            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return rawResourceDataSource;
                }
            };
            mediaSource = new ExtractorMediaSource(rawResourceDataSource.getUri(),
                    factory, new DefaultExtractorsFactory(), null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaSource;
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
        updateCurrentPostionHandler.post(updateMediaCurrentPositionRunnable);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
        if (jSonStringFromAsset == null) {
//            jSonStringFromAsset = loadJsonFromAsset(getResources().openRawResource(R.raw.lengyanzhou_xuexiban_drba_all));
            jSonStringFromAsset = loadJsonFromAsset(getResources().openRawResource(R.raw.shurangama_in_sanskrit));
            parseJsonFile(jSonStringFromAsset);
            updateCurrentPostionHandler.post(updateMediaCurrentPositionRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
        removeAllCallBacks();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        removeAllCallBacks();
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

    private void removeAllCallBacks() {
        updateCurrentPostionHandler.removeCallbacks(updateMediaCurrentPositionRunnable);
    }


}
