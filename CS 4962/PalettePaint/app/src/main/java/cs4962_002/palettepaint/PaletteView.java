package cs4962_002.palettepaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Melynda on 9/17/2014.
 */
public class PaletteView extends ViewGroup implements PaintView.OnSplotchTouchListener {
    public interface OnActiveColorChangedListener
    {
        public void onActiveColorChanged(PaletteView v);
    }

    private OnActiveColorChangedListener onActiveColorChangedListener = null;
    private boolean isDragging = false;

    public PaletteView(Context context)
    {
        super(context);

        addColor(Color.RED);
        addColor(Color.BLUE);
        addColor(Color.BLACK);
        addColor(Color.YELLOW);
        addColor(Color.WHITE);
    }

    // Only allow PaintViews to be added to the PaletteView.
    @Override
    public void addView(View child)
    {
        if(!(child instanceof PaintView))
            Log.e("PaletteView", "Can't add views to palette view.");
        else
            super.addView(child);
    }

    // Retrieve active color. If there is not one, return black.
    public int getActiveColor()
    {
        for(int paintViewIndex = 0; paintViewIndex < getChildCount(); paintViewIndex++)
        {
            PaintView paintView = (PaintView)getChildAt(paintViewIndex);

            if(paintView.is_active())
            {
                return paintView.getColor();
            }
        }

        return Color.BLACK;
    }

    // Set active color.
    public void setActiveColor(int c)
    {
        for(int paintViewIndex = 0; paintViewIndex < getChildCount(); paintViewIndex++)
        {
            PaintView paintView = (PaintView)getChildAt(paintViewIndex);

            if(paintView.getColor() == c)
                paintView.set_active(true);
            else
                paintView.set_active(false);
        }

        if(onActiveColorChangedListener != null)
            onActiveColorChangedListener.onActiveColorChanged(this);
    }


    public int[] getColors()
    {
        int[] colors = new int[getChildCount()];

        for(int paintViewIndex = 0; paintViewIndex < getChildCount(); paintViewIndex++)
        {
            PaintView paintView = (PaintView)getChildAt(paintViewIndex);
            colors[paintViewIndex] = paintView.getColor();
        }

        return colors;
    }

