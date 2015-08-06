/*
 * Copyright (C) 2014 Sony Mobile Communications Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gsma.rcs.utils;

import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * IntentUtils class sets appropriate flags to an intent using reflection
 */
public class IntentUtils {

    private static final int HONEYCOMB_MR1_VERSION_CODE = 12;

    private static final int JELLY_BEAN_VERSION_CODE = 16;

    private static final String ADD_FLAGS_METHOD_NAME = "addFlags";

    private static final Class<?>[] ADD_FLAGS_PARAM = new Class[] {
        int.class
    };

    private static final String FLAG_EXCLUDE_STOPPED_PACKAGES = "FLAG_EXCLUDE_STOPPED_PACKAGES";

    private static final String FLAG_RECEIVER_FOREGROUND = "FLAG_RECEIVER_FOREGROUND";

    /**
     * Using reflection to add FLAG_EXCLUDE_STOPPED_PACKAGES support backward compatibility.
     *
     * @param intent Intent to set flags
     */
    public static void tryToSetExcludeStoppedPackagesFlag(Intent intent) {

        if (Build.VERSION.SDK_INT < HONEYCOMB_MR1_VERSION_CODE) {
            /*
             * Since FLAG_EXCLUDE_STOPPED_PACKAGES is introduced only from API level
             * HONEYCOMB_MR1_VERSION_CODE we need to do nothing if we are running on a version prior
             * that so we just return then.
             */
            return;
        }

        try {
            Method addflagsMethod = intent.getClass().getDeclaredMethod(ADD_FLAGS_METHOD_NAME,
                    ADD_FLAGS_PARAM);
            Field flagExcludeStoppedPackages = intent.getClass().getDeclaredField(
                    FLAG_EXCLUDE_STOPPED_PACKAGES);
            addflagsMethod.invoke(intent, flagExcludeStoppedPackages.getInt(Intent.class));
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_EXCLUDE_STOPPED_PACKAGES to intent!", e);

        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_EXCLUDE_STOPPED_PACKAGES to intent!", e);

        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_EXCLUDE_STOPPED_PACKAGES to intent!", e);

        } catch (InvocationTargetException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_EXCLUDE_STOPPED_PACKAGES to intent!", e);
        }
    }

    /**
     * Using reflection to add FLAG_RECEIVER_FOREGROUND support backward compatibility.
     *
     * @param intent Intent to set flags
     */
    public static void tryToSetReceiverForegroundFlag(Intent intent) {

        if (Build.VERSION.SDK_INT < JELLY_BEAN_VERSION_CODE) {
            /*
             * Since FLAG_RECEIVER_FOREGROUND is introduced only from API level
             * JELLY_BEAN_VERSION_CODE we need to do nothing if we are running on a version prior
             * that so we just return then.
             */
            return;
        }

        try {
            Method addflagsMethod = intent.getClass().getDeclaredMethod(ADD_FLAGS_METHOD_NAME,
                    ADD_FLAGS_PARAM);
            Field flagReceiverForeground = intent.getClass().getDeclaredField(
                    FLAG_RECEIVER_FOREGROUND);
            addflagsMethod.invoke(intent, flagReceiverForeground.getInt(Intent.class));
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_RECEIVER_FOREGROUND to intent!", e);

        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_RECEIVER_FOREGROUND to intent!", e);

        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_RECEIVER_FOREGROUND to intent!", e);

        } catch (InvocationTargetException e) {
            throw new UnsupportedOperationException(
                    "Could not add FLAG_RECEIVER_FOREGROUND to intent!", e);
        }
    }
}