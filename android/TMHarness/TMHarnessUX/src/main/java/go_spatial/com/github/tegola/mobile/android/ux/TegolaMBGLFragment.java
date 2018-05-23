package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbox.mapboxsdk.camera.CameraPosition;
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

    private static final String ARG__TEGOLA_CAPABILITIES = "TEGOLA_CAPABILITIES";
    private TegolaCapabilities tegolaCapabilities = null;
    private static final String ARG__MBMAP_DEBUG_ACTIVE = "mbmap_debug_active";
    private boolean mbmap_debug_active = false;

    private OnFragmentInteractionListener mFragInteractionListener;
    private MapView mapView = null;

    private Dispatcher m_okhttp3_client_dispather = null;
    private OkHttpClient m_okhttp3_client = null;
    private MapboxMap m_mapboxMap = null;
    private TextView m_tv_map_name = null;
    private TextView m_tv_map_attribution = null;
    private ImageButton ibtn_hide_mbgl_frag = null;

    public TegolaMBGLFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tegolaCapabilities mbgl_style_url
     * @return A new instance of fragment TegolaMBGLFragment.
     */
    public static TegolaMBGLFragment newInstance(final TegolaCapabilities tegolaCapabilities, final boolean mbmap_debug_active) {
        TegolaMBGLFragment fragment = new TegolaMBGLFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG__TEGOLA_CAPABILITIES, tegolaCapabilities);
        args.putBoolean(ARG__MBMAP_DEBUG_ACTIVE, mbmap_debug_active);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tegolaCapabilities = getArguments().getParcelable(ARG__TEGOLA_CAPABILITIES);
            mbmap_debug_active = getArguments().getBoolean(ARG__MBMAP_DEBUG_ACTIVE);
        } else {
            Log.e(TAG, "(fragment) onCreate: cannot start mbgl mapview without mbgl style url!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View this_frag_layout_view = inflater.inflate(R.layout.fragment_mapbox, container, false);

        HttpRequestUtil.setLogEnabled(true);
        HttpRequestUtil.setPrintRequestUrlOnFailure(true);
        m_okhttp3_client_dispather = new Dispatcher();
        m_okhttp3_client_dispather.setMaxRequestsPerHost(BuildConfig.mbgl_http_max_requests_per_host);
        Log.d(TAG, "(fragment) onCreateView: mbgl http max requrests host set to: " + BuildConfig.mbgl_http_max_requests_per_host);
        m_okhttp3_client = new OkHttpClient.Builder()
            .dispatcher(m_okhttp3_client_dispather)
            .readTimeout(BuildConfig.mbgl_http_read_timeout, TimeUnit.SECONDS)
            .build();
        Log.d(TAG, "(fragment) onCreateView: mbgl http read timeout set to: " + BuildConfig.mbgl_http_read_timeout + " seconds");
        HttpRequestUtil.setOkHttpClient(m_okhttp3_client);

        mapView = (MapView)this_frag_layout_view.findViewById(R.id.mapView);
        m_tv_map_name = (TextView)this_frag_layout_view.findViewById(R.id.tv_map_name);
        m_tv_map_attribution = (TextView)this_frag_layout_view.findViewById(R.id.tv_map_attribution);
        ibtn_hide_mbgl_frag = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_hide_mbgl_frag);
        ibtn_hide_mbgl_frag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragInteractionListener != null)
                    mFragInteractionListener.onFragmentInteraction(OnFragmentInteractionListener.E_MBGL_FRAG_ACTION.HIDE);
            }
        });

        mapView.getMapAsync(m_OnMapReadyCallback);

        Log.d(TAG, "(fragment) onCreateView: mbglstyle url: " + tegolaCapabilities.parsed.maps[0].mbgl_style_json_url);
        mapView.setStyleUrl(tegolaCapabilities.parsed.maps[0].mbgl_style_json_url);

        Log.d(TAG, "(fragment) onCreateView: calling mapView.onCreate()...");
        mapView.onCreate(savedInstanceState);

        return this_frag_layout_view;
    }

    final private OnMapReadyCallback m_OnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            m_mapboxMap = mapboxMap;

            m_mapboxMap.addOnCameraMoveListener(m_OnCameraMoveListener);
            Log.d(TAG, "(fragment) onMapReady: setting DebugActive to: " + mbmap_debug_active);
            m_mapboxMap.setDebugActive(mbmap_debug_active);
            m_mapboxMap.getUiSettings().setCompassEnabled(true);

            if (tegolaCapabilities.parsed.maps_layers_inf_minzoom != -1.0)
                m_mapboxMap.setMinZoomPreference(tegolaCapabilities.parsed.maps_layers_inf_minzoom);
            if (tegolaCapabilities.parsed.maps_layers_sup_maxzoom != -1.0)
                m_mapboxMap.setMaxZoomPreference(tegolaCapabilities.parsed.maps_layers_sup_maxzoom);
            Log.d(TAG, "(fragment) onMapReady: map is ready - min zoom level: " + m_mapboxMap.getMinZoomLevel() + "; max zoom level: " + m_mapboxMap.getMaxZoomLevel());

            CameraPosition camera_pos = null;
            if (tegolaCapabilities.parsed.maps[0].center.latitude != -1.0 && tegolaCapabilities.parsed.maps[0].center.longitude != -1.0 && tegolaCapabilities.parsed.maps[0].center.zoom != -1.0) {
                Log.d(TAG, "(fragment) onMapReady: map is ready - setting camera to new pos (parsed from capabilities json): "
                        + "camera_pos.target.getLatitude() := " + tegolaCapabilities.parsed.maps[0].center.latitude
                        + "; camera_pos.target.getLongitude() := " + tegolaCapabilities.parsed.maps[0].center.longitude
                        + "; camera_pos.zoom := " + tegolaCapabilities.parsed.maps[0].center.zoom
                        + "; camera_pos.bearing := 0.0"
                );
                m_mapboxMap.setCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(tegolaCapabilities.parsed.maps[0].center.latitude, tegolaCapabilities.parsed.maps[0].center.longitude))
                                .zoom(tegolaCapabilities.parsed.maps[0].center.zoom)
                                .bearing(0.0)
                                .build()
                );
            }
            camera_pos = m_mapboxMap.getCameraPosition();
            Log.d(TAG, "(fragment) onMapReady: map is ready - camera initial pos: "
                    + "camera_pos.target.getLatitude(): " + camera_pos.target.getLatitude()
                    + "; camera_pos.target.getLongitude(): " + camera_pos.target.getLongitude()
                    + "; camera_pos.zoom: " + camera_pos.zoom
                    + "; camera_pos.bearing: " + camera_pos.bearing
            );

            Log.d(TAG, "(fragment) onMapReady: setting PrefetchedTiles to: true");
            m_mapboxMap.setPrefetchesTiles(true);

            m_tv_map_name.setText(tegolaCapabilities.parsed.maps[0].name);
            m_tv_map_attribution.setText(tegolaCapabilities.parsed.maps[0].attribution);
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

        m_okhttp3_client.dispatcher().cancelAll();
        m_okhttp3_client = null;
        m_okhttp3_client_dispather = null;
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
    }
}