    // Create a new paint splotch with that color.
    public void addColor(int color)
    {
        for(int c : getColors())
        {
            if (c == color)
                return;
        }

        PaintView paintView = new PaintView(getContext());
        paintView.setColor(color);
        paintView.setOnSplotchTouchListener(new PaintView.OnSplotchTouchListener() {
            @Override
            public void onSplotchTouch(PaintView v) {
                //((PaintView)v).setColor(Color.GREEN);
                //v.setHighlight(true);
                v.set_active(true);
                setActiveColor(v.getColor());

                View.DragShadowBuilder splotchShadow = new View.DragShadowBuilder(v);
                v.startDrag(null, splotchShadow, v, 0);
            }
        });
        paintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        paintView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                String msg = "";
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        isDragging = true;
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        int x_cord = (int) event.getX();
                        int y_cord = (int) event.getY();
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        // Detect if splotch exits when x is less than viewgroup bounds.
                        x_cord = (int) event.getX();
                        y_cord = (int) event.getY();
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        x_cord = (int) event.getX();
                        y_cord = (int) event.getY();
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        boolean isSplotchy = false;
                        Log.i("Drag Ended", "drop");
                        PaintView dView = (PaintView)event.getLocalState();
                        for(int i = 0; i < getChildCount(); i++) {
                            View view = getChildAt(i);
                            if (dView.equals(view)) {
                                isSplotchy = true;
                            }
                        }
                        if(!isSplotchy)
                            removeColor(dView.getColor());
                        return true;
                    case DragEvent.ACTION_DROP:
                        if(isDragging) {
                            View draggedView = (View)event.getLocalState();
                            msg = draggedView.getX() + ", " + draggedView.getY();
                            Log.i("Dragged View", msg);
                            for(int i = 0; i < getChildCount(); i++) {
                                View view = getChildAt(i);
                                if (!draggedView.equals(view) && v.equals(view) /*dropDistance(view, new PointF(event.getX(), event.getY()))*/) {
                                    int newColorNum = mixColors(((PaintView) draggedView).getColor(), ((PaintView) view).getColor());
                                    addColor(newColorNum);
                                    break;
                                } else {
                                    Log.i(msg, "ON DROP");
                                }
                            }
                        }
                        else
                        {
                            Log.i("Whut", "WHAT?");
                        }
                        isDragging = false;
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });


        addView(paintView);
        invalidate();
    }

    // Removes color from palette.
    public void removeColor(int color)
    {
        int activeColor = getActiveColor();

        for(int paintViewIndex = getChildCount() - 1; paintViewIndex >= 0; paintViewIndex--)
        {
            PaintView paintView = (PaintView)getChildAt(paintViewIndex);

            if(paintView.getColor() == color)
                removeView(paintView);
        }

        if(onActiveColorChangedListener != null)
        {
            onActiveColorChangedListener.onActiveColorChanged(this);
        }

    }

    // Calculates distance between splotch and shadow splotch.
    private boolean dropDistance(View v1, PointF v2)
    {
        double dist = Math.sqrt(
                Math.pow((double)v1.getX() - (double)v2.x, 2) +
                Math.pow((double)v1.getY() - (double)v2.y, 2)
        );

        double dia = Math.min(v1.getHeight(), v1.getWidth());
        float x = v2.x;
        float y = v2.y;

        String message = "";
        message += dist;
        Log.i("Splotch Distance", message);
        message = v1.getX() + ", " + v1.getY();
        Log.i("Onto splotch", message);
        message = x + ", " + y;
        Log.i("Event point", message);

        return dist <= dia;
    }

    // Mixes two colors, returns a median value.
    private int mixColors(int c1, int c2)
    {
        int c1_r = (c1 & 0x00FF0000);
        int c1_g = (c1 & 0x0000FF00);
        int c1_b = (c1 & 0x000000FF);

        int c2_r = (c2 & 0x00FF0000);
        int c2_g = (c2 & 0x0000FF00);
        int c2_b = (c2 & 0x000000FF);

        c1_r = (c1_r + c2_r) / 2;
        c1_g = (c1_g + c2_g) / 2;
        c1_b = (c1_b + c2_b) / 2;

        return 0xFF000000 + c1_r + c1_g + c1_b;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int childWidthMax = 0;
        int childHeightMax = 0;
        for(int childIndex = 0; childIndex < getChildCount(); childIndex++)
        {
            View child = getChildAt(childIndex);
            childWidthMax = Math.max(childWidthMax, child.getMeasuredWidth());
            childHeightMax = Math.max(childHeightMax, child.getMeasuredHeight());
        }

        Rect layoutRect = new Rect();
        layoutRect.left = getPaddingLeft() + childWidthMax / 2;
        layoutRect.top = getPaddingTop() + childHeightMax / 2;
        layoutRect.right = getWidth() - getPaddingRight() - childWidthMax / 2;
        layoutRect.bottom = getHeight() - getPaddingBottom() - childHeightMax / 2;

        for(int childIndex = 0; childIndex < getChildCount(); childIndex++)
        {
            double angle = (double) childIndex / (double) getChildCount() * 2.0 * Math.PI;
            int childCenterX = (int) (layoutRect.centerX() + (double) layoutRect.width() * 0.5 * Math.cos(angle));
            int childCenterY = (int) (layoutRect.centerY() + (double) layoutRect.height() * 0.5 * Math.sin(angle));

            View child = getChildAt(childIndex);
            child.layout(0, 0, 50, 50);
            Rect childLayout = new Rect();
            childLayout.left = childCenterX - childWidthMax / 2;
            childLayout.top = childCenterY - childHeightMax / 2;
            childLayout.right = childCenterX + childWidthMax / 2;
            childLayout.bottom = childCenterY + childHeightMax / 2;
            child.layout(childLayout.left, childLayout.top, childLayout.right, childLayout.bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthSpec = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSpec = View.MeasureSpec.getSize(heightMeasureSpec);

        int width = Math.max(widthSpec, getSuggestedMinimumWidth());
        int height = Math.max(heightSpec, getSuggestedMinimumHeight());

        int childState = 0;
        for(int childIndex = 0; childIndex < getChildCount(); childIndex++)
        {
            View child = getChildAt(childIndex);
            //LayoutParams childLayoutParams = child.getLayoutParams();
            child.measure(MeasureSpec.AT_MOST | 100, MeasureSpec.AT_MOST | 100);
            //measureChild(child, widthMeasureSpec, heightMeasureSpec);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState), resolveSizeAndState(height, heightMeasureSpec, childState));
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Bundle instanceState = new Bundle();

        instanceState.putInt("activeColor", getActiveColor());
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

        if(instanceState.containsKey("activeColor"))
        {
            int activeColor = instanceState.getInt("activeColor");
            setActiveColor(activeColor);
        }
    }

    public interface OnSplotchTouchListener
    {
        public void onSplotchTouch(PaintView v);
    }

    //Add onsplotch listener
    public void onSplotchTouch(PaintView v)
    {

    }

    public void setOnSplotchTouchListener(OnSplotchTouchListener listener)
    {

    }

    public void setOnActiveColorChangedListener(OnActiveColorChangedListener listener)
    {
        onActiveColorChangedListener = listener;
    }
}
