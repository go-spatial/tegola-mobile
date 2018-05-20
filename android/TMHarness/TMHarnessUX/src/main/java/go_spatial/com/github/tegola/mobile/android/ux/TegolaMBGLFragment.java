package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.http.HttpRequestUtil;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TegolaMBGLFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TegolaMBGLFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TegolaMBGLFragment extends android.support.v4.app.Fragment {
    public static final String TAG = TegolaMBGLFragment.class.getName();

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        enum E_MBGL_FRAG_ACTION {
            HIDE
        }
        void onFragmentInteraction(E_MBGL_FRAG_ACTION e_mbgl_frag_action);
    }

    private static final String ARG__MBGL_STYLE_URL = "mbgl_style_url";
    private String mbgl_style_url = "";
    private static final String ARG__C_LAT = "C_LAT";
    private double c_lat = -1.0;
    private static final String ARG__C_LONG = "C_LONG";
    private double c_long = -1.0;
    private static final String ARG__C_ZOOM = "C_ZOOM";
    private double c_zoom = -1.0;
    private static final String ARG__MBMAP_DEBUG_ACTIVE = "mbmap_debug_active";
    private boolean mbmap_debug_active = false;

    private OnFragmentInteractionListener mFragInteractionListener;
    private MapView mapView = null;
    private MapboxMap m_mapboxMap = null;

    private ImageButton ibtn_hide_mbgl_frag = null;

    public TegolaMBGLFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mbgl_style_url mbgl_style_url
     * @return A new instance of fragment TegolaMBGLFragment.
     */
    public static TegolaMBGLFragment newInstance(final String mbgl_style_url, final double latitude, final double longitude, final double zoom, final boolean mbmap_debug_active) {
        TegolaMBGLFragment fragment = new TegolaMBGLFragment();
        Bundle args = new Bundle();
        args.putString(ARG__MBGL_STYLE_URL, mbgl_style_url);
        args.putDouble(ARG__C_LAT, latitude);
        args.putDouble(ARG__C_LONG, longitude);
        args.putDouble(ARG__C_ZOOM, zoom);
        args.putBoolean(ARG__MBMAP_DEBUG_ACTIVE, mbmap_debug_active);
        fragment.setArguments(args);
        return fragment;
    }
    public static TegolaMBGLFragment newInstance(final String mbgl_style_url, final double latitude, final double longitude, final double zoom) {
        return newInstance(mbgl_style_url, latitude, longitude, zoom,false);
    }
    public static TegolaMBGLFragment newInstance(final String mbgl_style_url, final boolean mbmap_debug_active) {
        return newInstance(mbgl_style_url, -1.0, -1.0, -1.0, mbmap_debug_active);
    }
    public static TegolaMBGLFragment newInstance(final String mbgl_style_url) {
        return newInstance(mbgl_style_url, -1.0, -1.0, -1.0, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mbgl_style_url = getArguments().getString(ARG__MBGL_STYLE_URL);
            c_lat = getArguments().getDouble(ARG__C_LAT, -1.0);
            c_long = getArguments().getDouble(ARG__C_LONG, -1.0);
            c_zoom = getArguments().getDouble(ARG__C_ZOOM, -1.0);
            mbmap_debug_active = getArguments().getBoolean(ARG__MBMAP_DEBUG_ACTIVE);
        } else {
            Log.e(TAG, "(fragment) onCreate: cannot start mbgl mapview without mbgl style url!");
        }
    }

    private Dispatcher m_okhttp3_client_dispather = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View this_frag_layout_view = inflater.inflate(R.layout.fragment_mapbox, container, false);

        HttpRequestUtil.setLogEnabled(true);
        HttpRequestUtil.setPrintRequestUrlOnFailure(true);
        m_okhttp3_client_dispather = new Dispatcher();
        m_okhttp3_client_dispather.setMaxRequestsPerHost(20);
        HttpRequestUtil.setOkHttpClient(
            new OkHttpClient.Builder()
                .dispatcher(m_okhttp3_client_dispather)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
        );

        mapView = (MapView)this_frag_layout_view.findViewById(R.id.mapView);
        ibtn_hide_mbgl_frag = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_hide_mbgl_frag);
        ibtn_hide_mbgl_frag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragInteractionListener != null)
                    mFragInteractionListener.onFragmentInteraction(OnFragmentInteractionListener.E_MBGL_FRAG_ACTION.HIDE);
            }
        });

        mapView.getMapAsync(m_OnMapReadyCallback);

        Log.d(TAG, "(fragment) onCreateView: mbglstyle url: " + mbgl_style_url);
        mapView.setStyleUrl(mbgl_style_url);

        Log.d(TAG, "(fragment) onCreateView: calling mapView.onCreate()...");
        mapView.onCreate(savedInstanceState);

        return this_frag_layout_view;
    }

    final private OnMapReadyCallback m_OnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            m_mapboxMap = mapboxMap;
            Log.d(TAG, "(fragment) onMapReady: map is ready - min zoom level: " + m_mapboxMap.getMinZoomLevel() + "; max zoom level: " + m_mapboxMap.getMaxZoomLevel());

            CameraPosition camera_pos = m_mapboxMap.getCameraPosition();
            Log.d(TAG, "(fragment) onMapReady: map is ready - "
                    + "initial camera_pos.target.getLatitude(): " + camera_pos.target.getLatitude()
                    + "; initial camera_pos.target.getLongitude(): " + camera_pos.target.getLongitude()
                    + "; initial camera_pos.zoom: " + camera_pos.zoom
            );
            if (c_lat != -1.0 && c_long != -1.0 && c_zoom != -1.0) {
                m_mapboxMap.setCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(c_lat, c_long))
                                .zoom(c_zoom)
                                .bearing(0.0)
                                .build()
                );
            }
            m_mapboxMap.setPrefetchesTiles(true);
            m_mapboxMap.addOnCameraMoveListener(m_OnCameraMoveListener);
            m_mapboxMap.setDebugActive(mbmap_debug_active);

            m_mapboxMap.getUiSettings().setCompassEnabled(true);
        }
    };

    final private MapboxMap.OnCameraMoveListener m_OnCameraMoveListener = new MapboxMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            CameraPosition camera_pos = m_mapboxMap.getCameraPosition();
            Log.d(TAG, "(fragment) onCameraMove: "
                    + "camera_pos.target.getLatitude(): " + camera_pos.target.getLatitude()
                    + "; camera_pos.target.getLongitude(): " + camera_pos.target.getLongitude()
                    + "; camera_pos.zoom: " + camera_pos.zoom
            );
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "(fragment) onStart: calling mapView.onStart()...");
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "(fragment) onResume: calling mapView.onResume()...");
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "(fragment) onPause: calling mapView.onPause()...");
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "(fragment) onStop: calling mapView.onStop()...");
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Log.d(TAG, "(fragment) onLowMemory: calling mapView.onLowMemory()...");
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "(fragment) onDestroyView: calling mapView.onDestroy()...");
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "(fragment) onSaveInstanceState: calling mapView.onSaveInstanceState()...");
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mFragInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "(fragment) onDetach: entered");
        super.onDetach();
        mFragInteractionListener = null;
        m_okhttp3_client_dispather.cancelAll();
    }
}
