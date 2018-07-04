package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.telemetry.TelemetryEnabler;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.http.HttpRequestUtil;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Telemetry;
import com.mapbox.mapboxsdk.maps.widgets.CompassView;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import go_spatial.com.github.tegola.mobile.android.controller.Utils;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FAIL_LOADING_MAP;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FINISH_LOADING_MAP;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FINISH_LOADING_STYLE;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FINISH_RENDERING_FRAME;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FINISH_RENDERING_FRAME_FULLY_RENDERED;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FINISH_RENDERING_MAP;
import static com.mapbox.mapboxsdk.maps.MapView.DID_FINISH_RENDERING_MAP_FULLY_RENDERED;
import static com.mapbox.mapboxsdk.maps.MapView.REGION_DID_CHANGE;
import static com.mapbox.mapboxsdk.maps.MapView.REGION_DID_CHANGE_ANIMATED;
import static com.mapbox.mapboxsdk.maps.MapView.REGION_IS_CHANGING;
import static com.mapbox.mapboxsdk.maps.MapView.REGION_WILL_CHANGE_ANIMATED;
import static com.mapbox.mapboxsdk.maps.MapView.SOURCE_DID_CHANGE;
import static com.mapbox.mapboxsdk.maps.MapView.WILL_START_LOADING_MAP;
import static com.mapbox.mapboxsdk.maps.MapView.WILL_START_RENDERING_FRAME;
import static com.mapbox.mapboxsdk.maps.MapView.WILL_START_RENDERING_MAP;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MBGLFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MBGLFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MBGLFragment extends android.support.v4.app.Fragment implements LocationUpdatesManager.LocationUpdatesBrokerListener {
    public static final String TAG = MBGLFragment.class.getCanonicalName();
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

    private View loadingPanel = null;
    private MapView mapView = null;

    private Dispatcher m_okhttp3_client_dispather = null;
    private OkHttpClient m_okhttp3_client = null;
    private MapboxMap m_mapboxMap = null;
    private TextView m_tv_attribution = null;
    private TextView m_tv_version = null;
    private TextView m_tv_camera_loc = null;

    private CompassView m_cv = null;
    private View m_cam_ctrl_container = null;
    private ImageButton m_ibtn_zoom_in = null;
    private ImageButton m_ibtn_rotate_left = null;
    private ImageButton m_ibtn_rotate_up = null;
    private ImageButton m_ibtn_rotate_down = null;
    private ImageButton m_ibtn_rotate_right = null;
    private ImageButton m_ibtn_zoom_out = null;
    private ImageButton m_ibtn_goto_loc = null;
    private ImageButton m_ibtn_goto_map_ctr = null;

    private CheckBox m_ctv_show_camera_loc = null;
    private CheckBox m_ctv_sync_location = null;
    private ImageButton ibtn_hide_mbgl_frag = null;

    private final double ZOOM_BY = .25;
    private final double TILT_BY = 7.5; //degrees
    private final double ROTATE_BY = 7.5; //degrees

    public MBGLFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tegolaCapabilities mbgl_style_url
     * @return A new instance of fragment MBGLFragment.
     */
    public static MBGLFragment newInstance(final TegolaCapabilities tegolaCapabilities, final boolean mbmap_debug_active) {
        MBGLFragment fragment = new MBGLFragment();
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

    private EventListener m_okhttpeventlistener = new EventListener() {
        @Override
        public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.connectStart: for call " + call.request().toString());
            super.connectStart(call, inetSocketAddress, proxy);
        }

        @Override
        public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol, IOException ioe) {
            Log.d(TAG, "(fragment) okhttp.EventListener.connectFailed: for call " + call.request().toString() + "; err: " + ioe.getMessage());
            super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        }

        @Override
        public void secureConnectStart(Call call) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.secureConnectStart: for call " + call.request().toString());
            super.secureConnectStart(call);
        }

        @Override
        public void callStart(Call call) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.callStart: " + call.request().toString());
            super.callStart(call);
