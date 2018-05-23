package go_spatial.com.github.tegola.mobile.android.ux;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public final class TegolaCapabilities implements Parcelable {
    public String root_url = "";
    public JSONObject root_json_object = null;
    public String version = "";
    public static class Parsed implements Parcelable {
        public static class Map implements Parcelable {
            public String name = "";
            public String attribution = "";
            public String mbgl_style_json_url = "";
            public static class Center implements Parcelable {
                public double
                        latitude = 0.0,
                        longitude = 0.0,
                        zoom = 0.0;

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeDouble(this.latitude);
                    dest.writeDouble(this.longitude);
                    dest.writeDouble(this.zoom);
                }

                public Center() {
                }

                protected Center(Parcel in) {
                    this.latitude = in.readDouble();
                    this.longitude = in.readDouble();
                    this.zoom = in.readDouble();
                }

                public static final Creator<Center> CREATOR = new Creator<Center>() {
                    @Override
                    public Center createFromParcel(Parcel source) {
                        return new Center(source);
                    }

                    @Override
                    public Center[] newArray(int size) {
                        return new Center[size];
                    }
                };
            }
            public final Center center;
            public static class Layer implements Parcelable {
                public String name = "";
                public double
                        minzoom = 0.0,
                        maxzoom = 0.0;

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(this.name);
                    dest.writeDouble(this.minzoom);
                    dest.writeDouble(this.maxzoom);
                }

                public Layer() {
                }

                protected Layer(Parcel in) {
                    this.name = in.readString();
                    this.minzoom = in.readDouble();
                    this.maxzoom = in.readDouble();
                }

                public static final Creator<Layer> CREATOR = new Creator<Layer>() {
                    @Override
                    public Layer createFromParcel(Parcel source) {
                        return new Layer(source);
                    }

                    @Override
                    public Layer[] newArray(int size) {
                        return new Layer[size];
                    }
                };
            }
            public Layer[] layers = null;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.name);
                dest.writeString(this.attribution);
                dest.writeString(this.mbgl_style_json_url);
                dest.writeParcelable(this.center, flags);
                dest.writeTypedArray(this.layers, flags);
            }

            public Map() {
                center = new Center();
            }

            protected Map(Parcel in) {
                this.name = in.readString();
                this.attribution = in.readString();
                this.mbgl_style_json_url = in.readString();
                this.center = in.readParcelable(Center.class.getClassLoader());
                this.layers = in.createTypedArray(Layer.CREATOR);
            }

            public static final Creator<Map> CREATOR = new Creator<Map>() {
                @Override
                public Map createFromParcel(Parcel source) {
                    return new Map(source);
                }

                @Override
                public Map[] newArray(int size) {
                    return new Map[size];
                }
            };
        }
        public Map[] maps = null;
        public double
                maps_layers_inf_minzoom = -1.0,
                maps_layers_sup_maxzoom = -1.0;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedArray(this.maps, flags);
            dest.writeDouble(this.maps_layers_inf_minzoom);
            dest.writeDouble(this.maps_layers_sup_maxzoom);
        }

        public Parsed() {
        }

        protected Parsed(Parcel in) {
            this.maps = in.createTypedArray(Map.CREATOR);
            this.maps_layers_inf_minzoom = in.readDouble();
            this.maps_layers_sup_maxzoom = in.readDouble();
        }

        public static final Creator<Parsed> CREATOR = new Creator<Parsed>() {
            @Override
            public Parsed createFromParcel(Parcel source) {
                return new Parsed(source);
            }

            @Override
            public Parsed[] newArray(int size) {
                return new Parsed[size];
            }
        };
    }
    public final Parsed parsed;

    public TegolaCapabilities() {
        parsed = new Parsed();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.root_url);
        dest.writeString(this.version);
        dest.writeParcelable(this.parsed, flags);
    }

    protected TegolaCapabilities(Parcel in) {
        this.root_url = in.readString();
        this.version = in.readString();
        this.parsed = in.readParcelable(Parsed.class.getClassLoader());
    }

    public static final Creator<TegolaCapabilities> CREATOR = new Creator<TegolaCapabilities>() {
        @Override
        public TegolaCapabilities createFromParcel(Parcel source) {
            return new TegolaCapabilities(source);
        }

        @Override
        public TegolaCapabilities[] newArray(int size) {
            return new TegolaCapabilities[size];
        }
    };
}
