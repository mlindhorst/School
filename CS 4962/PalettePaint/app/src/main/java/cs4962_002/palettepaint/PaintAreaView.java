package cs4962_002.palettepaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Melynda on 9/17/2014.
 */
public class PaintAreaView extends View {
    Paint _bPaint;
    Path _bPath;
    Bitmap _bMap;
    Canvas _drawCanvas;
    Paint _mainPaint;
    Path _mainPath;
    ArrayList<PaintPath> _paintPaths;
    ArrayList<PointF> _paintPath;
    int _activeColor;
    float _percent = 0.0f;
    double _percentPoints = -0.1;
    int _totalPoints = 0;
    Boolean painting = true;

    public PaintAreaView(Context context)
    {
        super(context);

        _activeColor = Color.BLACK;

        _bPaint = new Paint(Paint.DITHER_FLAG);
        _bPath = new Path();
        _mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _mainPath = new Path();

        _mainPaint.setColor(_activeColor);
        _mainPaint.setStyle(Paint.Style.STROKE);
        _mainPaint.setStrokeWidth(3f);

        _paintPaths = new ArrayList<PaintPath>();

        setMinimumHeight(600);
    }

    public ArrayList<PaintPath> get_paintPaths() {
        return _paintPaths;
    }

    public void set_paintPaths(ArrayList<PaintPath> _paintPaths) {
        this._paintPaths = _paintPaths;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        _bMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        _drawCanvas = new Canvas(_bMap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSpec = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpec = MeasureSpec.getSize(heightMeasureSpec);

        int width = getSuggestedMinimumWidth();
        int height = getSuggestedMinimumHeight();

        if(widthMode == MeasureSpec.AT_MOST)
            width = widthSpec;
        if(heightMode == MeasureSpec.AT_MOST)
            height = heightSpec;

        if(widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSpec;
            height = width;
        }
        if(heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSpec;
            width = height;
        }

        if(width > height && widthMode != MeasureSpec.EXACTLY)
            width = height;
        if(height > width && heightMode != MeasureSpec.EXACTLY)
            height = width;

        //if(widthMode == MeasureSpec.EXACTLY && heightMeasureSpec == MeasureSpec.EXACTLY)

        setMeasuredDimension(
                resolveSizeAndState(width, widthMeasureSpec, width < getSuggestedMinimumWidth() ? MEASURED_STATE_TOO_SMALL : 0),
                resolveSizeAndState(height, widthMeasureSpec, height < getSuggestedMinimumHeight() ? MEASURED_STATE_TOO_SMALL : 0));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // Allows user to paint with finger!
        super.onDraw(canvas);

        _percentPoints = _percent * (float)countPoints();

        int pointCounter = (int) _percentPoints;
        Boolean first = false;
        if(pointCounter == 0)
            first = true;

        //canvas.drawPath(_mainPath, _mainPaint);

        for(PaintPath pp : _paintPaths)
        {
            int color = pp.getColor();

            PointF[] p = pp.getPointArray();

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            Path newPath = new Path();
            paint.setColor(color);

            for(int i = 0; i < p.length; i++)
            {
                if(first || pointCounter != 0) {
                    if (i == 0)
                        newPath.moveTo(p[i].x, p[i].y);
                    else
                        newPath.lineTo(p[i].x, p[i].y);
                }
                else
                    break;

                pointCounter --;
            }
            if(pointCounter <= 0 && !first)
                break;

            canvas.drawPath(newPath, paint);
        }

        first = false;
    }

    public void set_percent(float percy)
    {
        _percent = percy;
        invalidate();
        //setPercentPoints();
    }

    public double get_percent()
    {
        return _percent;
    }

    /*public void setPercentPoints()
    {
        _percentPoints = _percent * (double)countPoints();
        invalidate();
    }*/

    public int countPoints()
    {
        int counter = 0;

        for(PaintPath pp : _paintPaths)
        {
            counter += pp.getPointArray().length;
        }

        return counter;
    }

    public int get_activeColor() {
        return _activeColor;
    }

    public void set_activeColor(int _activeColor) {
        this._activeColor = _activeColor;

        if(_activeColor != -1)
            _mainPaint.setColor(_activeColor);
        else
        {
            Log.i("Received active color", "-1!!!");
            _mainPaint.setColor(Color.BLACK);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float downx = 0;
        float downy = 0;
        float x = 0;
        float y = 0;
        float upx = 0;
        float upy = 0;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                _paintPath = new ArrayList<PointF>();
                downx = event.getX();
                downy = event.getY();
                _paintPath.add(new PointF(downx, downy));
                _mainPath = new Path();
                _mainPath.moveTo(downx, downy);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                _paintPath.add(new PointF(x, y));
                _mainPath.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upx = event.getX();
                upy = event.getY();
                if(painting) {
                    _totalPoints++;
                    _paintPath.add(new PointF(upx, upy));
                    _paintPaths.add(new PaintPath(_activeColor, (PointF[]) _paintPath.toArray(new PointF[_paintPath.size()])));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }

        return true;
    }

    public void setPainting(boolean isPainting)
    {
        painting = isPainting;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Bundle instanceState = new Bundle();

        instanceState.putParcelableArrayList("floatArrayList", _paintPaths);
        instanceState.putParcelable("superState", super.onSaveInstanceState());

        return instanceState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if(state.getClass() != Bundle.class)
            return;

        Bundle instanceState = (Bundle)state;

        if(instanceState.containsKey("superState"))
        {
            super.onRestoreInstanceState(instanceState.getParcelable("superState"));
        }

        if(instanceState.containsKey("floatArrayList"))
        {
            ArrayList<PaintPath> reloadPaintPaths = instanceState.getParcelableArrayList("activeColor");
           _totalPoints = 0;

            for(PaintPath pp : reloadPaintPaths)
            {
                int color = pp.getColor();
                PointF[] paths = pp.getPointArray();

                set_activeColor(color);
                _bPath.reset();
                _bPath.moveTo(paths[0].x, paths[0].y);

                for(int pointsIndex = 1; pointsIndex < paths.length - 2; pointsIndex++) {
                    _mainPath.lineTo(paths[pointsIndex].x, paths[pointsIndex].y);
                    _totalPoints++;
                }

                _bPath.lineTo(paths[paths.length - 1].x, paths[paths.length - 1].y);
            }
        }
    }
}

class PaintPath implements Parcelable
{
    private int color;
    private PointF[] pointArray;

    public PaintPath(int c, PointF[] xy)
    {
        color = c;
        pointArray = xy;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public PointF[] getPointArray() {
        return pointArray;
    }

    public void setPointArray(PointF[] pointArray) {
        this.pointArray = pointArray;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(color);
        out.writeParcelableArray(pointArray, flags);
    }

    public static final Parcelable.Creator<PaintPath> CREATOR
            = new Parcelable.Creator<PaintPath>() {
        public PaintPath createFromParcel(Parcel in) {
            return new PaintPath(in);
        }

        public PaintPath[] newArray(int size) {
            return new PaintPath[size];
        }
    };

    private PaintPath(Parcel in) {
        color = in.readInt();
        pointArray = (PointF[])in.readParcelableArray(PointF.class.getClassLoader());
    }
}
