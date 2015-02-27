package cs4962_002.palettepaint;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;


public class PaintActivity extends Activity {
    PaintAreaView paintArea;
    //PaletteView paletteLayout;
    //MenuView paintMenu;
    LinearLayout menuLayout;
    Button _goToPalette;  // true
    Button _switchMode; // false
    Button _play;
    SeekBar _timeBar;
    Boolean _menuState = true;
    Boolean _pause = true;
    int _percentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout paintAreaLayout = new LinearLayout(this);
        paintArea = new PaintAreaView(this);

        paintAreaLayout.addView(paintArea, new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        paintAreaLayout.setBackgroundColor(Color.WHITE);

        menuLayout = new LinearLayout(this);
        menuLayout.setBackgroundColor(Color.DKGRAY);
        createMenuButtons();
        setButtonListeners();

        if(savedInstanceState != null) {
            paintArea.set_paintPaths((ArrayList<PaintPath>) savedInstanceState.get("PaintPaths"));
            paintArea.set_activeColor(savedInstanceState.getInt("PaintAreaViewSelectedColor"));
        }

        mainLayout.addView(paintAreaLayout,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 2));
        mainLayout.addView(menuLayout,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 140, 1));

        setContentView(mainLayout);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        try
        {
            Gson gson = new Gson();
            ArrayList<PaintPath> paintLines = paintArea.get_paintPaths();
            String jsonPaint = gson.toJson(paintLines);
            File file = new File(getFilesDir(), "PaintState.txt");
            FileWriter textWriter = new FileWriter(file);
            BufferedWriter bufferedTextWriter = new BufferedWriter(textWriter);
            bufferedTextWriter.write(jsonPaint);

            bufferedTextWriter.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            File file = new File(getFilesDir(), "PaintState.txt");
            FileReader textReader = new FileReader(file);
            BufferedReader bufferedTextReader = new BufferedReader(textReader);
            String stateInfo = null;
            String wholeState = "";

            do
            {
                stateInfo = bufferedTextReader.readLine();
                wholeState += stateInfo;
            }while(stateInfo != null);

            if(wholeState != null) {
                wholeState = wholeState.replace("null", "");

                Gson getPaints = new Gson();
                Type arrayPaintsToken = new TypeToken<ArrayList<PaintPath>>() {
                }.getType();
                ArrayList<PaintPath> savedPaints = (ArrayList<PaintPath>) getPaints.fromJson(wholeState, arrayPaintsToken);
                paintArea.set_paintPaths(savedPaints);
            }

            bufferedTextReader.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PaletteActivity.ACTIVE_COLOR_REQUEST_CODE)
        {
            // Maybe check result code?
            paintArea.set_activeColor(data.getIntExtra(PaletteActivity.ACTIVE_COLOR_EXTRA, -1));
            _goToPalette.setBackgroundColor(paintArea.get_activeColor());

            if(paintArea.get_activeColor() == Color.WHITE)
                _goToPalette.setTextColor(Color.BLACK);
            else
                _goToPalette.setTextColor(Color.WHITE);
        }
        else
            Log.i("onActivityResult", "Received bad request code.");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<PaintPath> gr = paintArea.get_paintPaths();
        outState.putInt("PaintAreaViewSelectedColor", paintArea.get_activeColor());
        outState.putParcelableArrayList("PaintPaths", paintArea.get_paintPaths());
    }

    public void createMenuButtons()
    {
        _goToPalette = new Button(this);
        _goToPalette.setText("Color Palette");
        _goToPalette.setTextColor(Color.WHITE);

        _switchMode = new Button(this);
        _switchMode.setText("Watch Mode");
        _switchMode.setTextColor(Color.WHITE);

        _play = new Button(this);
        _play.setText("Play");
        _play.setTextColor(Color.WHITE);

        _timeBar = new SeekBar(this);
        _timeBar.setMax(100);
        //_timeBar.setThumbOffset(100);

        _menuState = true;
        //_playing = false;

        _switchMode.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        _goToPalette.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        _play.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        _timeBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        changeMenu();

        menuLayout.addView(_switchMode);
        menuLayout.addView(_goToPalette);
        menuLayout.addView(_play);
        menuLayout.addView(_timeBar);
    }

    public void changeMenu()
    {
        // Default state is painting mode.
        if(_menuState == true)
        {
            _switchMode.setText("Watch Mode");
            _goToPalette.setVisibility(View.VISIBLE);
            _play.setVisibility(View.GONE);
            _timeBar.setVisibility(View.GONE);
            paintArea.setPainting(true);
            _menuState = false;
        }
        else
        {
            _switchMode.setText("Paint Mode");
            _goToPalette.setVisibility(View.GONE);
            _play.setVisibility(View.VISIBLE);
            _timeBar.setVisibility(View.VISIBLE);
            paintArea.setPainting(false);
            _menuState = true;
        }
    }

    public void setButtonListeners()
    {
        _goToPalette.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent activeColorDetail = new Intent();
                activeColorDetail.putExtra(PaletteActivity.ACTIVE_COLOR_EXTRA, paintArea.get_activeColor());
                activeColorDetail.setClass(PaintActivity.this, PaletteActivity.class);
                startActivityForResult(activeColorDetail, PaletteActivity.ACTIVE_COLOR_REQUEST_CODE);
            }
        });

        _play.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ObjectAnimator animator = new ObjectAnimator();
                animator.cancel();

                if(_pause) {
                    _pause = false;
                    animator.setTarget(paintArea);
                    animator.setPropertyName("_percent");
                    animator.setDuration(3000);
                    animator.setFloatValues(0.0f, 1.0f);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            _timeBar.setProgress((int)(paintArea.get_percent()*100));
                        }
                    });

                    animator.start();
                    _play.setText("Pause");
                }
                else {
                    _pause = true;
                    animator.cancel();
                    _play.setText("Play");
                }
            }
        });

        _switchMode.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.i("Current menu state", _menuState.toString());
                changeMenu();
            }
        });

        _timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paintArea.set_percent((float)progress/100);
                Log.i("SeekBar Progress", "Weee");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
