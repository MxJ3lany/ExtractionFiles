package com.linsh.utilseverywhere;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2017/11/10
 *    desc   : 工具类: 对 JsonObject 的处理进行了封装
 *
 *             已过时, 将移出 Utils-Everywhere
 * </pre>
 */
@Deprecated
public class JsonObjectUtils {

    private JsonObjectUtils() {
    }

    public static JsonObjectBuilder parseJson(String json) {
        return new JsonObjectBuilder(json);
    }

    public static JsonArrayBuilder parseJsonArray(String json) {
        return new JsonArrayBuilder(json);
    }

    public static JsonObjectBuilder newObject() {
        return new JsonObjectBuilder();
    }

    public static JsonArrayBuilder newArray() {
        return new JsonArrayBuilder();
    }

    public static class JsonObjectBuilder {
        private JSONObject mJsonObject;

        public JsonObjectBuilder() {
            mJsonObject = new JSONObject();
        }

        public JsonObjectBuilder(String json) {
            try {
                mJsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JsonObjectBuilder(JSONObject jsonObject) {
            mJsonObject = jsonObject;
        }

        public String getString(String name) {
            return mJsonObject.optString(name);
        }

        public int getInt(String name) {
            return mJsonObject.optInt(name);
        }

        public boolean getBoolean(String name) {
            return mJsonObject.optBoolean(name);
        }

        public long getLong(String name) {
            return mJsonObject.optLong(name);
        }

        public double getDouble(String name) {
            return mJsonObject.optDouble(name);
        }

        public JSONObject getJsonObject(String name) {
            return mJsonObject.optJSONObject(name);
        }

        public JSONArray getJsonArray(String name) {
            return mJsonObject.optJSONArray(name);
        }

        public JsonObjectBuilder put(String name, Object value) {
            try {
                mJsonObject.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonObjectBuilder put(String name, int value) {
            try {
                mJsonObject.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonObjectBuilder put(String name, long value) {
            try {
                mJsonObject.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonObjectBuilder put(String name, boolean value) {
            try {
                mJsonObject.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonObjectBuilder put(String name, float value) {
            try {
                mJsonObject.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonObjectBuilder put(String name, double value) {
            try {
                mJsonObject.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public String toString() {
            return mJsonObject.toString();
        }

        public JSONObject toObject() {
            return mJsonObject;
        }
    }

    public static class JsonArrayBuilder {
        private JSONArray mJsonArray;

        public JsonArrayBuilder() {
            mJsonArray = new JSONArray();
        }

        public JsonArrayBuilder(String json) {
            try {
                mJsonArray = new JSONArray(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JsonArrayBuilder(JSONArray jsonArray) {
            mJsonArray = jsonArray;
        }

        public JsonObjectBuilder getJsonObject(int index) {
            try {
                return new JsonObjectBuilder(mJsonArray.getJSONObject(index));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public JsonArrayBuilder getJsonArray(int index) {
            try {
                return new JsonArrayBuilder(mJsonArray.getJSONArray(index));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public JsonArrayBuilder put(Object value) {
            mJsonArray.put(value);
            return this;
        }

        public JsonArrayBuilder put(int value) {
            mJsonArray.put(value);
            return this;
        }

        public JsonArrayBuilder put(long value) {
            mJsonArray.put(value);
            return this;
        }

        public JsonArrayBuilder put(float value) {
            try {
                mJsonArray.put(value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonArrayBuilder put(double value) {
            try {
                mJsonArray.put(value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JsonArrayBuilder put(boolean value) {
            mJsonArray.put(value);
            return this;
        }

        public String toString() {
            return mJsonArray.toString();
        }

        public JSONArray toObject() {
            return mJsonArray;
        }
    }
}