//            int
//                    n_running = m_okhttp3_client_dispather.runningCallsCount(),
//                    n_queued = m_okhttp3_client_dispather.queuedCallsCount(),
//                    n_total = n_running + n_queued;
//            Log.d(TAG,
//                    "(fragment) okhttp.EventListener.callStart: m_okhttp3_client_dispather calls pending: "
//                            + n_total
//                            + " (" + n_running + " running and " + n_queued + " queued)"
//            );
//            if (loadingPanel != null) {
//                MBGLFragment.this.getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadingPanel.setVisibility(View.VISIBLE);
//                    }
//                });
//            }
        }

        @Override
        public void callFailed(Call call, IOException ioe) {
            Log.d(TAG, "(fragment) okhttp.EventListener.callFailed: for call " + call.request().toString() + "; error: " + ioe.getMessage());
            super.callFailed(call, ioe);
//            int
//                    n_running = m_okhttp3_client_dispather.runningCallsCount(),
//                    n_queued = m_okhttp3_client_dispather.queuedCallsCount(),
//                    n_total = n_running + n_queued;
//            Log.d(TAG,
//                    "(fragment) okhttp.EventListener.callFailed: m_okhttp3_client_dispather calls pending: "
//                            + n_total
//                            + " (" + n_running + " running and " + n_queued + " queued)"
//            );
//            if (n_total == 0) {
//                if (loadingPanel != null) {
//                    MBGLFragment.this.getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadingPanel.setVisibility(View.GONE);
//                        }
//                    });
//                }
//            }
        }

        @Override
        public void responseHeadersStart(Call call) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.responseHeadersStart: for call " + call.request().toString());
            super.responseHeadersStart(call);
        }

        @Override
        public void responseHeadersEnd(Call call, Response response) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.responseHeadersEnd: for call " + call.request().toString());
            if (response.cacheResponse() != null) {
                Log.d(TAG, "(fragment) okhttp.EventListener.responseHeadersEnd: cached response exists for call " + call.request().toString());
            }
            super.responseHeadersEnd(call, response);
        }

        @Override
        public void responseBodyStart(Call call) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.responseBodyStart: for call " + call.request().toString());
            super.responseBodyStart(call);
        }

        @Override
        public void responseBodyEnd(Call call, long byteCount) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.responseBodyEnd: for call " + call.request().toString());
            super.responseBodyEnd(call, byteCount);
        }

        @Override
        public void callEnd(Call call) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.callEnd: " + call.request().toString());
            super.callEnd(call);
