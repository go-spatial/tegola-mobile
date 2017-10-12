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
    private Rect rect = null, rect_text_bounds = null;
    private Paint paint_editor_margin_text = null;
    private Paint paint_editor_margin_div = null;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint_editor_margin_text = new Paint();
        paint_editor_margin_text.setStyle(Paint.Style.FILL);
        paint_editor_margin_text.setColor(Color.WHITE);
        //paint_editor_margin_text.setTextSize(20);
        paint_editor_margin_div = new Paint();
        paint_editor_margin_div.setColor(Color.WHITE);
        rect = new Rect();
        rect_text_bounds = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint_editor_margin_text.setTextSize(getTextSize());
        int baseline = getBaseline();
        int linecount = getLineCount();
        String s_last_line_num = "" + linecount;
        paint_editor_margin_text.getTextBounds(s_last_line_num, 0, s_last_line_num.length(), rect_text_bounds);
        int margin_text_rpad = 5;
        int margin_width = rect_text_bounds.width() + margin_text_rpad, margin_height = rect_text_bounds.height();
        int text_lpad = 5;
        setPadding(margin_width + text_lpad, 0, 0, 0);
        for (int i = 0; i < linecount; i++) {
            String s_line_num = "" + (i + 1);
            canvas.drawText(s_line_num, rect.left, baseline, paint_editor_margin_text);
            //canvas.drawLine(margin_width, baseline + margin_height, margin_width + 1, baseline, paint_editor_margin_div);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
