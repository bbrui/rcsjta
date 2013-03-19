/*
 * Copyright 2013, France Telecom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gsma.joyn.messaging;

import java.lang.String;

/**
 * Class FileSharingError.
 *
 * @author Jean-Marc AUFFRET (Orange)
 * @version 1.0
 * @since 1.0
 */
public class FileSharingError extends org.gsma.joyn.session.ImsSessionBasedServiceError {
	
	static final long serialVersionUID = 1L;

    /**
     * Constant MEDIA_TRANSFER_FAILED.
     */
    public static final int MEDIA_TRANSFER_FAILED = 121;

    /**
     * Constant UNSUPPORTED_MEDIA_TYPE.
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 122;

    /**
     * Constant MEDIA_SAVING_FAILED.
     */
    public static final int MEDIA_SAVING_FAILED = 123;

    /**
     * Creates a new instance of FileSharingError.
     *
     * @param 
     */
    public FileSharingError(org.gsma.joyn.session.ImsServiceError error) {
        super((org.gsma.joyn.session.ImsServiceError) null);
    }

    /**
     * Creates a new instance of FileSharingError.
     *
     * @param 
     */
    public FileSharingError(int code) {
        super((org.gsma.joyn.session.ImsServiceError) null);
    }

    /**
     * Creates a new instance of FileSharingError.
     *
     * @param 
     * @param 
     */
    public FileSharingError(int code, String message) {
        super((org.gsma.joyn.session.ImsServiceError) null);
    }

}