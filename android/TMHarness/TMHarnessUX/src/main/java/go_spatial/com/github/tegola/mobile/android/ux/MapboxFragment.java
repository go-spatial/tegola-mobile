package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.sources.VectorSource;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapboxFragment extends android.support.v4.app.Fragment {
    public static final String TAG = MapboxFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TILEJSON = "tilejson";
    private static final String ARG_VECTOR_SOURCE_MATRIX = "mat_vector_source";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private MapView mapView = null;
    private String m_tilejson = null;
    private Bundle m_bundle_vector_source = null;


    public MapboxFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tilejson URL or assset://filename
     * @return A new instance of fragment MapboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapboxFragment newInstance(String tilejson) {
        MapboxFragment fragment = new MapboxFragment();
        if (tilejson != null) {
            Bundle args = new Bundle();
            args.putString(ARG_TILEJSON, tilejson);
            fragment.setArguments(args);
        }
        return fragment;
    }
    public static MapboxFragment newInstance(String[][] mat_vector_source) {
        MapboxFragment fragment = new MapboxFragment();
        if (mat_vector_source != null) {
            Bundle args = new Bundle();
            Bundle bundle_vector_source = new Bundle();
            for (int i = 0; i < mat_vector_source.length; i++) {
                String[] vector_source = mat_vector_source[i];
                bundle_vector_source.putString(vector_source[0], vector_source[1]);
            }
            args.putBundle(ARG_VECTOR_SOURCE_MATRIX, bundle_vector_source);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            m_tilejson = getArguments().getString(ARG_TILEJSON);
            m_bundle_vector_source = getArguments().getBundle(ARG_VECTOR_SOURCE_MATRIX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View this_frag_layout_view = inflater.inflate(R.layout.fragment_mapbox, container, false);

        mapView = (MapView)this_frag_layout_view.findViewById(R.id.mapView);
        Log.d(TAG, "(fragment) onCreateView: calling mapView.onCreate()...");
        if (m_tilejson != null) {
            Log.d(TAG, "(fragment) onCreateView: setting mapview StyleUrl to " + m_tilejson);
            mapView.setStyleUrl(m_tilejson);
        }
//        if (m_bundle_vector_source != null) {
//            for (String  i < m_bundle_vector_source.size(); i++) {
//                m_bundle_vector_source.keySet();
//                String[] vector_source = mat_vector_source[i];
//                bundle_vector_source.putString(vector_source[0], vector_source[1]);
//                VectorSource vectorSource = new VectorSource();
//            }
//        }
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Customize map with markers, polylines, etc.

            }
        });

        return this_frag_layout_view;
    }

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




    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "(fragment) onDetach: entered");
        super.onDetach();
        mListener = null;
    }

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
