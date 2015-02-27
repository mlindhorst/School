package cs4962_002.palettepaint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Melynda on 10/1/2014.
 */
public class PaletteActivity extends Activity
{
    public static String ACTIVE_COLOR_EXTRA = "active_color";
    public static int ACTIVE_COLOR_REQUEST_CODE = 13;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LinearLayout palLayout = new LinearLayout(this);
        palLayout.setOrientation(LinearLayout.VERTICAL);

        Button returnButton = new Button(this);
        returnButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        //returnButton.setGravity(2);
        returnButton.setText("Return to Painting");
        returnButton.setBackgroundColor(Color.DKGRAY);
        returnButton.setTextColor(Color.WHITE);

        final PaletteView palette = new PaletteView(this);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent colorIntent = new Intent();
                colorIntent.putExtra(ACTIVE_COLOR_EXTRA, palette.getActiveColor());
                setResult(ACTIVE_COLOR_REQUEST_CODE, colorIntent);
                finish();
            }
        });

        Intent receiveActiveColor = getIntent();
        int aColor = receiveActiveColor.getIntExtra(ACTIVE_COLOR_EXTRA, -1);

        if(aColor == -1)
            Log.i("Receiving color", "Oops, -1.");
        else {
            palette.addColor(aColor);
            palette.setActiveColor(aColor);
        }

            palLayout.addView(palette,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        palLayout.addView(returnButton);

        setContentView(palLayout);
    }
}
