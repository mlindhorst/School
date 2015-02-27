package cs4962_002.palettepaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Melynda on 9/17/2014.
 */
public class PaintView extends View
{
    OnSplotchTouchListener _onSplotchTouchListener = null;
    int _color = Color. CYAN;
    int _totalSplotches;
    RectF _contentRect;
    float _radius;

    public boolean is_active() {
        return _active;
    }

    public void set_active(boolean _active) {
        this._active = _active;
    }

    boolean _active = false;

    public PaintView(Context context)
    {
        super(context);
        _totalSplotches = 5;
    }

    public int getColor()
    {
        return _color;
    }

    public void setColor(int color) {
        _color = color;
        invalidate();
    }

    public interface OnSplotchTouchListener
    {
        public void onSplotchTouch(PaintView v);
    }

    public void setOnSplotchTouchListener(OnSplotchTouchListener listener) {
        _onSplotchTouchListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        float circleCenterX = _contentRect.centerX();
        float circleCenterY = _contentRect.centerY();

        float distance = (float)Math.sqrt((circleCenterX - x)*(circleCenterX-x)+(circleCenterY-y)*(circleCenterY-y));

        if(_onSplotchTouchListener != null)
            _onSplotchTouchListener.onSplotchTouch(this);

        return super.onTouchEvent(event);
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

        setMeasuredDimension(
                resolveSizeAndState(width, widthMeasureSpec, width < getSuggestedMinimumWidth() ? MEASURED_STATE_TOO_SMALL : 0),
                resolveSizeAndState(height, widthMeasureSpec, height < getSuggestedMinimumHeight() ? MEASURED_STATE_TOO_SMALL : 0));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(_color);
        Path path = new Path();

        _contentRect = new RectF();
        _contentRect.left = getPaddingLeft();
        _contentRect.top = getPaddingTop();
        _contentRect.right = getWidth() - getPaddingRight();
        _contentRect.bottom = getHeight() - getPaddingBottom();

        PointF center = new PointF(_contentRect.centerX(), _contentRect.centerY());
        float maxRadius = Math.min(_contentRect.width() * 0.5f, _contentRect.height() * 0.5f);
        float minRadius = 0.25f * maxRadius;
        _radius = minRadius + (maxRadius - minRadius) * 0.75f;
        int pointCount = 50;
        for (int pointIndex = 0; pointIndex < pointCount; pointIndex += 3) {

            PointF control1 = new PointF();
            float control1Radius = _radius + (float) (Math.random() - 0.5f) * 2.0f * (maxRadius - _radius);
            control1.x = center.x + control1Radius * (float) Math.cos(((double) pointIndex / (double) pointCount) * 2.0 * Math.PI);
            control1.y = center.y + control1Radius * (float) Math.sin(((double) pointIndex / (double) pointCount) * 2.0 * Math.PI);

            PointF control2 = new PointF();
            float control2Radius = _radius + (float) (Math.random() - 0.5f) * 2.0f * (maxRadius - _radius);
            control1.x = center.x + control2Radius * (float) Math.cos(((double) pointIndex / (double) pointCount) * 2.0 * Math.PI);
            control1.y = center.y + control2Radius * (float) Math.sin(((double) pointIndex / (double) pointCount) * 2.0 * Math.PI);

            if (pointIndex == 0)
                path.moveTo((control1.x + control2.x) / 2, (control1.y + control2.y) / 2);
            else
                path.lineTo((control1.x + control2.x) / 2, (control1.y + control2.y) / 2);
        }

        path.close();
        canvas.drawPath(path, paint);

        // Draw black lines around paints.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.BLACK);
        canvas.drawPath(path, paint);

        // Draw highlight on active paint.
        if(_active)
        {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6.0f);
            paint.setColor(Color.YELLOW);
            canvas.drawPath(path, paint);
        }
    }
}


