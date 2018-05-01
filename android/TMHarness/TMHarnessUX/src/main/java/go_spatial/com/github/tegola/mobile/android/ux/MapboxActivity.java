package go_spatial.com.github.tegola.mobile.android.ux;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.maps.MapView;

public class MapboxActivity extends AppCompatActivity {
    private ImageButton m_btn_mb__close = null;
    private MapView mapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapbox);
        m_btn_mb__close = (ImageButton)findViewById(R.id.btn_mb__close);
        m_btn_mb__close.setOnClickListener(OnClickListener__m_btn_mb__close);
        mapView = (MapView)findViewById(R.id.mapView);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
    }

    private final View.OnClickListener OnClickListener__m_btn_mb__close = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MapboxActivity.this.finish();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
