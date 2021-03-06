/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010-2016 Orange.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.gsma.rcs.core.control.settings;

import com.gsma.services.rcs.CommonServiceConfiguration;
import com.gsma.services.rcs.CommonServiceConfiguration.MinimumBatteryLevel;
import com.gsma.services.rcs.RcsGenericException;
import com.gsma.services.rcs.RcsPermissionDeniedException;
import com.gsma.services.rcs.RcsServiceControl;
import com.gsma.services.rcs.RcsServiceException;
import com.gsma.services.rcs.RcsServiceListener;
import com.gsma.services.rcs.RcsServiceNotAvailableException;

import com.gsma.rcs.api.connection.ConnectionManager.RcsServiceName;
import com.gsma.rcs.api.connection.utils.ExceptionUtil;
import com.gsma.rcs.api.connection.utils.RcsPreferenceActivity;
import com.gsma.rcs.core.control.CoreControlApplication;
import com.gsma.rcs.core.control.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Settings display
 * 
 * @author Jean-Marc AUFFRET
 */
@SuppressWarnings("deprecation")
public class SettingsDisplay extends RcsPreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static final int PROGRESS_INIT_INCREMENT = 100;

    private final static int SERVICE_DEACTIVATION_CONFIRMATION_DIALOG = 1;

    private static final String LOGTAG = "[SET][" + SettingsDisplay.class.getSimpleName() + "]";

    private RcsServiceControl mRcsServiceControl;

    private CheckBoxPreference mRcsActivationCheckbox;

    private ListPreference mBatteryLevel;

    private RcsServiceListener mRcsServiceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rcs_settings_preferences);

        initialize();

        mRcsActivationCheckbox = (CheckBoxPreference) getPreferenceScreen().findPreference(
                "rcs_activation");
        mBatteryLevel = (ListPreference) findPreference("min_battery_level");

        mRcsServiceControl = CoreControlApplication.getRcsServiceControl();

        if (!CoreControlApplication.sCnxManagerStarted) {
            new WaitForConnectionManagerStart()
                    .execute(CoreControlApplication.DELAY_FOR_STARTING_CNX_MANAGER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOGTAG, "onResume");
        startMonitorApiCnx(mRcsServiceListener, RcsServiceName.FILE_TRANSFER, RcsServiceName.CHAT);

        if (!mRcsServiceControl.isAvailable()) {
            initCheckbox(mRcsActivationCheckbox, false, false);
            enablePreferences(false);
            showMessage(R.string.label_service_not_available);
            return;
        }

        try {
            boolean isServiceActivated = mRcsServiceControl.isActivated();
            boolean isChangeable = mRcsServiceControl.isActivationModeChangeable();
            boolean isServiceConnected = isServiceConnected(RcsServiceName.FILE_TRANSFER);
            initCheckbox(mRcsActivationCheckbox, (isServiceActivated), isChangeable);
            enablePreferences(isServiceActivated && isServiceConnected);
            if (isServiceConnected) {
                initBatteryLevel(getFileTransferApi().getCommonConfiguration());
            }

        } catch (RcsServiceException e) {
            enablePreferences(false);
            Log.d(LOGTAG, e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMonitorApiCnx();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        try {
            if (preference == mRcsActivationCheckbox) {
                if (mRcsActivationCheckbox.isChecked()) {
                    try {
                        mRcsServiceControl.setActivationMode(true);

                    } catch (RcsPermissionDeniedException e) {
                        showMessage(R.string.text_service_activate_unchangeable);
                    }
                } else {
                    if (mRcsServiceControl.isActivated()) {
                        showDialog(SERVICE_DEACTIVATION_CONFIRMATION_DIALOG);
                    }
                }
                return true;
            }
        } catch (RcsServiceException e) {
            showException(e);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SERVICE_DEACTIVATION_CONFIRMATION_DIALOG:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(R.string.rcs_settings_label_confirm)
                        .setMessage(R.string.rcs_settings_label_rcs_service_shutdown)
                        .setNegativeButton(R.string.rcs_settings_label_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int button) {
                                        mRcsActivationCheckbox.setChecked(!mRcsActivationCheckbox
                                                .isChecked());
                                    }
                                })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mRcsActivationCheckbox.setChecked(!mRcsActivationCheckbox
                                        .isChecked());
                            }
                        })
                        .setPositiveButton(R.string.rcs_settings_label_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int button) {
                                        /* Stop running service */
                                        enablePreferences(false);
                                        try {
                                            mRcsServiceControl.setActivationMode(false);

                                        } catch (RcsPermissionDeniedException e) {
                                            showMessage(R.string.text_service_activate_unchangeable);

                                        } catch (RcsGenericException e) {
                                            showException(e);
                                        }
                                    }
                                }).setCancelable(true).create();
                registerDialog(dialog);
                return dialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if ("min_battery_level".equals(preference.getKey())) {
            try {
                int level = Integer.parseInt((String) objValue);
                CommonServiceConfiguration configuration = getFileTransferApi()
                        .getCommonConfiguration();
                configuration.setMinimumBatteryLevel(MinimumBatteryLevel.valueOf(level));

            } catch (RcsServiceException e) {
                showException(e);
            }
        }
        return true;
    }

    /**
     * Initialize the service activation checkbox
     * 
     * @param checked checked
     * @param enabled enabled
     */
    private void initCheckbox(CheckBoxPreference checkbox, boolean checked, boolean enabled) {
        checkbox.setChecked(checked);
        checkbox.setEnabled(enabled);
    }

    private void initialize() {
        mRcsServiceListener = new RcsServiceListener() {

            @Override
            public void onServiceConnected() {
                try {
                    enablePreferences(true);
                    initCheckbox(mRcsActivationCheckbox, true,
                            mRcsServiceControl.isActivationModeChangeable());
                    initBatteryLevel(getFileTransferApi().getCommonConfiguration());

                } catch (RcsServiceNotAvailableException ignore) {

                } catch (RcsServiceException e) {
                    Log.w(LOGTAG, ExceptionUtil.getFullStackTrace(e));
                }
            }

            @Override
            public void onServiceDisconnected(ReasonCode reasonCode) {
                boolean changeable;
                try {
                    changeable = mRcsServiceControl.isActivationModeChangeable();

                } catch (RcsGenericException e) {
                    Log.w(LOGTAG, ExceptionUtil.getFullStackTrace(e));
                    changeable = true;
                }
                enablePreferences(false);
                initCheckbox(mRcsActivationCheckbox, false, changeable);
            }
        };
    }

    /**
     * Enable / disable preferences
     * 
     * @param enabled enabled
     */
    private void enablePreferences(boolean enabled) {
        findPreference("min_battery_level").setEnabled(enabled);
        findPreference("userprofile_settings").setEnabled(enabled);
        findPreference("messaging_settings").setEnabled(enabled);
    }

    /**
     * Initialize battery level from configuration
     * 
     * @param configuration configuration
     * @throws RcsServiceException
     */
    private void initBatteryLevel(CommonServiceConfiguration configuration)
            throws RcsServiceException {
        mBatteryLevel.setPersistent(false);
        mBatteryLevel.setOnPreferenceChangeListener(this);
        mBatteryLevel.setValue(String.valueOf(configuration.getMinimumBatteryLevel().toInt()));
    }

    private class WaitForConnectionManagerStart extends AsyncTask<Long, Void, Void> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = SettingsDisplay.this.showProgressDialog(SettingsDisplay.this
                    .getString(R.string.rcs_settings_label_wait_cnx_start));
        }

        @Override
        protected Void doInBackground(Long... duration) {
            long delay = (duration[0] / PROGRESS_INIT_INCREMENT);
            for (int i = 0; i < PROGRESS_INIT_INCREMENT; i++) {
                try {
                    Thread.sleep(delay);
                    if (CoreControlApplication.sCnxManagerStarted) {
                        break;
                    }
                } catch (InterruptedException ignore) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            mProgressDialog.cancel();
        }

    }

}
