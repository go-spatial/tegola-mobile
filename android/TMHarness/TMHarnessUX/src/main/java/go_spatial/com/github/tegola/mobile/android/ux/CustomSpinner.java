package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;

import java.util.List;


public class CustomSpinner extends AppCompatSpinner {
    public static class Adapter extends ArrayAdapter<String> {
        public Adapter(@NonNull Context context, @NonNull List<String> items) {
            super(context, R.layout.spinner_item, items);   //use our layout for spinner items after selected
            super.setDropDownViewResource(android.R.layout.simple_spinner_item);    //use android's default layout for spinner items when we click the drop-down buttom to select an item
        }
    }

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