//            int
//                    n_running = m_okhttp3_client_dispather.runningCallsCount(),
//                    n_queued = m_okhttp3_client_dispather.queuedCallsCount(),
//                    n_total = n_running + n_queued;
//            Log.d(TAG,
//                    "(fragment) okhttp.EventListener.callEnd: m_okhttp3_client_dispather calls pending: "
//                            + n_total
//                            + " (" + n_running + " running and " + n_queued + " queued)"
//            );
//            if (n_total == 0) {
//                if (loadingPanel != null) {
//                    MBGLFragment.this.getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadingPanel.setVisibility(View.GONE);
//                        }
//                    });
//                }
//            }
        }

        @Override
        public void secureConnectEnd(Call call, @Nullable Handshake handshake) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.secureConnectEnd: for call" + call.request().toString());
            super.secureConnectEnd(call, handshake);
        }

        @Override
        public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {
            //Log.d(TAG, "(fragment) okhttp.EventListener.connectEnd: for call " + call.request().toString());
            super.connectEnd(call, inetSocketAddress, proxy, protocol);
        }
    };

    private Cache m_okhttp_cache = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View this_frag_layout_view = inflater.inflate(R.layout.fragment_mbgl, container, false);

        loadingPanel = this_frag_layout_view.findViewById(R.id.loadingPanel);

        if (BuildConfig.mbgl_http_cache_size > 0) {
            try {
                File okhttp_cache_dir = new File(getApplicationContext().getFilesDir().getCanonicalPath(), "okhttp");
                File okhttp_cache_file = new File(okhttp_cache_dir.getCanonicalPath(), "cache");
                Log.d(TAG, "(fragment) onCreateView: creating okhttp cache file " + okhttp_cache_file.getCanonicalPath() + " (" + BuildConfig.mbgl_http_cache_size + " KB) for mapview okhttpclient");
                m_okhttp_cache = new Cache(okhttp_cache_file, BuildConfig.mbgl_http_cache_size * 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HttpRequestUtil.setLogEnabled(true);
        HttpRequestUtil.setPrintRequestUrlOnFailure(true);
        m_okhttp3_client_dispather = new Dispatcher();
        m_okhttp3_client_dispather.setMaxRequestsPerHost(BuildConfig.mbgl_http_max_requests_per_host);
        m_okhttp3_client_dispather.setIdleCallback(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG, "(fragment) onCreateView: m_okhttp3_client_dispather.IdleCallback.run: ");
            }
        });
        Log.d(TAG, "(fragment) onCreateView: mbgl http max requrests host set to: " + BuildConfig.mbgl_http_max_requests_per_host);
        OkHttpClient.Builder okhttpclientbuilder = new OkHttpClient.Builder()
            .dispatcher(m_okhttp3_client_dispather)
            .connectTimeout(BuildConfig.mbgl_http_connect_timeout, TimeUnit.SECONDS)
            .readTimeout(BuildConfig.mbgl_http_read_timeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            //.connectionPool(new ConnectionPool(BuildConfig.mbgl_http_max_requests_per_host, 5, TimeUnit.SECONDS))
            .eventListener(m_okhttpeventlistener);
        if (m_okhttp_cache != null)
            okhttpclientbuilder.cache(m_okhttp_cache);
        m_okhttp3_client = okhttpclientbuilder.build();
        Log.d(TAG, "(fragment) onCreateView: mbgl http read timeout set to: " + BuildConfig.mbgl_http_read_timeout + " seconds");
        HttpRequestUtil.setOkHttpClient(m_okhttp3_client);

        mapView = (MapView)this_frag_layout_view.findViewById(R.id.mapView);
        mapView.addOnMapChangedListener(new MapView.OnMapChangedListener() {
            @Override
            public void onMapChanged(int change) {
                switch (change) {
                    case REGION_IS_CHANGING: {
//                        Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: REGION_IS_CHANGING");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                    case REGION_DID_CHANGE: {
//                        Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: REGION_DID_CHANGE");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                    case REGION_WILL_CHANGE_ANIMATED: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: REGION_WILL_CHANGE_ANIMATED");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                    case REGION_DID_CHANGE_ANIMATED: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: REGION_DID_CHANGE_ANIMATED");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                    case WILL_START_LOADING_MAP: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: WILL_START_LOADING_MAP");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                    case DID_FINISH_LOADING_MAP: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FINISH_LOADING_MAP");
                        if (loadingPanel.getVisibility() != View.GONE)
                            loadingPanel.setVisibility(View.GONE);
                        break;
                    }
                    case DID_FAIL_LOADING_MAP: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FAIL_LOADING_MAP");
                        if (loadingPanel.getVisibility() != View.GONE)
                            loadingPanel.setVisibility(View.GONE);
                        break;
                    }
                    case WILL_START_RENDERING_FRAME: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: WILL_START_RENDERING_FRAME");
                        break;
                    }
                    case DID_FINISH_RENDERING_FRAME: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FINISH_RENDERING_FRAME");
                        break;
                    }
                    case DID_FINISH_RENDERING_FRAME_FULLY_RENDERED: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FINISH_RENDERING_FRAME_FULLY_RENDERED");
                        if (loadingPanel.getVisibility() != View.GONE)
                            loadingPanel.setVisibility(View.GONE);
                        break;
                    }
                    case WILL_START_RENDERING_MAP: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: WILL_START_RENDERING_MAP");
                        break;
                    }
                    case DID_FINISH_RENDERING_MAP: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FINISH_RENDERING_MAP");
                        break;
                    }
                    case DID_FINISH_RENDERING_MAP_FULLY_RENDERED: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FINISH_RENDERING_MAP_FULLY_RENDERED");
                        break;
                    }
                    case DID_FINISH_LOADING_STYLE: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: DID_FINISH_LOADING_STYLE");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                    case SOURCE_DID_CHANGE: {
                        //Log.d(TAG, "(fragment) MapChangedListener.onMapChanged: change: SOURCE_DID_CHANGE");
                        if (loadingPanel.getVisibility() != View.VISIBLE)
                            loadingPanel.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });
        m_tv_attribution = (TextView)this_frag_layout_view.findViewById(R.id.tv_attribution);
        m_tv_version = (TextView)this_frag_layout_view.findViewById(R.id.tv_tegola_version);
        m_tv_camera_loc = (TextView)this_frag_layout_view.findViewById(R.id.tv_camera_loc);

        m_cam_ctrl_container = (View)this_frag_layout_view.findViewById(R.id.mv_sublayout_camera);
        m_ibtn_zoom_in = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_zoom_in);
        m_ibtn_zoom_in.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                move_camera(
                        null,
                        null,
                        m_mapboxMap.getCameraPosition().zoom + ZOOM_BY,
                        null,
                        null
                );
                return true;
            }
        });
        m_ibtn_zoom_out = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_zoom_out);
        m_ibtn_zoom_out.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                move_camera(
                        null,
                        null,
                        m_mapboxMap.getCameraPosition().zoom - ZOOM_BY,
                        null,
                        null
                );
                return true;
            }
        });
        m_ibtn_rotate_left = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_rotate_left);
        m_ibtn_rotate_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //ROTATE_BY
                move_camera(
                        null,
                        null,
                        null,
                        m_mapboxMap.getCameraPosition().bearing - ROTATE_BY,
                        null);
                return true;
            }
        });
        m_ibtn_rotate_up = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_rotate_up);
        m_ibtn_rotate_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                move_camera(
                        null,
                        null,
                        null,
                        null,
                        m_mapboxMap.getCameraPosition().tilt + TILT_BY);
                return true;
            }
        });
        m_ibtn_rotate_down = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_rotate_down);
        m_ibtn_rotate_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                move_camera(
                        null,
                        null,
                        null,
                        null,
                        m_mapboxMap.getCameraPosition().tilt - TILT_BY);
                return true;
            }
        });
        m_ibtn_rotate_right = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_rotate_right);
        m_ibtn_rotate_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //ROTATE_BY
                move_camera(
                        null,
                        null,
                        null,
                        m_mapboxMap.getCameraPosition().bearing + ROTATE_BY,
                        null);
                return true;
            }
        });
        m_ibtn_goto_loc = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_goto_loc);
        m_ibtn_goto_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        m_ibtn_goto_map_ctr = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_goto_map_ctr);
        m_ibtn_goto_map_ctr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move_camera(
                        tegolaCapabilities.parsed.maps[0].center.latitude,
                        tegolaCapabilities.parsed.maps[0].center.longitude,
                        tegolaCapabilities.parsed.maps[0].center.zoom,
                        0.0,
                        null
                );
            }
        });
        m_cv = (CompassView)this_frag_layout_view.findViewById(com.mapbox.mapboxsdk.R.id.compassView);

        m_ctv_show_camera_loc = (CheckBox)this_frag_layout_view.findViewById(R.id.ctv_show_camera_loc);
        m_ctv_show_camera_loc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_tv_camera_loc.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        m_ctv_sync_location = (CheckBox)this_frag_layout_view.findViewById(R.id.ctv_sync_location);
        m_ctv_sync_location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    LocationUpdatesManager.getInstance().start_updates();
                else
                    LocationUpdatesManager.getInstance().stop_updates();
            }
        });
        ibtn_hide_mbgl_frag = (ImageButton)this_frag_layout_view.findViewById(R.id.ibtn_hide_mbgl_frag);
        ibtn_hide_mbgl_frag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragInteractionListener != null)
                    mFragInteractionListener.onFragmentInteraction(OnFragmentInteractionListener.E_MBGL_FRAG_ACTION.HIDE);
            }
        });

        //disable mbgl telemetry
        Log.d(TAG, "(fragment) onCreateView: disabling mb telemetry");
        TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);
        Telemetry.initialize();

        mapView.getMapAsync(m_OnMapReadyCallback);
        Log.d(TAG, "(fragment) onCreateView: setting mbglstyle url from tegolaCapabilities.parsed.maps[0].mbgl_style_json_url: " + tegolaCapabilities.parsed.maps[0].mbgl_style_json_url);
        mapView.setStyleUrl(tegolaCapabilities.parsed.maps[0].mbgl_style_json_url);

        Log.d(TAG, "(fragment) onCreateView: calling mapView.onCreate()...");
        mapView.onCreate(savedInstanceState);

        return this_frag_layout_view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, String.format("(fragment) onViewCreated"));
    }

    private final double PREF_AUTO_SCALE_MINZOOM_DIVISOR = 5.0;
    private final double PREF_AUTO_SCALE_MINZOOM_FACTOR = 0.5;

    final private OnMapReadyCallback m_OnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            m_mapboxMap = mapboxMap;

            m_mapboxMap.addOnCameraMoveListener(m_OnCameraMoveListener);
            Log.d(TAG, "(fragment) onMapReady: setting DebugActive to: " + mbmap_debug_active);
            m_mapboxMap.setDebugActive(mbmap_debug_active);

            if (tegolaCapabilities.parsed.maps_layers_minzoom != -1.0) {
                Log.d(TAG, "(fragment) onMapReady: updating min zoom from tegolaCapabilities.parsed.maps_layers_minzoom: " + tegolaCapabilities.parsed.maps_layers_minzoom);
                m_mapboxMap.setMinZoomPreference(tegolaCapabilities.parsed.maps_layers_minzoom);
            }
            if (tegolaCapabilities.parsed.maps_layers_maxzoom != -1.0) {
                Log.d(TAG, "(fragment) onMapReady: updating max zoom from tegolaCapabilities.parsed.maps_layers_maxzoom: " + tegolaCapabilities.parsed.maps_layers_maxzoom);
                m_mapboxMap.setMaxZoomPreference(tegolaCapabilities.parsed.maps_layers_maxzoom);
            }
            Log.d(TAG, "(fragment) onMapReady: min zoom level: " + m_mapboxMap.getMinZoomLevel() + "; max zoom level: " + m_mapboxMap.getMaxZoomLevel());

            //adjust mb uisettings and attribution parsed from tegola capabilities before prefetching tiles
            m_mapboxMap.getUiSettings().setAllGesturesEnabled(true);
            m_mapboxMap.getUiSettings().setCompassEnabled(true);
            m_mapboxMap.getUiSettings().setCompassFadeFacingNorth(false);
            StringBuilder sb_attribution = new StringBuilder()
                .append(tegolaCapabilities.parsed.maps[0].attribution)
                .append(" - ")
                .append(tegolaCapabilities.parsed.maps[0].name);
            m_tv_attribution.setText(sb_attribution.toString());
            StringBuilder sb_verion = new StringBuilder()
                .append("Tegola Version")
                .append(": ")
                .append(tegolaCapabilities.version);
            m_tv_version.setText(sb_verion.toString());

            Log.d(TAG, "(fragment) onMapReady: setting PrefetchedTiles to: true");
            m_mapboxMap.setPrefetchesTiles(true);

