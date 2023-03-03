package com.example.devicecontrol.slice;

import com.example.devicecontrol.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.zson.ZSONObject;
import okhttp3.*;

import java.io.IOException;

public class MainAbilitySlice extends AbilitySlice implements AbsButton.CheckedStateChangedListener {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(HiLog.LOG_APP, 0x002F5, "OrmContextSlice");
    Switch btn_switch;
    Text textTem;
    Text textHumidity;
    String response_data;
    String getTemUrl = "http://api.heclouds.com/devices/955854197/datastreams/temperature";
    String getHumidityUrl = "http://api.heclouds.com/devices/955854197/datastreams/humidity";

    @Override
    public void onStart(Intent intent) {
        HiLog.info(LABEL_LOG, "启动了");
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        btn_switch = (Switch) findComponentById(ResourceTable.Id_btn_switch);
        textTem = (Text) findComponentById(ResourceTable.Id_text);
        textHumidity = (Text) findComponentById(ResourceTable.Id_textShidu);
        btn_switch.setCheckedStateChangedListener(this);
        Get(getTemUrl);
        Get(getHumidityUrl);
    }

    //final String cloud_url = "http://api.heclouds.com/cmds?device_id=962195921";
    final String cloud_url = "http://api.heclouds.com/cmds?device_id=955854197";

    public void Control(boolean b) {
        if (b) {
            new Thread() {
                @Override
                public void run() {
                    ZSONObject control_command = new ZSONObject();
                    String key = "ledSwitch";
                    String object = "ON";
                    control_command.put(key, object);
                    try {
                        response_data = post(cloud_url, control_command.toString());
                        HiLog.info(LABEL_LOG, response_data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else if (!b) {
            new Thread() {
                @Override
                public void run() {
                    ZSONObject control_command = new ZSONObject();
                    String key = "ledSwitch";
                    String object = "OFF";
                    control_command.put(key, object);
                    try {
                        response_data = post(cloud_url, control_command.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    //    zWrCkawdyNgK6jFeU07gKPd9tJw=   mine
    //    gryM5amudrw0JUnim5SljOa16rY=
    public void Get(String url) {

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url)
                                .get()
                                .header("api-key", "gryM5amudrw0JUnim5SljOa16rY=")
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();

                        ZSONObject zsonObject = ZSONObject.stringToZSON(responseData);
                        String result = zsonObject.getString("data");
                        ZSONObject zsonObject1 = ZSONObject.stringToZSON(result);
                        String result1 = zsonObject1.getString("current_value");
                        HiLog.info(LABEL_LOG, result1);
                        if (url.equals(getTemUrl)) {
                            TaskDispatcher uiTaskDispather = getUITaskDispatcher();
                            uiTaskDispather.asyncDispatch(() -> textTem.setText("温度为：" + result1));
                        } else {
                            TaskDispatcher uiTaskDispather = getUITaskDispatcher();
                            uiTaskDispather.asyncDispatch(() -> textHumidity.setText("湿度为：" + result1));
                        }


                        Thread.sleep(1500);

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    public static String post(String url, String json) throws IOException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .header("api-key", "gryM5amudrw0JUnim5SljOa16rY=")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }


    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    public void onCheckedChanged(AbsButton absButton, boolean b) {
        Control(b);
    }

}
