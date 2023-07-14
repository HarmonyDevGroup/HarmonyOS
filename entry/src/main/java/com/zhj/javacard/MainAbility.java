package com.zhj.javacard;

import com.zhj.javacard.slice.MainAbilitySlice;
import com.zhj.javacard.widget.controller.*;
import ohos.aafwk.ability.*;
import ohos.aafwk.content.Intent;
import ohos.agp.render.render3d.Engine;
import ohos.agp.window.service.WindowManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.timeutility.Time;
import ohos.utils.zson.ZSONObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MainAbility extends Ability {
    public static final int DEFAULT_DIMENSION_2X2 = 2; //System-defined constant. Do not change its value.
    public static final int DIMENSION_1X2 = 1;
    public static final int DIMENSION_2X4 = 3;
    public static final int DIMENSION_4X4 = 4;
    private static final int INVALID_FORM_ID = -1;
    private static final HiLogLabel TAG = new HiLogLabel(HiLog.DEBUG, 0x0, MainAbility.class.getName());
    private String topWidgetSlice;
    private boolean triggerTag = true;
    private static final String TAG_LOG = "LogUtil";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(0, 0, TAG_LOG);

    private static final String LOG_FORMAT = "%{public}s: %{public}s";
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        WindowManager.getInstance().getTopWindow().get().addFlags(WindowManager.LayoutConfig.MARK_ALLOW_EXTEND_LAYOUT);
        if (intentFromWidget(intent)) {
            topWidgetSlice = getRoutePageSlice(intent);
            if (topWidgetSlice != null) {
                setMainRoute(topWidgetSlice);
            }
        }
    }

    @Override
    protected ProviderFormInfo onCreateForm(Intent intent) {
        super.onCreateForm(intent);
        WindowManager.getInstance().getTopWindow().get().addFlags(WindowManager.LayoutConfig.MARK_ALLOW_EXTEND_LAYOUT);
        HiLog.info(TAG,"Start!!!!");
        String dateTime = Time.getTimeZone();
        String [] arr = dateTime.split("-");
        String date = arr[0];
        String time = arr[1];

        ZSONObject zsonObject = new ZSONObject();
        zsonObject.put("date", date);
        zsonObject.put("time", time);
        ProviderFormInfo formInfo = new ProviderFormInfo();
        formInfo.setJsBindingData(new FormBindingData(zsonObject));
        return formInfo;
    }

    @Override
    protected void onUpdateForm(long formId) {
        super.onUpdateForm(formId);
        ZSONObject newInfo = new ZSONObject();
        newInfo.put("temperature", "15°");
        FormBindingData formBindingData = new FormBindingData(newInfo);
        try {
            updateForm(formId, formBindingData);
        } catch (FormException ignore) {}
    }

    @Override
    protected void onDeleteForm(long formId) {
        HiLog.info(TAG, "onDeleteForm: formId=" + formId);
        super.onDeleteForm(formId);
        FormControllerManager formControllerManager = FormControllerManager.getInstance(this);
        FormController formController = formControllerManager.getController(formId);
        formController.onDeleteForm(formId);
        formControllerManager.deleteFormController(formId);
    }

    @Override
    protected void onTriggerFormEvent(long formId, String message) {
        super.onTriggerFormEvent(formId,message);
        HiLog.info(TAG,"click!");
        ZSONObject zsonObject = ZSONObject.stringToZSON(message);
        String msg = zsonObject.getString("message");
        if(msg.equalsIgnoreCase("change city")) {
            ZSONObject newInfo = new ZSONObject();
            newInfo.put("temperature", "15°");
            if (triggerTag) {
                newInfo.put("temperature", "15°");
                newInfo.put("time","12:00");
            } else {
                newInfo.put("temperature", "18°");
                newInfo.put("time","15:00");
            }
            triggerTag = !triggerTag;

            FormBindingData formBindingData = new FormBindingData(newInfo);
            try {
                updateForm(formId, formBindingData);
            } catch (FormException ignore) {}
        }
    }

    private boolean intentFromWidget(Intent intent) {
        long formId = intent.getLongParam(AbilitySlice.PARAM_FORM_IDENTITY_KEY, INVALID_FORM_ID);
        return formId != INVALID_FORM_ID;
    }

    private String getRoutePageSlice(Intent intent) {
        long formId = intent.getLongParam(AbilitySlice.PARAM_FORM_IDENTITY_KEY, INVALID_FORM_ID);
        if (formId == INVALID_FORM_ID) {
            return null;
        }
        FormControllerManager formControllerManager = FormControllerManager.getInstance(this);
        FormController formController = formControllerManager.getController(formId);
        if (formController == null) {
            return null;
        }
        Class<? extends AbilitySlice> clazz = formController.getRoutePageSlice(intent);
        if (clazz == null) {
            return null;
        }
        return clazz.getName();
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intentFromWidget(intent)) { // Only response to it when starting from a service widget.
            String newWidgetSlice = getRoutePageSlice(intent);
            if (topWidgetSlice == null || !topWidgetSlice.equals(newWidgetSlice)) {
                topWidgetSlice = newWidgetSlice;
                restart();
            }
        } else {
            if (topWidgetSlice != null) {
                topWidgetSlice = null;
                restart();
            }
        }
    }
}
