package com.example.sikang.rotateview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;


public class MainActivity extends Activity {
    private SeekBar mSeekBar;
    private CircleLayout mCircleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleLayout = (CircleLayout) this.findViewById(R.id.activity_main_circleLayout);
        mSeekBar = (SeekBar) this.findViewById(R.id.activity_main_seekbar);
        mSeekBar.setMax(11);
        mSeekBar.setProgress(9);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()

                                            {
                                                @Override
                                                public void onProgressChanged(SeekBar seekBar,
                                                                              int progress, boolean fromUser) {
                                                    Log.d("Main", progress + "");
                                                    mCircleLayout.setChildCount(progress + 1);
                                                }

                                                @Override
                                                public void onStartTrackingTouch(SeekBar seekBar) {

                                                }

                                                @Override
                                                public void onStopTrackingTouch(SeekBar seekBar) {

                                                }
                                            }

        );
    }
}
