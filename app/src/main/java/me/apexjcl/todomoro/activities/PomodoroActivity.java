package me.apexjcl.todomoro.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import flepsik.github.com.progress_ring.ProgressRingView;
import me.apexjcl.todomoro.R;
import me.apexjcl.todomoro.logic.Pomodoro;
import me.apexjcl.todomoro.logic.Timer;
import me.apexjcl.todomoro.realm.handlers.TaskHandler;
import me.apexjcl.todomoro.realm.models.Task;


public class PomodoroActivity extends AppCompatActivity implements Timer.TimerListener {

    @BindView(R.id.control_button)
    ImageView mControlButton;
    @BindView(R.id.progressRing)
    ProgressRingView mProgressRing;
    @BindView(R.id.timer_text)
    TextView mTimerText;

    public static final String TASK_ID = "task_id";
    private Timer mTimer;
    private Pomodoro mPomodoro;
    private String mTaskId;
    private Task mTask;

    private boolean autoCycle = true;
    private boolean started = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        mTaskId = getIntent().getStringExtra(TASK_ID);
        if (mTaskId == null) {
            finish();
            return;
        }
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mTask = (Task) TaskHandler.getTask(mTaskId);
        mPomodoro = new Pomodoro(mTask);
        mTimer = new Timer.Builder()
                .setDuration(mPomodoro.getCycleTime())
                .setRemaining(mPomodoro.getRemainingTime())
                .setListener(this)
                .setCountUpdate(250)
                .build();
        updateTimeLabel(mPomodoro.getRemainingTime());
        mProgressRing.setProgress(mPomodoro.getCompletion());
    }


    @OnClick(R.id.control_button)
    void controlTimer() {
        switch (mTimer.getState()) {
            case INIT:
                mPomodoro.start();
                setPauseButton();
                mTimer.start();
                break;
            case PAUSED:
                mPomodoro.start();
                setPauseButton();
                mTimer.start();
                break;
            case RUNNING:
                mTimer.pause();
                mPomodoro.stop(mTimer.getRemaining());
                setPlayButton();
                break;
            case FINISHED:
                setPlayButton();
                break;
        }
    }


    private void setPlayButton() {
        mControlButton.setImageDrawable(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                        getDrawable(R.drawable.ic_play_arrow_24dp) :
                        getResources().getDrawable(R.drawable.ic_play_arrow_24dp)
        );
    }

    private void setPauseButton() {
        mControlButton.setImageDrawable(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                        getDrawable(R.drawable.ic_pause_black_24dp) :
                        getResources().getDrawable(R.drawable.ic_pause_black_24dp)
        );
    }

    private void updateTimeLabel(long tickUpdateInMillis) {
        long seconds = (tickUpdateInMillis / 1000) % 60;
        long minutes = (tickUpdateInMillis / (1000 * 60)) % 60;
        String time = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        mTimerText.setText(time);
    }

    @Override
    protected void onDestroy() {
        mTask = null;
        mPomodoro = null;
        super.onDestroy();
    }

    @Override
    public void onTick(long milisUntilFinished) {
        mProgressRing.setProgress(mPomodoro.getCompletion());
        updateTimeLabel(milisUntilFinished);
    }

    @Override
    public void onFinishCountdown() {
        mProgressRing.setProgress(1);
        mPomodoro.finishCycle();
        mTimer.setRemaining(mPomodoro.getCycleTime());
        setPlayButton();
    }
}
