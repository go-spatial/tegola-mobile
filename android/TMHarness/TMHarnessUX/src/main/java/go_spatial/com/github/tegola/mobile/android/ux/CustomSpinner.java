package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatSpinner;


public class CustomSpinner extends AppCompatSpinner {
    public CustomSpinner(Context context) {
        super(context);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int position) {
        final boolean nochange = position == getSelectedItemPosition();
        if (nochange)
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        else
            super.setSelection(position);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        final boolean nochange = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (nochange)
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        else
            super.setSelection(position, animate);
    }
}