//            int
//                    new_top = (m_cv.getVisibility() != View.GONE ? m_cv.getBottom() : 0),
//                    translate_y = new_top - (m_cam_ctrl_container.getTop() - 50);
//            Log.d(TAG, "(fragment) onMapReady: moving top of m_cam_ctrl_container by " + translate_y);
//            m_cam_ctrl_container.setTranslationY(translate_y);
            show_nav(true);
            m_ctv_show_camera_loc.setChecked(true);
            m_ctv_show_camera_loc.callOnClick();

            CameraPosition camera_pos = m_mapboxMap.getCameraPosition();
            Log.d(TAG, "(fragment) onMapReady: camera initial pos: "
                    + "camera_pos.target.getLatitude(): " + camera_pos.target.getLatitude()
                    + "; camera_pos.target.getLongitude(): " + camera_pos.target.getLongitude()
                    + "; camera_pos.zoom: " + camera_pos.zoom
                    + "; camera_pos.bearing: " + camera_pos.bearing
            );
            if (tegolaCapabilities.parsed.maps[0].center.latitude != -1.0 && tegolaCapabilities.parsed.maps[0].center.longitude != -1.0 && tegolaCapabilities.parsed.maps[0].center.zoom != -1.0)
                m_ibtn_goto_map_ctr.callOnClick();
        }
    };

    private void show_nav(boolean show) {
        m_cam_ctrl_container.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void move_camera(Double lat, Double lon, Double zoom, Double bearing, Double tilt) {
        CameraPosition.Builder camera_pos_builder = new CameraPosition.Builder();
        if (lat == null)
            lat = m_mapboxMap.getCameraPosition().target.getLatitude();
        if (lon == null)
            lon = m_mapboxMap.getCameraPosition().target.getLongitude();
        if (zoom == null)
            zoom = m_mapboxMap.getCameraPosition().zoom;
        if (bearing == null)
            bearing = m_mapboxMap.getCameraPosition().bearing;
        if (tilt == null)
            tilt = m_mapboxMap.getCameraPosition().tilt;
        Log.d(TAG, "(fragment) move_camera: setting camera to new pos: "
                + "lat := " + lat
                + "; lon := " + lon
                + "; zoom := " + zoom
                + "; bearing := " + bearing
                + "; tilt := " + tilt
        );
        m_mapboxMap.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                    .target(new LatLng(lat, lon))
                    .zoom(zoom)
                    .bearing(bearing)
                    .tilt(tilt)
                    .build()
            )
        );
    }

    @Override
    public void onBrokerLocationUpdate(Location location) {
        double
                lat = location.getLatitude(),
                lon = location.getLongitude(),
                bearing = location.getBearing();
        Log.d(TAG, "(fragment) onBrokerLocationUpdate: received location update from broker - easing camera to: "
                + "lat: " + lat
                + "; lon: " + lon
                + "; bearing: " + bearing
        );
        m_mapboxMap.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .bearing(location.getBearing())
                    .build()
            )
        );
    }

    long last_min_zoom_time = 0, last_max_zoom_time = 0;
    final private MapboxMap.OnCameraMoveListener m_OnCameraMoveListener = new MapboxMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            CameraPosition camera_pos = m_mapboxMap.getCameraPosition();
