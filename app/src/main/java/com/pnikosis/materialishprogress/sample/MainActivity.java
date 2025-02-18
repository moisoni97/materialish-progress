package com.pnikosis.materialishprogress.sample;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pnikosis.materialishprogress.ProgressWheel;


public class MainActivity extends AppCompatActivity {

    private ProgressWheel progressWheel;
    private ProgressWheel progressWheelInterpolated;
    private ProgressWheel progressWheelLinear;

    private TextView interpolatedValue;
    private TextView linearValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonAbout = findViewById(R.id.button_about);
        buttonAbout.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_text)
                    .setPositiveButton(R.string.ok, (dialog1, which) -> dialog1.dismiss());

            dialog.show();
        });

        progressWheel = findViewById(R.id.progress_wheel);
        progressWheelInterpolated = findViewById(R.id.interpolated);
        progressWheelLinear = findViewById(R.id.linear);

        interpolatedValue = findViewById(R.id.interpolatedValue);
        linearValue = findViewById(R.id.linearValue);

        Spinner spinnerOptions = findViewById(R.id.spinner_options);
        spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        progressWheelLinear.setProgress(0.0f);
                        progressWheelInterpolated.setProgress(0.0f);

                        progressWheelInterpolated.setCallback(progress -> {
                            if (progress == 0) {
                                progressWheelInterpolated.setProgress(1.0f);
                            } else if (progress == 1.0f) {
                                progressWheelInterpolated.setProgress(0.0f);
                            }

                            interpolatedValue.setText(String.format("%.2f", progress));
                        });

                        progressWheelLinear.setCallback(progress -> {
                            if (progress == 0) {
                                progressWheelLinear.setProgress(1.0f);
                            } else if (progress == 1.0f) {
                                progressWheelLinear.setProgress(0.0f);
                            }

                            linearValue.setText(String.format("%.2f", progress));
                        });
                        break;
                    case 1:
                        setProgress(0.0f);
                        break;
                    case 2:
                        setProgress(0.1f);
                        break;
                    case 3:
                        setProgress(0.25f);
                        break;
                    case 4:
                        setProgress(0.5f);
                        break;
                    case 5:
                        setProgress(0.75f);
                        break;
                    case 6:
                        setProgress(1.0f);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final int defaultBarColor = progressWheel.getBarColor();
        final int defaultWheelColor = progressWheel.getRimColor();

        Spinner colorOptions = findViewById(R.id.spinner_options_color);
        colorOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        progressWheel.setBarColor(defaultBarColor);
                        progressWheelInterpolated.setBarColor(defaultBarColor);
                        progressWheelLinear.setBarColor(defaultBarColor);
                        break;
                    case 1:
                        progressWheel.setBarColor(Color.RED);
                        progressWheelInterpolated.setBarColor(Color.RED);
                        progressWheelLinear.setBarColor(Color.RED);
                        break;
                    case 2:
                        progressWheel.setBarColor(Color.MAGENTA);
                        progressWheelInterpolated.setBarColor(Color.MAGENTA);
                        progressWheelLinear.setBarColor(Color.MAGENTA);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner wheelColorOptions = findViewById(R.id.spinner_options_rim_color);
        wheelColorOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        progressWheel.setRimColor(defaultWheelColor);
                        progressWheelInterpolated.setRimColor(defaultWheelColor);
                        progressWheelLinear.setRimColor(defaultWheelColor);
                        break;
                    case 1:
                        progressWheel.setRimColor(Color.LTGRAY);
                        progressWheelInterpolated.setRimColor(Color.LTGRAY);
                        progressWheelLinear.setRimColor(Color.LTGRAY);
                        break;
                    case 2:
                        progressWheel.setRimColor(Color.GRAY);
                        progressWheelInterpolated.setRimColor(Color.GRAY);
                        progressWheelLinear.setRimColor(Color.GRAY);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setProgress(float progress) {
        progressWheelLinear.setCallback(progress1 -> linearValue.setText(String.format("%.2f", progress1)));
        progressWheelInterpolated.setCallback(progress12 -> interpolatedValue.setText(String.format("%.2f", progress12)));

        progressWheelLinear.setProgress(progress);
        progressWheelInterpolated.setProgress(progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
