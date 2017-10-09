package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * Author S Mahbub Uz Zaman on 5/9/15.
 * Lisence Under GPL2
 *
 * https://gist.github.com/lifeparticle/de9224424d14a9d6e185
 */
public class CustomEditText extends android.support.v7.widget.AppCompatEditText {
    private Rect rect;
    private Paint paint;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawText("" + (i + 1), rect.left, baseline, paint);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