//            Log.d(TAG, "(fragment) onCameraMove: "
//                    + "camera_pos.target.getLatitude(): " + camera_pos.target.getLatitude()
//                    + "; camera_pos.target.getLongitude(): " + camera_pos.target.getLongitude()
//                    + "; camera_pos.zoom: " + camera_pos.zoom
//            );
            m_tv_camera_loc.setText(String.format("%.4f, %.4f, %.4f", camera_pos.target.getLatitude(), camera_pos.target.getLongitude(), camera_pos.zoom));
            long now = System.currentTimeMillis();
            if (m_mapboxMap.getCameraPosition().zoom == m_mapboxMap.getMinZoomLevel()) {
                if (now - last_min_zoom_time >= 5000)
                    Toast.makeText(getApplicationContext(), "Camera already at MIN zoom level (" + m_mapboxMap.getMinZoomLevel() + ")! Cannot zoom out any further.", Toast.LENGTH_SHORT).show();
                last_min_zoom_time = now;
            }
            if (m_mapboxMap.getCameraPosition().zoom == m_mapboxMap.getMaxZoomLevel()) {
                if (now - last_max_zoom_time >= 5000)
                    Toast.makeText(getApplicationContext(), "Camera already at MAX zoom level (" + m_mapboxMap.getMaxZoomLevel() + ")! Cannot zoom in any further.", Toast.LENGTH_SHORT).show();
                last_min_zoom_time = now;
            }
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

        if (m_okhttp_cache != null && m_okhttp_cache.directory().exists()) {
            try {
                Utils.Files.delete(m_okhttp_cache.directory());
            } catch (IOException e) {
                e.printStackTrace();
            }
            m_okhttp_cache = null;
        }

        LocationUpdatesManager.getInstance().stop_updates();
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
